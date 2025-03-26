package ee.taltech.inbankbackend.exception;

import ee.taltech.inbankbackend.dto.DecisionResponse;
import ee.taltech.inbankbackend.util.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GeneralExceptionAdvice {
    @ExceptionHandler(InvalidLoanAmountException.class)
    public ResponseEntity<DecisionResponse> handle(InvalidLoanAmountException exception) {
        DecisionResponse response = new DecisionResponse(null, null, exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidLoanPeriodException.class)
    public ResponseEntity<DecisionResponse> handle(InvalidLoanPeriodException exception) {
        DecisionResponse response = new DecisionResponse(null, null, exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidPersonalCodeException.class)
    public ResponseEntity<DecisionResponse> handle(InvalidPersonalCodeException exception) {
        DecisionResponse response = new DecisionResponse(null, null, exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NoValidLoanException.class)
    public ResponseEntity<DecisionResponse> handle(NoValidLoanException exception) {
        DecisionResponse response = new DecisionResponse(null, null, exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AgeConstraintException.class)
    public ResponseEntity<DecisionResponse> handle(AgeConstraintException exception) {
        DecisionResponse response = new DecisionResponse(null, null, exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<DecisionResponse> handle(HttpMessageNotReadableException exception) {
        DecisionResponse response = new DecisionResponse(null, null, ErrorMessage.INVALID_REQUEST.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DecisionResponse> handle(Exception exception) {
        DecisionResponse response = new DecisionResponse(null, null, ErrorMessage.UNEXPECTED_ERROR.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
