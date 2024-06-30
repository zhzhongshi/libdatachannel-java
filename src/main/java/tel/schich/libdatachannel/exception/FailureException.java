package tel.schich.libdatachannel.exception;

import generated.DatachannelLibrary;

public class FailureException extends LibDataChannelException {

    public FailureException() {
        super(DatachannelLibrary.RTC_ERR_FAILURE);
    }
}
