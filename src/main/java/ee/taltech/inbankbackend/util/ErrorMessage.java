package ee.taltech.inbankbackend.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Holds all error messages and error codes of application.
 * Current version of app only support one language.
 */
@AllArgsConstructor
@Getter
public enum ErrorMessage {
    INVALID_PERSONAL_ID_CODE("E1001", "Invalid personal ID code!"),
    INVALID_LOAN_AMOUNT("E1002", "Invalid loan amount!"),
    INVALID_LOAN_PERIOD("E1003", "Invalid loan period!"),
    NO_VALID_LOAN_FOUND("E1004", "No valid loan found!"),
    INVALID_AGE_RANGE("E1005", "Unfortunately, we cannot offer a loan based on our age policy"),
    INVALID_COUNTRY_NAME("E1006", "Invalid country!"),
    INVALID_REQUEST("E1007", "Invalid request!"),
    UNEXPECTED_ERROR("E1008", "An unexpected error occurred"),;

    private final String code;
    private final String message;

}
