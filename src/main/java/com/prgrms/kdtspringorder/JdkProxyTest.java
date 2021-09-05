package com.prgrms.kdtspringorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


class CalculatorImpl implements Calculator{ //target class

    @Override
    public int add(int a, int b) {
        return a+b;
    }
}

interface Calculator {
    int add(int a, int b);
}
class LoggingInvocationHandler implements InvocationHandler{
    private static final Logger logger = LoggerFactory.getLogger(LoggingInvocationHandler.class);
    private final Object target;
    public LoggingInvocationHandler(Object target) { //타겟 object를 생성자로 들고 있게 하는 방식
        this.target = target;
    }

    @Override //proxy : 프록시 객체, method : 메서드
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.info("{} executed in {}",method.getName(), target.getClass().getCanonicalName());
        return method.invoke(target,args);
    }
}

/*
proxy 객체 :
일반 implements의 관계인 클래스들을 평범하게 구현하고
Invocationhandler를 implement하는 Handler 객체를 생성한다.
Handler 객체의 invoke()를 오버라이드 해서 여기에다가 실행하고 싶은 메서드를 구현
그 다음에 main에서 해당 프록시 객체를 적용시키고 싶은 구현체 배열들과 handler를 명시하고 마지막 인자에 Handler 인스턴스를 생성해서 넣어줌
 */
public class JdkProxyTest {
    private static final Logger logger = LoggerFactory.getLogger(JdkProxyTest.class);
    public static void main(String[] args) {
        var calculator = new CalculatorImpl();
        Calculator proxyInstance = (Calculator) Proxy.newProxyInstance(//왜 강제 형변환????
                LoggingInvocationHandler.class.getClassLoader(),
                new Class[]{Calculator.class},
                new LoggingInvocationHandler(calculator));
        //인터페이스가 호출될 때마다 핸들러가 호출됨(3번째 인자). 2번째 인자는 인터페이스를 배열로 넘겨야함
        var result = proxyInstance.add(1, 2);
        logger.info("Add -> {}",result);
    }
}
