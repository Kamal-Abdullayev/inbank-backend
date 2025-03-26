package ee.taltech.inbankbackend.exception;


/**
 * Thrown when user's age outside the approved age range.
 */
public class AgeConstraintException extends RuntimeException {
    public AgeConstraintException(String message) {
        super(message);
    }
}
