package pl.psnc.vtl.portal.model.users;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Positive;

/**
 * Configuration of user quota.
 */
@Configuration
@Data
@Validated
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "quota")
public class QuotaConfiguration {

    /**
     * Default user space quota.
     */
    @Positive
    private Long space;

    /**
     * Should quota be applied from claims.
     */
    @Value("${apply-from-claims: false}")
    Boolean applyFromClaims;


}
