package ee.taltech.inbankbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Holds the request data of the REST endpoint.
 */
@Getter
@AllArgsConstructor
@ToString
public class DecisionRequest {
    private String personalCode;
    private Long loanAmount;
    private int loanPeriod;
    private Country country;
}