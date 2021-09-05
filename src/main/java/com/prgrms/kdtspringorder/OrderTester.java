package com.prgrms.kdtspringorder;

import com.prgrms.kdtspringorder.order.OrderItem;
import com.prgrms.kdtspringorder.order.OrderProperties;
import com.prgrms.kdtspringorder.order.OrderService;
import com.prgrms.kdtspringorder.voucher.FixedAmountVoucher;
import com.prgrms.kdtspringorder.voucher.JdbcVoucherRepository;
import com.prgrms.kdtspringorder.voucher.VoucherRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.BeanFactoryAnnotationUtils;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class OrderTester {
    //Logger의 타입이 slf4j.Logger, slf4j.LoggerFactory인지 확인해야 함
    private static final Logger logger = LoggerFactory.getLogger(OrderTester.class); //this.getClass()
    public static void main(String[] args) throws IOException {
        AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS);
//        var applicationContext = new AnnotationConfigApplicationContext(AppConfiguration.class);
//
//        //envirment
////        var environment = applicationContext.getEnvironment();
////        var property = environment.getProperty("kdt.minimum-order-amount");
////        var supportVendors = environment.getProperty("kdt.support-vendors", List.class);
////        var description = environment.getProperty("kdt.description", List.class);
////        System.out.println("property : "+property);
////        System.out.println("supportVendors : "+supportVendors);
////        System.out.println("description : "+description);

        //profile
        var applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(AppConfiguration.class);
        var environment = applicationContext.getEnvironment();
        environment.setActiveProfiles("dev");

        applicationContext.refresh();

        var orderProperties = applicationContext.getBean(OrderProperties.class);
        System.out.println("getVersion : "+orderProperties.getVersion());
        System.out.println("supportVendors : "+orderProperties.getSupportVendors());
        System.out.println("description : "+orderProperties.getDescription());
        System.out.println("getMinimumOrderAmount : "+orderProperties.getMinimumOrderAmount());


        logger.info("@@@@@logger name -> {}", logger.getName());
        logger.info("@@@@@supportVendors : {}",orderProperties.getSupportVendors());
        logger.info("@@@@@description : {}",orderProperties.getDescription());
        logger.info("@@@@@getMinimumOrderAmount : {}",orderProperties.getMinimumOrderAmount());


/*


        var resource = applicationContext.getResource("application.yaml");//classpath상에서 가져오기
        var resource = applicationContext.getResource("classpath:application.yaml");//classpath상에서 가져오기
        var resource = applicationContext.getResource("file:sample.txt");//working directory 기준

        System.out.println(MessageFormat.format("Resource -> {0}",resource.getClass().getCanonicalName())); //ClassPathContextResource가 실제 구현체.
        File file = resource.getFile();
        List<String> strings = Files.readAllLines(file.toPath());
        System.out.println(strings.stream().reduce("",(a,b)-> a+"\n"+b)); //파일에 쓴 그대로 볼 수 있음

        //file 입출력
        var resource = applicationContext.getResource("https://stackoverflow.com/");//url에서 다운로드. 구현체 : UrlResource
        ReadableByteChannel readableByteChannel = Channels.newChannel(resource.getURL().openStream());
        BufferedReader bufferedReader = new BufferedReader(Channels.newReader(readableByteChannel, StandardCharsets.UTF_8));//channel을 reader로 바꿔서 읽는거
        //String collect = bufferedReader.lines().collect(Collectors.joining());
        String contents = bufferedReader.lines().collect(Collectors.joining("\n")); //개행 추가 버전
        System.out.println(contents);
*/

        var customerId = UUID.randomUUID();

        var voucherRepository = applicationContext.getBean(VoucherRepository.class); //변수에서는 구현체를 알수가 없다.
        //여기서는 repository 인터페이스의 어떤 구현체를 선택할지 @Qualifier를 전달할 수 업음.
        // bean의 이름을 다르게 해서 하거나, BeanFactoryAnnotationUils를 이용해서 가져올 수 있음
        //BeanFactoryAnnotationUtils.qualifiedBeansOfType(applicationContext.getBeanFactory(), VoucherRepository.class, "memory");

        var voucher = voucherRepository.insert(new FixedAmountVoucher(UUID.randomUUID(), 10L));

        System.out.println(MessageFormat.format("is Jdbc Repo -> {0}",voucherRepository instanceof JdbcVoucherRepository));
        System.out.println(MessageFormat.format("is Jdbc Repo -> {0}",voucherRepository.getClass().getCanonicalName()));



        var orderService = applicationContext.getBean(OrderService.class);

        var order = orderService.createOrder(customerId,
                new ArrayList<OrderItem>(){{
                    add(new OrderItem(UUID.randomUUID(), 100L,1));
                }},
                voucher.getVoucherId()
        );

        Assert.isTrue(order.totalAmount() == 90L, MessageFormat.format("totalAmount {0} is not 90L ",order.totalAmount()));

        //종료
        applicationContext.close();

        //var orderContext = new AppConfiguration();
/*
        var applicationContext = new AnnotationConfigApplicationContext(AppConfiguration.class);
        var customerId = UUID.randomUUID();
        //var orderService = orderContext.orderService();
        var orderService = applicationContext.getBean(OrderService.class);
        var order = orderService.createOrder(customerId,new ArrayList<OrderItem>(){{
            add(new OrderItem(UUID.randomUUID(), 100L,1));
        }});
        Assert.isTrue(order.totalAmount() == 100L, MessageFormat.format("totalAmount {0} is not 100L ",order.totalAmount()));
*/

        /*
        var orderContext = new AppConfiguration();
        var orderService = orderContext.orderService();
        var order = orderService.createOrder(customerId,new ArrayList<OrderItem>(){{
            add(new OrderItem(UUID.randomUUID(), 100L,1));
        }});
        Assert.isTrue(order.totalAmount() == 100L, MessageFormat.format("totalAmount {0} is not 100L ",order.totalAmount()));
        */
        /*
        var orderItems = new ArrayList<OrderItem>(){{
            add(new OrderItem(UUID.randomUUID(), 100L,1));
        }};
        var fixedAmountVoucher = new FixedAmountVoucher(UUID.randomUUID(), 10L);

        var order = new Order(UUID.randomUUID(), customerId, orderItems, fixedAmountVoucher);
        Assert.isTrue(order.totalAmount() == 90L, MessageFormat.format("totalAmount {0} is not 90L ",order.totalAmount()));
        */
    }
}
