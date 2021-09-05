package com.prgrms.kdtspringorder;

import com.prgrms.kdtspringorder.order.OrderItem;
import com.prgrms.kdtspringorder.order.OrderService;
import com.prgrms.kdtspringorder.order.OrderStatus;
import com.prgrms.kdtspringorder.voucher.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

//@ExtendWith(SpringExtension.class) //junit과 실질적으로 상호작용해서 testcontext가 만들어지게 하는거
//@ContextConfiguration(classes = {AppConfiguration.class}) //어떤식으로 applicationContext가 만들어 져야 하는지만 알려줌.
                        // 클래스를 인자로 주면 클래스를 로드해서 context를 만들어줌
                        //프로파일이나 별도의 설정이 필요한 경우가 있기 때문에 test용 applicationConfiguration을 생성해서 전달해줄 수도 있다.
                        //이 안에서 static 클래스로 정의할 수도 있고, test 폴더 안에 만들 수도 있음.
//@ContextConfiguration //configuration을 별도로 전달하지 않으면 기본적으로 클래스 안에있는 @Configuration static 클래스를 찾음
@SpringJUnitConfig //내부에서 ExtendWith, ContextConfiguration을 사용하고 있음. 그래서 이거 하나만 써주면 됨
@ActiveProfiles("test") //프로파일이 적용된 컴포넌트들만 스캔됨
public class KdtSpringContextTests {
    @Configuration
    @ComponentScan(basePackages = {"com.prgrms.kdtspringorder.order", "com.prgrms.kdtspringorder.voucher"}) //여기서도 가능!
    static class Config {
        /*@Bean
        VoucherRepository voucherRepository(){
            return new VoucherRepository(){

                @Override
                public Optional<Voucher> findById(UUID voucherId) {
                    return Optional.empty();
                }

                @Override
                public Voucher insert(Voucher voucher) {
                    return null;
                }
            };
        }*/
    }
    @Autowired
    ApplicationContext context;

    @Autowired
    OrderService orderService;

    @Autowired
    VoucherRepository voucherRepository;

    @Test
    @DisplayName("applicationContext가 생성되어야 한다.")
    public void testApplicationContext() {
        //given
        assertThat(context,notNullValue());
    }

    @Test
    @DisplayName("VoucherRepository가 빈으로 등록되어 있어야 한다.")
    public void testVoucherRepositoryCreation() throws Exception {
        var bean = context.getBean(VoucherRepository.class);
        assertThat(bean,notNullValue());
    }

    @Test
    @DisplayName("OrderService를 사용해서 주문을 생성할 수 있다.")
    public void testOrderService() throws Exception {
        //given
        var fixedAmountVoucher = new FixedAmountVoucher(UUID.randomUUID(), 100);
        voucherRepository.insert(fixedAmountVoucher);

        //when
        var order = orderService.createOrder(UUID.randomUUID(), List.of(new OrderItem(UUID.randomUUID(), 200L, 1)), fixedAmountVoucher.getVoucherId());

        //then
        assertThat(order.totalAmount(),is(100L));
        assertThat(order.getVoucher().isEmpty(),is(false));
        assertThat(order.getVoucher().get().getVoucherId(),is(fixedAmountVoucher.getVoucherId()));
        assertThat(order.getOrderStatus(),is(OrderStatus.ACCEPTED));
    }
}
