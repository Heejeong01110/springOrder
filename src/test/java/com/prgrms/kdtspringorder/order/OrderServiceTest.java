package com.prgrms.kdtspringorder.order;

import com.prgrms.kdtspringorder.voucher.FixedAmountVoucher;
import com.prgrms.kdtspringorder.voucher.MemoryVoucherRepository;
import com.prgrms.kdtspringorder.voucher.VoucherRepository;
import com.prgrms.kdtspringorder.voucher.VoucherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    class OrderRepositoryStub implements OrderRepository{

        @Override
        public Order insert(Order order) {
            return null;
        }
    }

    @Test
    @DisplayName("오더가 생성되어야 한다. (stub)")
    public void createOrder() throws Exception {
        //given
        var voucherRepository = new MemoryVoucherRepository();
        var fixedAmountVoucher = new FixedAmountVoucher(UUID.randomUUID(), 100);
        voucherRepository.insert(fixedAmountVoucher);

        var sut = new OrderService(new VoucherService(voucherRepository), new OrderRepositoryStub());

        //when
        var order = sut.createOrder(UUID.randomUUID(), List.of(new OrderItem(UUID.randomUUID(), 200L, 1)), fixedAmountVoucher.getVoucherId());

        //then
        assertThat(order.totalAmount(),is(100L));
        assertThat(order.getVoucher().isEmpty(),is(false));
        assertThat(order.getVoucher().get().getVoucherId(),is(fixedAmountVoucher.getVoucherId()));
        assertThat(order.getOrderStatus(),is(OrderStatus.ACCEPTED));
    }

    @Test
    @DisplayName("오더가 생성되어야 한다. (mock)")
    public void createOrderByMock()  {
        //given
        var voucherServiceMock = mock(VoucherService.class);
        var orderRepositoryMock = mock(OrderRepository.class);
        var fixedAmountVoucher = new FixedAmountVoucher(UUID.randomUUID(), 100);
        //정의해 준 함수만 동작하고 다른 함수를 호출한 경우 오루가 남
        when(voucherServiceMock.getVoucher(fixedAmountVoucher.getVoucherId())).thenReturn(fixedAmountVoucher);

        var sut = new OrderService(voucherServiceMock, orderRepositoryMock);

        //when
        Order order = sut.createOrder(UUID.randomUUID(),
                List.of(new OrderItem(UUID.randomUUID(), 200L, 1)),
                fixedAmountVoucher.getVoucherId());


        //then
        assertThat(order.totalAmount(), is(100L));
        assertThat(order.getVoucher().isEmpty(), is(false));

        verify(voucherServiceMock).getVoucher(fixedAmountVoucher.getVoucherId()); //내부에서 이 메서드가 호출이 됐는지 확인
        verify(orderRepositoryMock).insert(order);
        verify(voucherServiceMock).useVoucher(fixedAmountVoucher);

        //특정한 순서에 따라 호출되는지 확인하고 싶을 때. 매개변수로 여러가지 목을 넣을 수 있음
        InOrder inOrder = inOrder(voucherServiceMock, orderRepositoryMock);
        inOrder.verify(voucherServiceMock).getVoucher(fixedAmountVoucher.getVoucherId());
        inOrder.verify(orderRepositoryMock).insert(order);
        inOrder.verify(voucherServiceMock).useVoucher(fixedAmountVoucher);
    }
}