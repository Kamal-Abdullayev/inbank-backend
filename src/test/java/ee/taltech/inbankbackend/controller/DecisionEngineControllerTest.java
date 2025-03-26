package ee.taltech.inbankbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.inbankbackend.dto.Country;
import ee.taltech.inbankbackend.dto.DecisionRequest;
import ee.taltech.inbankbackend.dto.DecisionResponse;
import ee.taltech.inbankbackend.exception.*;
import ee.taltech.inbankbackend.service.DecisionEngineService;
import ee.taltech.inbankbackend.util.AgeValidationConstants;
import ee.taltech.inbankbackend.util.DecisionEngineConstant;
import ee.taltech.inbankbackend.util.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ee.taltech.inbankbackend.constant.TestConstant.*;

/**
 * This class holds integration tests for the DecisionEngineController endpoint.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
public class DecisionEngineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DecisionEngineService decisionEngine;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DecisionEngineConstant decisionEngineConstant;

    @MockBean
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
    /**
     * This method tests the /loan/decision endpoint with valid inputs.
     */
    @Test
    public void givenValidRequest_whenRequestDecision_thenReturnsExpectedResponse() throws Exception {
        DecisionResponse decision = new DecisionResponse(7200, 24, null);
        when(decisionEngine.calculateApprovedLoan(Mockito.any(DecisionRequest.class))).thenReturn(decision);

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(SEGMENT_2_REQUEST))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").value(7200))
                .andExpect(jsonPath("$.loanPeriod").value(24))
                .andExpect(jsonPath("$.errorMessage").isEmpty())
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.getLoanAmount() == 7200;
        assert response.getLoanPeriod() == 24;
        assert response.getErrorMessage() == null;
    }

    /**
     * This test ensures that if an invalid personal code is provided, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenInvalidPersonalCode_whenRequestDecision_thenReturnsBadRequest() throws Exception{
        when(decisionEngine.calculateApprovedLoan(Mockito.any(DecisionRequest.class)))
                .thenThrow(new InvalidPersonalCodeException(ErrorMessage.INVALID_PERSONAL_ID_CODE.getMessage()));

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(INVALID_PERSONAL_CODE_REQUEST))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(ErrorMessage.INVALID_PERSONAL_ID_CODE.getMessage()))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.getLoanAmount() == null;
        assert response.getLoanPeriod() == null;
        assert response.getErrorMessage().equals(ErrorMessage.INVALID_PERSONAL_ID_CODE.getMessage());
    }

    /**
     * This test ensures that if an invalid loan amount is provided, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenInvalidLoanAmount_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan(Mockito.any(DecisionRequest.class)))
                .thenThrow(new InvalidLoanAmountException(ErrorMessage.INVALID_LOAN_AMOUNT.getMessage()));

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(INVALID_LOAN_AMOUNT_REQUEST))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(ErrorMessage.INVALID_LOAN_AMOUNT.getMessage()))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.getLoanAmount() == null;
        assert response.getLoanPeriod() == null;
        assert response.getErrorMessage().equals(ErrorMessage.INVALID_LOAN_AMOUNT.getMessage());
    }

    /**
     * This test ensures that if an invalid loan period is provided, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenInvalidLoanPeriod_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan(Mockito.any(DecisionRequest.class)))
                .thenThrow(new InvalidLoanPeriodException(ErrorMessage.INVALID_LOAN_PERIOD.getMessage()));

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(INVALID_LOAN_PERIOD_REQUEST))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(ErrorMessage.INVALID_LOAN_PERIOD.getMessage()))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.getLoanAmount() == null;
        assert response.getLoanPeriod() == null;
        assert response.getErrorMessage().equals(ErrorMessage.INVALID_LOAN_PERIOD.getMessage());
    }

    /**
     * This test ensures that if no valid loan is found, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenNoValidLoan_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan(Mockito.any(DecisionRequest.class)))
                .thenThrow(new NoValidLoanException(ErrorMessage.NO_VALID_LOAN_FOUND.getMessage()));

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(INVALID_LOAN_AMOUNT_REQUEST))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(ErrorMessage.NO_VALID_LOAN_FOUND.getMessage()))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.getLoanAmount() == null;
        assert response.getLoanPeriod() == null;
        assert response.getErrorMessage().equals(ErrorMessage.NO_VALID_LOAN_FOUND.getMessage());
    }

    /**
     * This test ensures that if an unexpected error occurs when processing the request, the controller returns
     * an HTTP Internal Server Error (500) response with the appropriate error message in the response body.
     */
    @Test
    public void givenUnexpectedErrorSuchAsInvalidRequestWithOnlyPersonalId_whenRequestDecision_thenReturnsInternalServerError() throws Exception {
        when(decisionEngine.calculateApprovedLoan(Mockito.any(DecisionRequest.class))).thenThrow(new RuntimeException());

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(new DecisionRequest(SEGMENT_1_PERSONAL_CODE, null, -1, COUNTRY)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(ErrorMessage.UNEXPECTED_ERROR.getMessage()))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.getLoanAmount() == null;
        assert response.getLoanPeriod() == null;
        assert response.getErrorMessage().equals(ErrorMessage.UNEXPECTED_ERROR.getMessage());
    }

    /**
     * This test ensures that if user is not satisfy the age range, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenInvalidAge_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan(Mockito.any(DecisionRequest.class)))
                .thenThrow(new AgeConstraintException(ErrorMessage.INVALID_AGE_RANGE.getMessage()));

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(INVALID_MAXIMUM_AGE_FOR_ESTONIA_REQUEST))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(ErrorMessage.INVALID_AGE_RANGE.getMessage()))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.getLoanAmount() == null;
        assert response.getLoanPeriod() == null;
        assert response.getErrorMessage().equals(ErrorMessage.INVALID_AGE_RANGE.getMessage());
    }

    /**
     * This test ensures that if country name is invalid, the controller returns
     * an HTTP Bad Request (400) response with the appropriate error message in the response body.
     */
    @Test
    public void givenInvalidCountryName_whenRequestDecision_thenReturnsBadRequest() throws Exception {
        when(decisionEngine.calculateApprovedLoan(Mockito.any(DecisionRequest.class)))
                .thenThrow(new HttpMessageNotReadableException(ErrorMessage.INVALID_REQUEST.getMessage()));

        MvcResult result = mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(INVALID_COUNTRY_NAME_REQUEST))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").isEmpty())
                .andExpect(jsonPath("$.loanPeriod").isEmpty())
                .andExpect(jsonPath("$.errorMessage").value(ErrorMessage.INVALID_REQUEST.getMessage()))
                .andReturn();

        DecisionResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DecisionResponse.class);
        assert response.getLoanAmount() == null;
        assert response.getLoanPeriod() == null;
        assert response.getErrorMessage().equals(ErrorMessage.INVALID_REQUEST.getMessage());
    }
}
