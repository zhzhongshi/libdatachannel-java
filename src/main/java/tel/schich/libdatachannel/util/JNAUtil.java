package tel.schich.libdatachannel.util;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.BiFunction;

public class JNAUtil {

    public static Pointer toPointerArray(String... strings) {
        final var pointers = Arrays.stream(strings).map(JNAUtil::toPointer).toArray(Pointer[]::new);

        Memory pointerArray = new Memory((long) Native.POINTER_SIZE * pointers.length);
        pointerArray.write(0, pointers, 0, pointers.length);

        return pointerArray;
    }


    public static Pointer toPointer(String string) {
        final byte[] bytes = Native.toByteArray(string);
        return toPointer(bytes);
    }

    public static Pointer toPointer(byte[] bytes) {
        final Memory memory = new Memory(bytes.length + 1);
        memory.write(0, bytes, 0, bytes.length);
        return memory;
    }

    public static String readStringWithBuffer(BiFunction<ByteBuffer, Integer, Integer> biFunc) {
        var bufferSize = Util.wrapError(biFunc.apply(null, -1));
        final var buffer = ByteBuffer.allocate(bufferSize);
        bufferSize = Util.wrapError(biFunc.apply(buffer, bufferSize));
        final var bytes = new byte[bufferSize - 1];
        buffer.get(bytes, 0, bufferSize - 1);
        return new String(bytes);
    }
}
