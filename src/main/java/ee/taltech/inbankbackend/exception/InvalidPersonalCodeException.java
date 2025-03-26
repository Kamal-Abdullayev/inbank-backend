package ee.taltech.inbankbackend.exception;

/**
 * Thrown when provided personal ID code is invalid.
 */
public class InvalidPersonalCodeException extends RuntimeException {
    public InvalidPersonalCodeException(String message) {
        super(message);
    }
}
