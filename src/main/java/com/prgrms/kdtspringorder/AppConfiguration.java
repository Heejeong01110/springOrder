package com.prgrms.kdtspringorder;

import com.prgrms.kdtspringorder.configuration.YamlPropertiesFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

@Configuration
//scan의 대상을 조절할 수 있음. 대상들의 패키지를 배열 형식으로 입력
@ComponentScan(basePackages = {"com.prgrms.kdtspringorder.order", "com.prgrms.kdtspringorder.voucher", "com.prgrms.kdtspringorder.configuration"})
//@ComponentScan(basePackageClasses = {Order.class, Voucher.class})
//@ComponentScan(basePackages = {"com.prgrms.kdtspringorder.order com.prgrms.kdtspringorder.voucher"})
//@ComponentScan(excludeFilters = {@ComponentScan.Filter(type = FilterType.CUSTOM)}) //사용자가 직접 만든 애너테이션 명을 이용.
/*@ComponentScan(basePackages = {"com.prgrms.kdtspringorder.order", "com.prgrms.kdtspringorder.voucher"},
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = MemoryVoucherRepository.class)}) */
//basepackage 안의 특정 패키지를 제외하는 필터. includeFilters도 가능
//3번째 방식은 ide의 도움을 받지 못해서 좋지 않음. 2번째가 주로 쓰임
//@PropertySource("application.yaml") //원래 지원을 안함. propertyFactory를 구현해야함 스프링부트는 지원
@PropertySource(value = "application.yaml",factory = YamlPropertiesFactory.class)
@EnableConfigurationProperties //스프링 프레임워크에서는 지원해주지 않는 기능이기 때문에 이렇게 명시해줘야함
public class AppConfiguration {

//관계에 대한 책임을 가짐

    //생명주기 설정
    @Bean(initMethod = "init")
    public BeanOne beanOne(){
        return new BeanOne();
    }


//    @Bean
//    public VoucherRepository voucherRepository(){
//        return new VoucherRepository() {
//            @Override
//            public Optional<Voucher> findById(UUID voucherId) {
//                return Optional.empty();
//            }
//
//            @Override
//            public Voucher insert(Voucher voucher) {
//                return null;
//            }
//        };
//    }
//
//    @Bean
//    public OrderRepository orderRepository(){
//        return new OrderRepository(){
//            @Override
//            public void insert(Order order) {
//
//            }
//        };
//    }

//    @Bean
//    public VoucherService voucherService(VoucherRepository voucherRepository){ //원래는 return에서 바로 객체를 생성했지만 매개변수 값을 추가할 수도 있음
//        //bean을 정의한 메서드에 매개변수가 있으면 스프링이 알아서 bean 객체를 전달해줌
//        return new VoucherService(voucherRepository);
//    }
//
//    @Bean
//    public OrderService orderService(VoucherService voucherService, OrderRepository orderRepository){
//        return new OrderService(voucherService, orderRepository);
//    }
}

class BeanOne implements InitializingBean {

    private void init() {
        System.out.println("BeanOne init called!");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("BeanOne afterProportiesSet called!");
    }
}
