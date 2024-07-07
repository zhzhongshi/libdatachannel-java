package tel.schich.libdatachannel.exception;

import tel.schich.jniaccess.JNIAccess;

public class NativeOperationException extends LibDataChannelException {
    @JNIAccess
    public NativeOperationException(int code) {
        super(code);
    }
}
