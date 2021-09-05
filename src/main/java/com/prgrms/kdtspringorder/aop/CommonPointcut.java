package com.prgrms.kdtspringorder.aop;

import org.aspectj.lang.annotation.Pointcut;

public class CommonPointcut {

    @Pointcut("execution(public * com.prgrms.kdtspringorder..*.*Service.*(..))")
    public void servicePublicMethodPointcut(){};

    @Pointcut("execution(* com.prgrms.kdtspringorder..*.*Repository.*(..))")
    public void repositoryMethodPointcut(){};

    @Pointcut("execution(* com.prgrms.kdtspringorder..*.*Repository.insert(..))")
    public void repositoryInsertMethodPointcut(){};
}
