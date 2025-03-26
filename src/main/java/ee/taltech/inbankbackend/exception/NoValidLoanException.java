package ee.taltech.inbankbackend.exception;

/**
 * Thrown when no valid loan is found.
 */
public class NoValidLoanException extends RuntimeException {
    public NoValidLoanException(String message) {
        super(message);
    }
}
