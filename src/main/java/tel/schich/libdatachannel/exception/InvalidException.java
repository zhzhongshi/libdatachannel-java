package tel.schich.libdatachannel.exception;

import generated.DatachannelLibrary;
import tel.schich.jniaccess.JNIAccess;

public class InvalidException extends LibDataChannelException {
    @JNIAccess
    public InvalidException() {
        super(DatachannelLibrary.RTC_ERR_INVALID);
    }
}
