package ee.taltech.inbankbackend.constant;

import ee.taltech.inbankbackend.dto.Country;
import ee.taltech.inbankbackend.dto.DecisionRequest;
import ee.taltech.inbankbackend.dto.DecisionResponse;
import ee.taltech.inbankbackend.util.ErrorMessage;

public class TestConstant {
    public static final String DEBTOR_PERSONAL_CODE = "37605030299";
    public static final String SEGMENT_1_PERSONAL_CODE = "50307172740";
    public static final String SEGMENT_2_PERSONAL_CODE = "38411266610";
    public static final String SEGMENT_3_PERSONAL_CODE = "35006069515";
    public static final String INVALID_PERSONAL_CODE = "12345678901";
    public static final String PERSONAL_CODE_AGE_LESS_THEN_MINIMUM_AGE = "51001015876";
    public static final String PERSONAL_CODE_AGE_LARGER_THEN_MINIMUM_AGE = "34001014839";
    public static final long VALID_LOAN_AMOUNT = 4000;
    public static final long VALID_LOAN_AMOUNT_2 = 2000;
    public static final long INVALID_LOAN_AMOUNT = 1200;
    public static final int VALID_LOAN_PERIOD = 12;
    public static final int VALID_LOAN_PERIOD_2 = 24;
    public static final int INVALID_LOAN_PERIOD = 6;
    public static final Country COUNTRY = Country.ESTONIA;
    public static final Country COUNTRY_2 = Country.LATVIA;
    public static final Country INVALID_COUNTRY = null;
    public static final DecisionRequest DEBTOR_REQUEST = new DecisionRequest(DEBTOR_PERSONAL_CODE, VALID_LOAN_AMOUNT, VALID_LOAN_PERIOD, COUNTRY);
    public static final DecisionRequest SEGMENT_1_REQUEST_INVALID = new DecisionRequest(SEGMENT_1_PERSONAL_CODE, VALID_LOAN_AMOUNT, VALID_LOAN_PERIOD, COUNTRY);
    public static final DecisionRequest SEGMENT_1_REQUEST = new DecisionRequest(SEGMENT_1_PERSONAL_CODE, VALID_LOAN_AMOUNT_2, VALID_LOAN_PERIOD_2, COUNTRY);
    public static final DecisionRequest SEGMENT_2_REQUEST = new DecisionRequest(SEGMENT_2_PERSONAL_CODE, VALID_LOAN_AMOUNT_2, VALID_LOAN_PERIOD_2, COUNTRY);
    public static final DecisionRequest SEGMENT_3_REQUEST = new DecisionRequest(SEGMENT_3_PERSONAL_CODE, VALID_LOAN_AMOUNT_2, VALID_LOAN_PERIOD_2, COUNTRY);
    public static final DecisionRequest INVALID_PERSONAL_CODE_REQUEST = new DecisionRequest(INVALID_PERSONAL_CODE, VALID_LOAN_AMOUNT, VALID_LOAN_PERIOD, COUNTRY);
    public static final DecisionRequest INVALID_LOAN_AMOUNT_REQUEST = new DecisionRequest(SEGMENT_1_PERSONAL_CODE, INVALID_LOAN_AMOUNT, VALID_LOAN_PERIOD, COUNTRY);
    public static final DecisionRequest INVALID_LOAN_PERIOD_REQUEST = new DecisionRequest(SEGMENT_1_PERSONAL_CODE, VALID_LOAN_AMOUNT, INVALID_LOAN_PERIOD, COUNTRY);
    public static final DecisionRequest INVALID_COUNTRY_NAME_REQUEST = new DecisionRequest(SEGMENT_1_PERSONAL_CODE, VALID_LOAN_AMOUNT, VALID_LOAN_PERIOD, INVALID_COUNTRY);
    public static final DecisionRequest INVALID_MINIMUM_AGE_FOR_ESTONIA_REQUEST = new DecisionRequest(PERSONAL_CODE_AGE_LESS_THEN_MINIMUM_AGE, VALID_LOAN_AMOUNT, VALID_LOAN_PERIOD, COUNTRY);
    public static final DecisionRequest INVALID_MAXIMUM_AGE_FOR_ESTONIA_REQUEST = new DecisionRequest(PERSONAL_CODE_AGE_LARGER_THEN_MINIMUM_AGE, VALID_LOAN_AMOUNT, VALID_LOAN_PERIOD, COUNTRY);
    public static final DecisionRequest INVALID_MAXIMUM_AGE_FOR_ESTONIA_BUT_VALID_FOR_LATVIA_REQUEST = new DecisionRequest(PERSONAL_CODE_AGE_LARGER_THEN_MINIMUM_AGE, VALID_LOAN_AMOUNT_2, VALID_LOAN_PERIOD_2, COUNTRY_2);
    public static final DecisionResponse INVALID_DEBTOR_RESPONSE = new DecisionResponse(null, null, ErrorMessage.NO_VALID_LOAN_FOUND.getMessage());

}
