package tel.schich.libdatachannel.util;

import generated.DatachannelLibrary;
import tel.schich.libdatachannel.exception.FailureException;
import tel.schich.libdatachannel.exception.InvalidException;
import tel.schich.libdatachannel.exception.LibDataChannelException;
import tel.schich.libdatachannel.exception.NotAvailableException;
import tel.schich.libdatachannel.exception.TooSmallException;

import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Util {

    public static <T, K> Map<K, T> mappedEnum(final T[] values, Function<T, K> mapper) {
        return Arrays.stream(values).collect(Collectors.toMap(mapper, s -> s));
    }

    public static int wrapError(int code) throws LibDataChannelException{
        return switch (code) {
            case DatachannelLibrary.RTC_ERR_SUCCESS -> 0;
            case DatachannelLibrary.RTC_ERR_NOT_AVAIL -> throw new NotAvailableException();
            case DatachannelLibrary.RTC_ERR_TOO_SMALL -> throw new TooSmallException();
            case DatachannelLibrary.RTC_ERR_INVALID -> throw new InvalidException();
            case DatachannelLibrary.RTC_ERR_FAILURE -> throw new FailureException();

            default ->  code;
        };
    }

    public static <C, A> void registerCallback(final BiFunction<Integer, C, Integer> cbRegister, final A apiCb, final C cb, final Integer channelId) {
        if (apiCb == null) {
            wrapError(cbRegister.apply(channelId, null));
        } else {
            wrapError(cbRegister.apply(channelId, cb));
        }
    }
}
