package ee.taltech.inbankbackend.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Holds all necessary constants for the age validation.
 */
@ConfigurationProperties(prefix = "application.age-validation")
@Component
@Data
public class AgeValidationConstants {
    private int maximumAgeEs;
    private int minimumAgeEs;
    private int maximumAgeLv;
    private int minimumAgeLv;
    private int maximumAgeLt;
    private int minimumAgeLt;

}
