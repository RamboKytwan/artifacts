package artifacts.fabric.integration;

import artifacts.fabric.mixin.compat.apoli.ConditionTypeFactoryAccessor;
import artifacts.item.UmbrellaItem;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.condition.factory.ConditionTypeFactory;
import io.github.apace100.apoli.registry.ApoliRegistries;
import io.github.apace100.calio.data.SerializableData;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;
import java.util.function.Predicate;

public class ApoliCompat implements CompatHandler {

    @Override
    public void run() {
        for (ConditionTypeFactory<Entity> conditionFactory : ApoliRegistries.ENTITY_CONDITION) {
            processCondition(conditionFactory);
        }
        RegistryEntryAddedCallback.event(ApoliRegistries.ENTITY_CONDITION).register(
                (rawId, id, conditionFactory) -> processCondition(conditionFactory)
        );
    }

    @SuppressWarnings("unchecked")
    private void processCondition(ConditionTypeFactory<Entity> conditionFactory) {
        // Held-up umbrella blocks apoli:exposed_to_sun condition
        if (conditionFactory.getSerializerId().equals(Apoli.identifier("exposed_to_sun"))) {
            ConditionTypeFactoryAccessor<LivingEntity> conditionAccess = (ConditionTypeFactoryAccessor<LivingEntity>) conditionFactory;
            // Wrapper around original condition
            final Function<SerializableData.Instance, Predicate<LivingEntity>> condition = conditionAccess.getConditionFactory();
            conditionAccess.setConditionFactory((instance) -> condition.apply(instance).and(
                    entity -> !UmbrellaItem.isHoldingUmbrellaUpright(entity)
            ));
        }
    }

    @Override
    public String getModId() {
        return "apoli";
    }
}
