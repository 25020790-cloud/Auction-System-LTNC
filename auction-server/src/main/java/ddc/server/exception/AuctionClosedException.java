package ddc.server.exception;

public class AuctionClosedException extends Exception {
    private static final long serialVersionUID = 1L;

    public AuctionClosedException() {
        super();
    }

    public AuctionClosedException(String message) {
        super(message);
    }

    public AuctionClosedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuctionClosedException(Throwable cause) {
        super(cause);
    }
}
