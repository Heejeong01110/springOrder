package com.prgrms.kdtspringorder.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

//@Component
@Configuration
@ConfigurationProperties(prefix = "kdt")
public class OrderProperties implements InitializingBean {
//    @Value("${kdt.version2:v0.0.0}") //property 소스를 못찾으면 기분 값으로 넣어라!!
    private String version;

    //logger level
    private final static Logger logger = LoggerFactory.getLogger(OrderProperties.class);

//    @Value("${kdt.minimum-order-amount}") //알아서 타입변화를 시도하는데 변환이 안될경우 에러 발생
    //그래서 ${}를 사용
    private int minimumOrderAmount;

//    @Value("${kdt.support-vendors}")
    private List<String> supportVendors;
    private String description;

    @Value("${JAVA_HOME}")
    private String javaHome;

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.debug("version -> {}" ,version);
        logger.debug("minimumOrderAmount -> {}" ,minimumOrderAmount);
        logger.debug("supportVendors -> {}" ,supportVendors);
        logger.debug("javaHome -> {}" ,javaHome);
        logger.debug("description -> {}" ,description);
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setMinimumOrderAmount(int minimumOrderAmount) {
        this.minimumOrderAmount = minimumOrderAmount;
    }

    public void setSupportVendors(List<String> supportVendors) {
        this.supportVendors = supportVendors;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public String getVersion() {
        return version;
    }

    public int getMinimumOrderAmount() {
        return minimumOrderAmount;
    }

    public List<String> getSupportVendors() {
        return supportVendors;
    }

    public String getDescription() {
        return description;
    }

    public String getJavaHome() {
        return javaHome;
    }
}
