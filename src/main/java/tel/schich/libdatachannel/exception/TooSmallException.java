package tel.schich.libdatachannel.exception;

import generated.DatachannelLibrary;
import tel.schich.jniaccess.JNIAccess;

public class TooSmallException extends LibDataChannelException {
    @JNIAccess
    public TooSmallException() {
        super(DatachannelLibrary.RTC_ERR_TOO_SMALL);
    }
}
