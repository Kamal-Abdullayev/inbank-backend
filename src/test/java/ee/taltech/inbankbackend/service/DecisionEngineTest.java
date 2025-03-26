package ee.taltech.inbankbackend.service;

import ee.taltech.inbankbackend.dto.DecisionResponse;
import ee.taltech.inbankbackend.exception.*;
import ee.taltech.inbankbackend.util.AgeValidationConstants;
import ee.taltech.inbankbackend.util.DecisionEngineConstant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static ee.taltech.inbankbackend.constant.TestConstant.*;

@ExtendWith(MockitoExtension.class)
class DecisionEngineTest {


    @InjectMocks
    private DecisionEngineService decisionEngine;

    @Mock
    private DecisionEngineConstant decisionEngineConstant;

    @Mock
    private AgeValidationConstants ageValidationConstants;


    @BeforeEach
    void setUp() {
        Mockito.lenient().when(decisionEngineConstant.getMinimumLoanAmount()).thenReturn(2000);
        Mockito.lenient().when(decisionEngineConstant.getMaximumLoanAmount()).thenReturn(10000);
        Mockito.lenient().when(decisionEngineConstant.getMinimumLoanPeriod()).thenReturn(12);
        Mockito.lenient().when(decisionEngineConstant.getMaximumLoanPeriod()).thenReturn(48);
        Mockito.lenient().when(decisionEngineConstant.getSegment1CreditModifier()).thenReturn(100);
        Mockito.lenient().when(decisionEngineConstant.getSegment2CreditModifier()).thenReturn(300);
        Mockito.lenient().when(decisionEngineConstant.getSegment3CreditModifier()).thenReturn(1000);
        Mockito.lenient().when(decisionEngineConstant.getLoanInterval()).thenReturn(6);
        Mockito.lenient().when(ageValidationConstants.getMaximumAgeEs()).thenReturn(78);
        Mockito.lenient().when(ageValidationConstants.getMinimumAgeEs()).thenReturn(18);
        Mockito.lenient().when(ageValidationConstants.getMaximumAgeLv()).thenReturn(95);
        Mockito.lenient().when(ageValidationConstants.getMinimumAgeLv()).thenReturn(18);
    }


    @Test
    void testDebtorPersonalCode_whenRequestValid() {
        DecisionResponse response = decisionEngine.calculateApprovedLoan(DEBTOR_REQUEST);
        assertEquals(INVALID_DEBTOR_RESPONSE.getErrorMessage(), response.getErrorMessage());
    }

    @Test
    void testSegment1PersonalCode_whenRequestedLoanAmountExceedTheUsersLimit() {
        DecisionResponse decision = decisionEngine.calculateApprovedLoan(SEGMENT_1_REQUEST_INVALID);
        assertNull(decision.getLoanAmount());
        assertEquals(42, decision.getLoanPeriod());
    }

    @Test
    void testSegment1PersonalCode_whenRequestedValid() {
        DecisionResponse decision = decisionEngine.calculateApprovedLoan(SEGMENT_1_REQUEST);
        assertEquals(2400, decision.getLoanAmount());
        assertEquals(24, decision.getLoanPeriod());
    }

    @Test
    void testSegment2PersonalCode_whenRequestedValid() {
        DecisionResponse decision = decisionEngine.calculateApprovedLoan(SEGMENT_2_REQUEST);
        assertEquals(7200, decision.getLoanAmount());
        assertEquals(24, decision.getLoanPeriod());
    }

    @Test
    void testSegment3PersonalCode_whenRequestedValid() {
        DecisionResponse decision = decisionEngine.calculateApprovedLoan(SEGMENT_3_REQUEST);
        assertEquals(10000, decision.getLoanAmount());
        assertEquals(24, decision.getLoanPeriod());
    }

    @Test
    void testCalculateApprovedLoan_whenPersonalCodeNotValid() {
        assertThrows(InvalidPersonalCodeException.class,
                () -> decisionEngine.calculateApprovedLoan(INVALID_PERSONAL_CODE_REQUEST));
    }

    @Test
    void testCalculateApprovedLoan_whenLoanAmountIsLessThanMinimumLoanAmount() {
        assertThrows(InvalidLoanAmountException.class,
                () -> decisionEngine.calculateApprovedLoan(INVALID_LOAN_AMOUNT_REQUEST));
    }

    @Test
    void testCalculateApprovedLoan_whenLoanPeriodIsLessThanMinimumLoanPeriod() {
        assertThrows(InvalidLoanPeriodException.class,
                () -> decisionEngine.calculateApprovedLoan(INVALID_LOAN_PERIOD_REQUEST));
    }

    @Test
    void testCalculateApprovedLoan_whenCountryNameIsNull() {
        assertThrows(InvalidCountryException.class,
                () -> decisionEngine.calculateApprovedLoan(INVALID_COUNTRY_NAME_REQUEST));
    }

    @Test
    void testCalculateApprovedLoan_whenAgeIsLessThanCountrySpecificMinimumAgeAndCountryIsEstonia() {
        assertThrows(AgeConstraintException.class,
                () -> decisionEngine.calculateApprovedLoan(INVALID_MINIMUM_AGE_FOR_ESTONIA_REQUEST));
    }

    @Test
    void testCalculateApprovedLoan_whenAgeIsMoreThanCountrySpecificMaximumAgeAndCountryIsEstonia() {
        assertThrows(AgeConstraintException.class,
                () -> decisionEngine.calculateApprovedLoan(INVALID_MAXIMUM_AGE_FOR_ESTONIA_REQUEST));
    }

    @Test
    void testCalculateApprovedLoan_whenAgeIsMoreThanCountryEstoniaMaximumAgeButLessThenLatvianAndCountryIsEstonia() {
        DecisionResponse decision = decisionEngine.calculateApprovedLoan(INVALID_MAXIMUM_AGE_FOR_ESTONIA_BUT_VALID_FOR_LATVIA_REQUEST);
        assertEquals(2400, decision.getLoanAmount());
        assertEquals(24, decision.getLoanPeriod());
    }

}

