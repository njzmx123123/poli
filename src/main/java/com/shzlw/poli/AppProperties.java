package com.shzlw.poli;

import com.zhizhi.common.utils.AESUtil;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "poli")
public class AppProperties {

    Integer datasourceMaximumPoolSize;

    Integer maximumQueryRecords;

    String localeLanguage;

    Boolean allowMultipleQueryStatements;

    String exportServerUrl;

    /**
     * uc url
     */
    String ssoUrl;

    /**
     * uc client code
     */
    String ssoClientCode;

    /**
     * uc secret key
     */
    String ssoClientSecret;

    public AppProperties() {
        datasourceMaximumPoolSize = 50;
        maximumQueryRecords = 1000;
        localeLanguage = "en";
        allowMultipleQueryStatements = false;
    }
}