package com.prgrms.kdtspringorder.voucher;

import java.util.UUID;

public class FixedAmountVoucher implements Voucher {
    private static final long MAX_VOUCHER_AMOUNT = 10000;
    private final UUID voucherId;
    private final long amount;

    public FixedAmountVoucher(UUID voucherId, long amount) {
        if(amount <0) throw new IllegalAccessError("Amount should be positive");
        if(amount == 0) throw new IllegalAccessError("Amount should be positive");
        if(amount >MAX_VOUCHER_AMOUNT) throw new IllegalAccessError("Amount should be less then %d".formatted(MAX_VOUCHER_AMOUNT));
        this.voucherId = voucherId;
        this.amount = amount;
    }

    @Override
    public UUID getVoucherId() {
        return voucherId;
    }

    public long discount(long beforeDiscount){
        var discountedAmount = beforeDiscount - amount;
        return discountedAmount<0?0:discountedAmount;
    }

    @Override
    public String toString() {
        return "FixedAmountVoucher{" +
                "voucherId=" + voucherId +
                ", amount=" + amount +
                '}';
    }
}
