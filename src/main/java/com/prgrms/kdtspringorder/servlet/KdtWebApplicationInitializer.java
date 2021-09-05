package com.prgrms.kdtspringorder.servlet;

import com.prgrms.kdtspringorder.customer.CustomerController;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class KdtWebApplicationInitializer implements WebApplicationInitializer { //springMVC에서는 WebApplicationInitializer를 대체로 구현하게 됨
    private static final Logger logger = LoggerFactory.getLogger(KdtWebApplicationInitializer.class);

    @EnableWebMvc //spring MVC에 필요한 빈들 자동 등록됨
    @Configuration
    @ComponentScan(basePackages = "com.prgrms.kdtspringorder.customer",
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = CustomerController.class),
        useDefaultFilters = false) //controller만 스캔하도록 설정. useDefaultFilters = false로 해줘야 scan할 때 다른 stereo 타입들이 등록되는걸 방지할 수 있다.
//    @EnableTransactionManagement //rootApplication이 있을 때는 필요 없음(web 관련 로직에서 구현할 부분이 아님)
    static class ServletConfig implements WebMvcConfigurer , ApplicationContextAware { //원하는 mvc내용 설정을 configuration 하고싶을때 implements WebMvcConfigurer

        ApplicationContext applicationContext;

        @Override
        public void configureViewResolvers(ViewResolverRegistry registry) {
            //특정한 viewReworver를 셋업해줌
            //jsp랑 thymleaf를 동시에 사용하려고 하면 충돌이 나기 때문에 두개에 대한 view name을 구분해줘야 함
            registry.jsp().viewNames("jsp/*");

            var springResourceTemplateResolver = new SpringResourceTemplateResolver();
            springResourceTemplateResolver.setApplicationContext(applicationContext);
            springResourceTemplateResolver.setPrefix("/WEB-INF/");
            springResourceTemplateResolver.setSuffix(".html");
            var springTemplateEngine = new SpringTemplateEngine();
            springTemplateEngine.setTemplateResolver(springResourceTemplateResolver);

            var thymeleafViewResolver = new ThymeleafViewResolver();
            thymeleafViewResolver.setTemplateEngine(springTemplateEngine);
            thymeleafViewResolver.setOrder(1); //view이름 mapping을 지정해놓고 여러개 중에 하나를 찾게한 것(jsp와 겹친것)
            thymeleafViewResolver.setViewNames(new String[]{"views/*"});
            registry.viewResolver(thymeleafViewResolver);
        }

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/resources/**") //요청 명
                    .addResourceLocations("/resources/")//위치(리소스 파일)
                    .setCachePeriod(60)  //캐시 시간 지정
                    .resourceChain(true)
                    .addResolver(new EncodedResourceResolver()); //특정 파일을 매핑. gzip파일이 있으면 그걸 먼저 사용?. 컨텐츠 용량이 줄어든다.

        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }

        @Override //configureMessageConverters가 기존에 있는 모든 컨버터를 오버라이드함. 기존의 j to h 메시지 컨버터를 사용하려면 추가를 또 해줘야하기 때문에 extendMessageConverters 메서드 사용
        public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {// 메시지 컨버터. default로 전달했던걸 원하는 메시지로 바꿀수 있음
            var messageConverter = new MarshallingHttpMessageConverter();//xml 생성을 위한 위한 컨버터 생성
            var xStreamMarshaller = new XStreamMarshaller(); //xml 만드는 툴 같은 느낌
            messageConverter.setMarshaller(xStreamMarshaller); //xml로 말아준다(?)
            messageConverter.setUnmarshaller(xStreamMarshaller); //xml을 java class객체로 인스턴스화 시켜줌
            converters.add(0, messageConverter);

            var javaTimeModule = new JavaTimeModule();
            var modules = Jackson2ObjectMapperBuilder.json().modules(javaTimeModule);
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_DATE_TIME));
            converters.add(1, new MappingJackson2HttpMessageConverter(modules.build()));
        }

        @Override
        public void addCorsMappings(CorsRegistry registry) { //모든 controller에 crossorigin 매핑이 적용됨 --> 각각 하는법 : @CrossOrigin
            registry.addMapping("/api/**")
                    .allowedMethods("GET", "POST") //get, post요청만 cors 처리 하겠다
                    .allowedOrigins("*"); //전체 주소 cors 허용
        }
    }



    @Configuration
    @ComponentScan(basePackages = "com.prgrms.kdtspringorder.customer",
            excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = CustomerController.class))
    @EnableTransactionManagement
    static class RootConfig{ //web mvc관련된 설정을 할 필요가 없다. rootApplicationContext에 들어갈 config이기 때문에 web관련 작업은 하지 않고, service, DAO 계층의 작업만 처리함

        @Bean
        public DataSource dataSource(){
            var dataSource = DataSourceBuilder.create()
                    .url("jdbc:mysql://localhost/order_mgmt") //test용
                    .username("root")
                    .password("root1234!")
                    .type(HikariDataSource.class)
                    .build();
            dataSource.setMaximumPoolSize(1000);
            dataSource.setMinimumIdle(100);
            return dataSource;
        }

        @Bean
        public JdbcTemplate jdbcTemplate(DataSource dataSource){
            return new JdbcTemplate(dataSource);
        }

        @Bean
        public NamedParameterJdbcTemplate namedParameterJdbcTemplate(JdbcTemplate jdbcTemplate){
            return new NamedParameterJdbcTemplate(jdbcTemplate);
        }

        @Bean
        public PlatformTransactionManager platformTransactionManager(DataSource dataSource){
            return new DataSourceTransactionManager(dataSource);
        }

    }



    @Override
    public void onStartup(ServletContext servletContext) {
        logger.info("Start Server...");

        //roolApplicationListener 추가
        var rootApplicationContext = new AnnotationConfigWebApplicationContext();
        rootApplicationContext.register(RootConfig.class);//bean설정 전달
        var loaderListener = new ContextLoaderListener(rootApplicationContext);
        servletContext.addListener(loaderListener);

        var applicationContext = new AnnotationConfigWebApplicationContext();

        applicationContext.register(ServletConfig.class);
        var dispatcherServlet = new DispatcherServlet(applicationContext);
        var servletRegistration = servletContext.addServlet("test", dispatcherServlet); //생성한 dispatherServlet을 추가해줘야 함
        servletRegistration.addMapping("/");
        servletRegistration.setLoadOnStartup(-1);
        /* .setLoadOnStartup()
        * default 값이 -1.  -> 로드를 아무것도 안한다?? root application 밖에 없다 --> 이렇게 해야 처음에 로드가 안되다가 api 요청이 들어왔을 때 로드가 된다
        * 0인 경우 test-servlet이라는 이름으로 만들어진 WebApplicationContext가 있음.
        *
        * 이 부분에서 servlet을 여러개를 등록할 수 있다. order영역, voucher영역 등등
        * */

    }
}
