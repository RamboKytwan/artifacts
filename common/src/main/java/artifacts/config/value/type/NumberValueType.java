package artifacts.config.value.type;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public abstract class NumberValueType<T extends Number & Comparable<T>> extends ValueType<T, T> {

    private final T min;
    private final T max;
    private final Codec<T> valueCodec;
    private final StreamCodec<ByteBuf, T> valueStreamCodec;

    public NumberValueType(T min, T max, Codec<T> valueCodec, StreamCodec<ByteBuf, T> valueStreamCodec) {
        this.min = min;
        this.max = max;
        this.valueCodec = valueCodec;
        this.valueStreamCodec = valueStreamCodec;
    }

    public T getMin() {
        return min;
    }

    public T getMax() {
        return max;
    }

    @Override
    protected Codec<T> valueCodec() {
        return valueCodec;
    }

    @Override
    public StreamCodec<ByteBuf, T> valueStreamCodec() {
        return valueStreamCodec;
    }

    @Override
    public boolean isCorrect(T value) {
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }

    @Override
    public String makeError(T value) {
        if (min.doubleValue() == 0 && max.intValue() == Integer.MAX_VALUE) {
            return "Value must be non-negative: %s".formatted(value);
        }
        return "Value must be within range [%s;%s]: %s".formatted(min, max, value);
    }

    @Override
    public String getAllowedValuesComment() {
        return "Range: %s ~ %s".formatted(getMin(), getMax())
                .replace("0.0 ~ Infinity", "> 0.0")
                .replace("0 ~ 2147483647", "> 0");
    }

    @Override
    public T read(T value) {
        return value;
    }

    @Override
    public T write(T value) {
        return value;
    }
}
