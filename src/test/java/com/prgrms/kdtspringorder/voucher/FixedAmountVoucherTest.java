package com.prgrms.kdtspringorder.voucher;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FixedAmountVoucherTest {
    private static final Logger logger = LoggerFactory.getLogger(FixedAmountVoucherTest.class);
    @BeforeAll
    static void setUp(){
        logger.info("@BeforeAll - only once");
    }

    @BeforeEach
    void init(){
        logger.info("@BeforeAll - run before each test methond");
    }

    /*
sut : system under test. FixedAmountVoucher에 해당
mut : method under test. testAssertEqual(){}에 해당
메서드는꼭 void형이어야 함.

성공경우만 생각하면 안되고 예외경우를 테스트코드로 적어봐야 함
*/
    @Test
    @DisplayName("테스트 메서드 이름 지정 🎁")
    void testAssertEqual() {
        //(예상값, 테스트할 값)
        assertEquals(2,1+1);

    }

    @Test
    @DisplayName("주어진 금액만큼 할인을 해야 한다.")
    void testDiscount() {
        var sut = new FixedAmountVoucher(UUID.randomUUID(), 100);
        assertEquals(900,sut.discount(1000));
    }

    @Test
    @DisplayName("할인 금액은 마이너스가 될 수 없다.")
//    @Disabled //테스트 코드를 스킵함
    void testWithMinus() {
        assertThrows(IllegalAccessError.class, () ->
                new FixedAmountVoucher(UUID.randomUUID(), -100));
    }

    @Test
    @DisplayName("할인된 금액은 마이너스가 될 수 없다.")
    void testMinusDiscountedAmount() {
        var sut = new FixedAmountVoucher(UUID.randomUUID(), 1000);
        assertEquals(0,sut.discount(900));
    }

    @Test
    @DisplayName("유효한 할인 금액으로만 생성할 수 있다..")
    void testVoucherCreation() {
        assertAll("FixedAmountVoucher creation",
                () -> assertThrows(IllegalAccessError.class, () -> new FixedAmountVoucher(UUID.randomUUID(), 0)),
                () -> assertThrows(IllegalAccessError.class, () -> new FixedAmountVoucher(UUID.randomUUID(), -100)),
                () -> assertThrows(IllegalAccessError.class, () -> new FixedAmountVoucher(UUID.randomUUID(), 10000000))
                );
    }
}