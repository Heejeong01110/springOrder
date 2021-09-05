package com.prgrms.kdtspringorder;

import com.prgrms.kdtspringorder.order.OrderProperties;
import com.prgrms.kdtspringorder.voucher.FixedAmountVoucher;
import com.prgrms.kdtspringorder.voucher.JdbcVoucherRepository;
import com.prgrms.kdtspringorder.voucher.VoucherRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.text.MessageFormat;
import java.util.UUID;

@SpringBootApplication //이건 별다른 설정 없이 사용 가능하다.(자동으로 yaml, configurationproperty 등)
@ComponentScan(basePackages = {"com.prgrms.kdtspringorder.order", "com.prgrms.kdtspringorder.voucher", "com.prgrms.kdtspringorder.configuration"})
public class KdtSpringOrderApplication {

    public static void main(String[] args) {
        // run configuration 에서 springboot profile 설정하기
        //  Program arguments : --spring.profiles.active=dev
        //프로퍼티 파일을 나눠서 저장했을 경우에도 @Profile({"loacl","default"}) 애너테이션 설정 후 윗줄 입력시 윗줄로 실행됨

        //이렇게 프로퍼티를 지정해주는건 스프링부트에서만 지원해주는 기능임
        SpringApplication springApplication = new SpringApplication(KdtSpringOrderApplication.class);
//        springApplication.setAdditionalProfiles("local"); //profile 입력
        ConfigurableApplicationContext applicationContext = springApplication.run(args);
//        ConfigurableApplicationContext applicationContext = SpringApplication.run(KdtSpringOrderApplication.class, args);




        var orderProperties = applicationContext.getBean(OrderProperties.class);
        System.out.println("getVersion : "+orderProperties.getVersion());
        System.out.println("supportVendors : "+orderProperties.getSupportVendors());
        System.out.println("description : "+orderProperties.getDescription());
        System.out.println("getMinimumOrderAmount : "+orderProperties.getMinimumOrderAmount());

        var customerId = UUID.randomUUID();
        var voucherRepository = applicationContext.getBean(VoucherRepository.class); //변수에서는 구현체를 알수가 없다.
        var voucher = voucherRepository.insert(new FixedAmountVoucher(UUID.randomUUID(), 10L));
        System.out.println(MessageFormat.format("is Jdbc Repo -> {0}",voucherRepository instanceof JdbcVoucherRepository));
        System.out.println(MessageFormat.format("is Jdbc Repo -> {0}",voucherRepository.getClass().getCanonicalName()));
    }

}
