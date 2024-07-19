package artifacts.fabric.registry;

import artifacts.registry.ModDataComponents;
import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;

public class ModDataComponentsFabric {

    public static final RegistrySupplier<DataComponentType<Boolean>> COSMETICS_ENABLED = ModDataComponents.DATA_COMPONENT_TYPES.register("cosmetic_toggle", () ->
            DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build()
    );

    public static void register() {
        // no-op
    }
}
