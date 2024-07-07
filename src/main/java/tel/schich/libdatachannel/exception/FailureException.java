package tel.schich.libdatachannel.exception;

import tel.schich.jniaccess.JNIAccess;

public class FailureException extends LibDataChannelException {
    @JNIAccess
    public FailureException() {
        super(ERR_FAILURE);
    }
}
