package ee.taltech.inbankbackend.exception;

/**
 * Thrown when requested country name is invalid.
 */
public class InvalidCountryException extends RuntimeException {
    public InvalidCountryException(String message) {
        super(message);
    }
}
