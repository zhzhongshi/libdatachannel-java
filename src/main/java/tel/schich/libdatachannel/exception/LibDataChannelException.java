package tel.schich.libdatachannel.exception;

public class LibDataChannelException extends RuntimeException {

    private final int errorCode;

    public LibDataChannelException(final int errorCode) {
        this.errorCode = errorCode;
    }

    public int errorCode() {
        return errorCode;
    }
}
