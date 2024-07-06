package tel.schich.libdatachannel.exception;

import generated.DatachannelLibrary;
import tel.schich.jniaccess.JNIAccess;

public class FailureException extends LibDataChannelException {
    @JNIAccess
    public FailureException() {
        super(DatachannelLibrary.RTC_ERR_FAILURE);
    }
}
