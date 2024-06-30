package tel.schich.libdatachannel.exception;

import generated.DatachannelLibrary;

public class NotAvailableException extends LibDataChannelException {

    public NotAvailableException() {
        super(DatachannelLibrary.RTC_ERR_NOT_AVAIL);
    }
}
