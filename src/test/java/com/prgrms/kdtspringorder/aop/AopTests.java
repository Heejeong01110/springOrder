package com.prgrms.kdtspringorder.aop;

import com.prgrms.kdtspringorder.order.OrderItem;
import com.prgrms.kdtspringorder.order.OrderService;
import com.prgrms.kdtspringorder.order.OrderStatus;
import com.prgrms.kdtspringorder.voucher.FixedAmountVoucher;
import com.prgrms.kdtspringorder.voucher.VoucherRepository;
import com.prgrms.kdtspringorder.voucher.VoucherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringJUnitConfig //내부에서 ExtendWith, ContextConfiguration을 사용하고 있음. 그래서 이거 하나만 써주면 됨
@ActiveProfiles("test") //프로파일이 적용된 컴포넌트들만 스캔됨
public class AopTests {
    @Configuration
    @ComponentScan(basePackages = {"com.prgrms.kdtspringorder.voucher", "com.prgrms.kdtspringorder.aop"})

    @EnableAspectJAutoProxy //프록시 애너테이션
    static class Config {
    }
    @Autowired
    ApplicationContext context;

    @Autowired
    VoucherRepository voucherRepository;

//    @Autowired
//    VoucherService voucherService;

    @Test
    @DisplayName("Aop test")
    public void testOrderService() {
        //given
        var fixedAmountVoucher = new FixedAmountVoucher(UUID.randomUUID(), 100);
        voucherRepository.insert(fixedAmountVoucher);

//        VoucherService voucherService = new VoucherService(voucherRepository);
//        voucherService.getVoucher(fixedAmountVoucher.getVoucherId()); //bean으로 등록하지 않은 메서드라면 aop 실행이 안됨.
//        //스프링의 aop는 등록된 빈 객체들에게만 proxy 객체가 만들어져서 적용됨.



    }
}
