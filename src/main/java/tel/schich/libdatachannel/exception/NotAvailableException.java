package tel.schich.libdatachannel.exception;

import generated.DatachannelLibrary;
import tel.schich.jniaccess.JNIAccess;

public class NotAvailableException extends LibDataChannelException {
    @JNIAccess
    public NotAvailableException() {
        super(DatachannelLibrary.RTC_ERR_NOT_AVAIL);
    }
}
