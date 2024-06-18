package artifacts.platform;

import artifacts.Artifacts;
import artifacts.client.item.renderer.ArtifactRenderer;
import artifacts.component.AbilityToggles;
import artifacts.component.SwimData;
import artifacts.item.WearableArtifactItem;
import artifacts.registry.ModAttributes;
import artifacts.registry.RegistrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface PlatformHelper {

    Stream<ItemStack> findAllEquippedBy(LivingEntity entity, Predicate<ItemStack> predicate);

    void iterateEquippedItems(LivingEntity entity, Consumer<ItemStack> consumer);

    <T> T reduceItems(LivingEntity entity, T init, BiFunction<ItemStack, T, T> f);

    boolean tryEquipInFirstSlot(LivingEntity entity, ItemStack item);

    @Nullable
    AbilityToggles getAbilityToggles(LivingEntity entity);

    @Nullable
    SwimData getSwimData(LivingEntity entity);

    Holder<Attribute> getSwimSpeedAttribute();

    default Holder<Attribute> registerAttribute(String name, Attribute attribute) {
        return RegistrySupplier.of(ModAttributes.ATTRIBUTES.register(Artifacts.id(name), () -> attribute));
    }

    void processWearableArtifactBuilder(WearableArtifactItem.Builder builder);

    void registerAdditionalDataComponents();

    void addCosmeticToggleTooltip(List<MutableComponent> tooltip, ItemStack stack);

    boolean isEyeInWater(Player player);

    boolean isVisibleOnHand(LivingEntity entity, InteractionHand hand, Item item);

    boolean areBootsHidden(LivingEntity entity);

    boolean isFishingRod(ItemStack stack);

    void registerArtifactRenderer(Item item, Supplier<ArtifactRenderer> rendererSupplier);

    @Nullable
    ArtifactRenderer getArtifactRenderer(Item item);

    Path getConfigDir();
}
