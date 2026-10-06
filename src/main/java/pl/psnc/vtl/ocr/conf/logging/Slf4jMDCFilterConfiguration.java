package pl.psnc.vtl.ocr.conf.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "config.slf4jfilter")
public class Slf4jMDCFilterConfiguration {

    @Bean
    public FilterRegistrationBean servletRegistrationBean(final Slf4jMDCFilter slf4jMDCFilter) {
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(slf4jMDCFilter);
        registrationBean.setOrder(2);
        return registrationBean;
    }
}
