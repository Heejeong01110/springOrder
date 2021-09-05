package com.prgrms.kdtspringorder.aop;

import com.prgrms.kdtspringorder.JdbcCustomerRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect //spring 애너테이션이 아님
@Component //스프링에서 사용하기 위해 scan의 대상이 되게 하기위해서
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(public * com.prgrms.kdtspringorder..*.*Service.*(..))") //pointcut들을 미리 모듈화 시킬 수 있음 --> CommonPointcut
   public void servicePublicMethodPointcut(){};//무조건 void 형이어야 함함

//    @Around("execution(public * com.prgrms.kdtspringorder..*Repository.)") //kdt 아래의 모든 *Repository에 적용
//@Around("execution(public * com.prgrms.kdtspringorder..*.*(..))") //전체 클래스의 전체 메서드. 인자 상관없이
//@Around("execution(private * com.prgrms.kdtspringorder..*.*(..))") //스프링 aop는 interface 기반이기 때문에 public 밖에 안됨
//    @Around("execution(public * com.prgrms.kdtspringorder..*.*Service(..))") //전체 클래스의 서비스 메서드. 인자 상관없이
//@within() : 논리연산자를 사용해서 쓸 수 있음. 특정 함수를 지정 가능

//    @Around("servicePublicMethodPointcut()")
//    @Around("com.prgrms.kdtspringorder.aop.CommonPointcut.repositoryInsertMethodPointcut()")

    @Around("@annotation(com.prgrms.kdtspringorder.aop.TrackTime)")//특정 애너테이션이 부여된 메서드에만 쓰겠다고 지정할 수 있음. 풀네임을 넣어줘야됨
    public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("Before method called. {}", joinPoint.getSignature().toString());
        var startTime = System.nanoTime();// 1->100,000,000 ns
        var result = joinPoint.proceed();
        var emdTime = System.nanoTime()-startTime;// 1->100,000,000 ns
        logger.info("After method called with result -> {} and time taken {} ns",result,emdTime);
        return result;

    }
}
