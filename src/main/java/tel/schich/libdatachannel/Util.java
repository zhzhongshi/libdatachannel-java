package tel.schich.libdatachannel;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Util {

    public static <T, K> Map<K, T> mappedEnum(final T[] values, Function<T, K> mapper) {
        return Arrays.stream(values).collect(Collectors.toMap(mapper, s -> s));
    }
}
