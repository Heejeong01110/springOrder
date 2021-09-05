package com.prgrms.kdtspringorder.voucher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.UUID;

@Service
public class VoucherService {



    //이렇게 필드에다가 autowired를 줄 수도 있고 setter를 통해 어노테이션을 줄 수도 있음. 생성자를 통해서도 할 수 있음
    //@Autowired //Autowired는 final 사용 불가
    private final VoucherRepository voucherRepository;

    //위에서 말한 setter를 통해 의존관계 주입을 하는 법
//    @Autowired
//    public void setVoucherRepository(VoucherRepository voucherRepository) {
//        this.voucherRepository = voucherRepository;
//    }

//    @Autowired //이걸 안붙혀도 자동으로 주입이 되게끔 기능이 추가됨.
    public VoucherService(/*@Qualifier("Memory")*/ VoucherRepository voucherRepository) {
        this.voucherRepository = voucherRepository;
    }

    public Voucher getVoucher(UUID voucherId) {
        return voucherRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException(
                        MessageFormat.format("Can not find a voucher for {0}", voucherId)
                ));
    }

    public void useVoucher(Voucher voucher) {
    }
}
