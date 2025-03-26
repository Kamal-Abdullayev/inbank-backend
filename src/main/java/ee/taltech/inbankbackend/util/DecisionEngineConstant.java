package ee.taltech.inbankbackend.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Holds all necessary constants for the decision engine.
 */
@ConfigurationProperties(prefix = "application.decision-engine")
@Component
@Data
public class DecisionEngineConstant {
    private int minimumLoanAmount;
    private int maximumLoanAmount;
    private int minimumLoanPeriod;
    private int maximumLoanPeriod;
    private int segment1CreditModifier;
    private int segment2CreditModifier;
    private int segment3CreditModifier;
    private int loanInterval;
}
