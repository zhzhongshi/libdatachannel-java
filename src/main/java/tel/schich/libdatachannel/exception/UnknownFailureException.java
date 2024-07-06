package tel.schich.libdatachannel.exception;

import tel.schich.jniaccess.JNIAccess;

public class UnknownFailureException extends LibDataChannelException {
    @JNIAccess
    public UnknownFailureException(int code) {
        super(code);
    }
}
