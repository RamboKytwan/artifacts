package artifacts.fabric.mixin.compat.apoli;

import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.calio.data.SerializableData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(value = ConditionTypeFactory.class, remap = false)
public interface ConditionTypeFactoryAccessor<T> {

    @Accessor
    Function<SerializableData.Instance, Predicate<T>> getConditionFactory();

    @Mutable
    @Accessor
    void setConditionFactory(Function<SerializableData.Instance, Predicate<T>> conditionFactory);
}
