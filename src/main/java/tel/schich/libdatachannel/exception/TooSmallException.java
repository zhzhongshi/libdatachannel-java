package tel.schich.libdatachannel.exception;

import generated.DatachannelLibrary;

public class TooSmallException extends LibDataChannelException {

    public TooSmallException() {
        super(DatachannelLibrary.RTC_ERR_TOO_SMALL);
    }
}
