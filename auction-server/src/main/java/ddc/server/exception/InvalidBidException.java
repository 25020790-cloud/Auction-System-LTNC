package ddc.server.exception;

public class InvalidBidException extends Exception{
    private static final long serialVersionUID = 1L;

    public InvalidBidException() {
        super();
    }

    public InvalidBidException(String message) {
        super(message);
    }

    public InvalidBidException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidBidException(Throwable cause) {
        super(cause);
    }
}
