package tel.schich.libdatachannel;

import tel.schich.jniaccess.JNIAccess;

public class NativeOperationException {
    private int errno;

    @JNIAccess
    public NativeOperationException(int errno) {
        this.errno = errno;
    }

    public int getErrno() {
        return errno;
    }
}
