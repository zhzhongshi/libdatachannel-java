package tel.schich.libdatachannel;

import tel.schich.jniaccess.JNIAccess;

public class NativeOperationException extends RuntimeException {
    private final int code;

    @JNIAccess
    public NativeOperationException(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
