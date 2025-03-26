package ee.taltech.inbankbackend.service;

import com.github.vladislavgoltjajev.personalcode.exception.PersonalCodeException;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeParser;
import com.github.vladislavgoltjajev.personalcode.locale.estonia.EstonianPersonalCodeValidator;
import ee.taltech.inbankbackend.dto.Country;
import ee.taltech.inbankbackend.dto.DecisionRequest;
import ee.taltech.inbankbackend.exception.*;
import ee.taltech.inbankbackend.util.AgeValidationConstants;
import ee.taltech.inbankbackend.util.DecisionEngineConstant;
import ee.taltech.inbankbackend.util.ErrorMessage;
import ee.taltech.inbankbackend.dto.DecisionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;

/**
 * A service class that provides a method for calculating an approved loan amount and period for a customer.
 * The loan amount is calculated based on the customer's credit modifier,
 * which is determined by the last four digits of their ID code.
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class DecisionEngineService {
    // Used to check for the validity of the presented ID code.
    private final EstonianPersonalCodeValidator validator = new EstonianPersonalCodeValidator();
    private final EstonianPersonalCodeParser estonianPersonalCodeParser = new EstonianPersonalCodeParser();
    private final DecisionEngineConstant decisionEngineConstant;
    private final AgeValidationConstants ageValidationConstants;

    public DecisionResponse calculateApprovedLoan(DecisionRequest request) {
        verifyInputs(request.getPersonalCode(), request.getLoanAmount(), request.getLoanPeriod());
        verifyCountry(request.getCountry());
        verifyAgeBasedOnTheCountry(request.getPersonalCode(), request.getCountry());

        int creditModifier = getCreditModifier(request.getPersonalCode());

        if (creditModifier == 0) {
            log.warn("Customer has debit and no valid loan found for personal code: {}", request.getPersonalCode());
            return new DecisionResponse(null, null, ErrorMessage.NO_VALID_LOAN_FOUND.getMessage());
        }

        float creditScore = calculateCreditScore(request.getLoanPeriod(), creditModifier, request.getLoanAmount());
        int approvedLoanAmount = calculateEligibleLoanAmount(request.getLoanPeriod(), creditModifier);
        if (creditScore < 0.1) {
            log.info("Credit score is too low for personal code: {}, credit score: {}", request.getPersonalCode(), creditScore);
            int expectedLoanPeriod = calculateSuitableLoanPeriod(request.getLoanAmount(), creditModifier);
            log.info("Expected loan period: {}, expected loan amount: {}", expectedLoanPeriod, approvedLoanAmount);

            if (!validateExpectedLoanPeriod(expectedLoanPeriod))
                return new DecisionResponse(approvedLoanAmount, null, ErrorMessage.NO_VALID_LOAN_FOUND.getMessage());

            if(!validateExpectedLoanAmount(approvedLoanAmount))
                return new DecisionResponse(null, expectedLoanPeriod, ErrorMessage.NO_VALID_LOAN_FOUND.getMessage());

            log.info("Decision response for personal code: {}, offered loan amount: {}, loan period: {}",
                    request.getPersonalCode(), approvedLoanAmount, expectedLoanPeriod);
            return new DecisionResponse(approvedLoanAmount, expectedLoanPeriod, ErrorMessage.NO_VALID_LOAN_FOUND.getMessage());
        }

        log.info("Credit score is valid and loan amount is approved for " +
                "personal code: {}, offered loan amount: {}, loan period: {}",
                request.getPersonalCode(), approvedLoanAmount, request.getLoanPeriod());
        return new DecisionResponse(approvedLoanAmount, request.getLoanPeriod(), null);
    }

    /**
     * Verify that all inputs are valid according to business rules.
     * If inputs are invalid, then throws corresponding exceptions.
     *
     * @param personalCode Provided personal ID code
     * @param loanAmount Requested loan amount
     * @param loanPeriod Requested loan period
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidLoanAmountException If the requested loan amount is invalid
     * @throws InvalidLoanPeriodException If the requested loan period is invalid
     * */
    private void verifyInputs(String personalCode, Long loanAmount, int loanPeriod) {
        if (!validator.isValid(personalCode)) {
            log.error("Invalid personal code: {}", personalCode);
            throw new InvalidPersonalCodeException(ErrorMessage.INVALID_PERSONAL_ID_CODE.getMessage());
        }
        if (loanAmount < decisionEngineConstant.getMinimumLoanAmount()
                || loanAmount > decisionEngineConstant.getMaximumLoanAmount()) {
            log.error("Invalid loan amount: {}", loanAmount);
            throw new InvalidLoanAmountException(ErrorMessage.INVALID_LOAN_AMOUNT.getMessage());
        }
        if (loanPeriod < decisionEngineConstant.getMinimumLoanPeriod()
                || loanPeriod > decisionEngineConstant.getMaximumLoanPeriod()
                || loanPeriod % decisionEngineConstant.getLoanInterval() != 0) {
            log.error("Invalid loan period: {}", loanPeriod);
            throw new InvalidLoanPeriodException(ErrorMessage.INVALID_LOAN_PERIOD.getMessage());
        }
    }

    /**
     * Verify user age based on the country field
     * If inputs are invalid, then throws corresponding exceptions.
     *
     * @param personalCode Provided personal ID code
     * @param country The loan requested country
     * @throws InvalidPersonalCodeException If the provided personal ID code is invalid
     * @throws InvalidCountryException If the requested country name is invalid
     * @throws AgeConstraintException If the user's age is not in the expected range
     * */
    private void verifyAgeBasedOnTheCountry(String personalCode, Country country) {
        int minAge;
        int maxAge;

        switch (country.name()) {
            case "ESTONIA" -> {
                minAge = ageValidationConstants.getMinimumAgeEs();
                maxAge = ageValidationConstants.getMaximumAgeEs();
            }
            case "LATVIA" -> {
                minAge = ageValidationConstants.getMinimumAgeLv();
                maxAge = ageValidationConstants.getMaximumAgeLv();
            }
            case "LITHUANIA" -> {
                minAge = ageValidationConstants.getMinimumAgeLt();
                maxAge = ageValidationConstants.getMaximumAgeLt();
            }
            default -> throw new InvalidCountryException(ErrorMessage.INVALID_COUNTRY_NAME.getMessage());
        }

        try {
            Period period = estonianPersonalCodeParser.getAge(personalCode);
            int age = period.getYears();
            int customerAgeAtLoanEnd = calculateCustomerAgeAtLoanEnd(period);

            if (age < minAge || customerAgeAtLoanEnd > maxAge) {
                log.error("Invalid age range for personal code: {}, age: {}, age in the end of loan: {}, " +
                                "the expected age range are from {} to {}",
                        personalCode, age, customerAgeAtLoanEnd, minAge, maxAge);
                throw new AgeConstraintException(ErrorMessage.INVALID_AGE_RANGE.getMessage());
            }
        } catch (PersonalCodeException e) {
            throw new InvalidPersonalCodeException(e.getMessage());
        }
    }

    /**
     * Verify that country name is valid.
     * If inputs are invalid, then throws corresponding exceptions.
     *
     * @param country The loan requested country
     * @throws InvalidCountryException If the requested country name is invalid
     * */
    private void verifyCountry(Country country) {
        if (country == null || country.name().isEmpty()) {
            throw new InvalidCountryException(ErrorMessage.INVALID_COUNTRY_NAME.getMessage());
        }
    }

    /**
     * Calculates the credit modifier of the customer to according to the last four digits of their ID code.
     * Debt - 0000...2499
     * Segment 1 - 2500...4999
     * Segment 2 - 5000...7499
     * Segment 3 - 7500...9999
     *
     * @param personalCode ID code of the customer that made the request.
     * @return Segment to which the customer belongs.
     */
    private int getCreditModifier(String personalCode) {
        int segment = Integer.parseInt(personalCode.substring(personalCode.length() - 4));
        // I'm not changing this part because it's hardcoded on purpose.

        if (segment < 2500) {
            return 0;
        } else if (segment < 5000) {
            return decisionEngineConstant.getSegment1CreditModifier();
        } else if (segment < 7500) {
            return decisionEngineConstant.getSegment2CreditModifier();
        }
        // This part open to future possible bugs
        return decisionEngineConstant.getSegment3CreditModifier();
    }

    /**
     * Calculates the largest valid loan for the current credit modifier and loan period.
     *
     * @return Largest valid loan amount
     */
    private int highestValidLoanAmount(int loanPeriod, int creditModifier) {
        log.info("Calculate highest valid loan amount for loan period: {}, credit modifier: {}", loanPeriod, creditModifier);
        return creditModifier * loanPeriod;
    }

    /**
     * Calculate user's credit score
     *
     * @param loanPeriod Requested loan period
     * @param creditModifier User's calculated credit modifier based on the last four digits of their ID code
     * @param loanAmount Requested loan amount
     * */
    private float calculateCreditScore(int loanPeriod, int creditModifier, long loanAmount) {
        return (((float) creditModifier / loanAmount) * loanPeriod) / 10;
    }

    /**
     * Calculates eligible loan amount for the user
     *
     * @param loanPeriod Requested loan period
     * @param creditModifier User's calculated credit modifier based on the last four digits of their ID code
     * @throws NoValidLoanException If the loan period is larger than the maximum loan period
     * */
    private int calculateEligibleLoanAmount(int loanPeriod, int creditModifier) {
        int approvedLoanAmount;

        while (highestValidLoanAmount(loanPeriod, creditModifier) < decisionEngineConstant.getMinimumLoanPeriod()) {
            loanPeriod++;
        }

        if (loanPeriod <= decisionEngineConstant.getMaximumLoanPeriod()) {
            approvedLoanAmount = Math.min(decisionEngineConstant.getMaximumLoanAmount(),
                    highestValidLoanAmount(loanPeriod, creditModifier));
        } else {
            throw new NoValidLoanException(ErrorMessage.NO_VALID_LOAN_FOUND.getMessage());
        }
        return approvedLoanAmount;
    }


    /**
     * Calculate suitable loan period for the user
     * If the expected loan period is not in the valid range, then return the nearest possible option
     * @param loanAmount Requested loan amount
     * @param creditModifier User's calculated credit modifier based on the last four digits of their ID code
     * */
    private int calculateSuitableLoanPeriod(Long loanAmount, int creditModifier) {
        int expectedLoanPeriod = Math.toIntExact((loanAmount / creditModifier));
        log.info("For the current amount, the minimum loan period could be: {}", expectedLoanPeriod);

        int remainingLoanMonths = expectedLoanPeriod % decisionEngineConstant.getLoanInterval();
        if (remainingLoanMonths != 0) {
            expectedLoanPeriod += (decisionEngineConstant.getLoanInterval() -
                    (remainingLoanMonths));
        }

        log.info("Adjusted loan period to the nearest possible option: {}", expectedLoanPeriod);
        return expectedLoanPeriod;
    }

    /**
     * Calculate user's age at the end of the loan period
     *
     * @param period user's age based on the personal code
     * */
    private int calculateCustomerAgeAtLoanEnd(Period period) {
        LocalDate birthDate = LocalDate.now().minus(period);
        LocalDate expectedLoanEndDate = LocalDate.now().plusMonths(decisionEngineConstant.getMaximumLoanPeriod());
        return Period.between(birthDate, expectedLoanEndDate).getYears();
    }

    /**
     * Validate expected loan period
     * If the expected loan period is not in the valid range, then return false
     *
     * @param expectedLoanPeriod Provided personal ID code
     * */
    private boolean validateExpectedLoanPeriod(int expectedLoanPeriod) {
        return expectedLoanPeriod >= decisionEngineConstant.getMinimumLoanPeriod() &&
                expectedLoanPeriod <= decisionEngineConstant.getMaximumLoanPeriod();
    }

    /**
     * Validate expected loan amount
     * If the expected loan amount is not in the valid range, then return false
     *
     * @param expectedLoanAmount Provided personal ID code
     * */
    private boolean validateExpectedLoanAmount(int expectedLoanAmount) {
        return expectedLoanAmount >= decisionEngineConstant.getMinimumLoanAmount() &&
                expectedLoanAmount <= decisionEngineConstant.getMaximumLoanAmount();
    }
}
