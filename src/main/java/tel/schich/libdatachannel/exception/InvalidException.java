package tel.schich.libdatachannel.exception;

import generated.DatachannelLibrary;

public class InvalidException extends LibDataChannelException {

    public InvalidException() {
        super(DatachannelLibrary.RTC_ERR_INVALID);
    }
}
