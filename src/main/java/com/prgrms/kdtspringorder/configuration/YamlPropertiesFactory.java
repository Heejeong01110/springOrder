package com.prgrms.kdtspringorder.configuration;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.io.IOException;

//스프링 프레임워크에서 yaml을 쓰기위해서 꼭 팩토리를 선언해줘야함
public class YamlPropertiesFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(String s, EncodedResource encodedResource) throws IOException {
        var yamlProperiesFactoryBean = new YamlPropertiesFactoryBean();
        yamlProperiesFactoryBean.setResources(encodedResource.getResource());

        var properties = yamlProperiesFactoryBean.getObject();
        return new PropertiesPropertySource(encodedResource.getResource().getFilename(), properties);
    }
}
