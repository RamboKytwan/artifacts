package artifacts.neoforge.platform;

import artifacts.client.item.renderer.ArtifactRenderer;
import artifacts.component.AbilityToggles;
import artifacts.component.SwimData;
import artifacts.item.WearableArtifactItem;
import artifacts.neoforge.integration.cosmeticarmor.CosmeticArmorCompat;
import artifacts.neoforge.integration.curios.CuriosIntegration;
import artifacts.neoforge.integration.curios.CuriosIntegrationClient;
import artifacts.neoforge.registry.ModAttachmentTypes;
import artifacts.platform.PlatformHelper;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class NeoForgePlatformHelper implements PlatformHelper {

    @Override
    public Stream<ItemStack> findAllEquippedBy(LivingEntity entity, Predicate<ItemStack> predicate) {
        List<ItemStack> armor = new ArrayList<>(4);
        for (ItemStack stack : entity.getArmorAndBodyArmorSlots()) {
            if (predicate.test(stack)) {
                armor.add(stack);
            }
        }
        if (ModList.get().isLoaded("curios")) {
            return Stream.concat(CuriosIntegration.findAllEquippedBy(entity, predicate), armor.stream());
        }
        return armor.stream();
    }

    @Override
    public void iterateEquippedItems(LivingEntity entity, Consumer<ItemStack> consumer) {
        if (ModList.get().isLoaded("curios")) {
            CuriosIntegration.iterateEquippedCurios(entity, consumer);
        }
        for (ItemStack item : entity.getArmorAndBodyArmorSlots()) {
            if (!item.isEmpty()) {
                consumer.accept(item);
            }
        }
    }

    @Override
    public <T> T reduceItems(LivingEntity entity, T init, BiFunction<ItemStack, T, T> f) {
        if (ModList.get().isLoaded("curios")) {
            init = CuriosIntegration.reduceCurios(entity, init, f);
        }

        for (ItemStack item : entity.getArmorAndBodyArmorSlots()) {
            if (!item.isEmpty()) {
                init = f.apply(item, init);
            }
        }
        return init;
    }

    @Override
    public boolean tryEquipInFirstSlot(LivingEntity entity, ItemStack item) {
        if (ModList.get().isLoaded("curios")) {
            return CuriosIntegration.tryEquipInFirstSlot(entity, item);
        }
        return false;
    }

    @Nullable
    @Override
    public AbilityToggles getAbilityToggles(LivingEntity entity) {
        return entity.getData(ModAttachmentTypes.ABILITY_TOGGLES);
    }

    @Nullable
    @Override
    public SwimData getSwimData(LivingEntity entity) {
        return entity.getData(ModAttachmentTypes.SWIM_DATA);
    }

    @Override
    public Holder<Attribute> getSwimSpeedAttribute() {
        return NeoForgeMod.SWIM_SPEED;
    }

    @Override
    public void processWearableArtifactBuilder(WearableArtifactItem.Builder builder) {

    }

    @Override
    public void registerAdditionalDataComponents() {

    }

    @Override
    public void addCosmeticToggleTooltip(List<MutableComponent> tooltip, ItemStack stack) {

    }

    @Override
    public boolean isEyeInWater(Player player) {
        return player.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value());
    }

    @Override
    public boolean isVisibleOnHand(LivingEntity entity, InteractionHand hand, Item item) {
        if (ModList.get().isLoaded("curios")) {
            return CuriosIntegration.isVisibleOnHand(entity, hand, item);
        }
        return false;
    }

    @Override
    public boolean areBootsHidden(LivingEntity entity) {
        if (entity instanceof Player player && ModList.get().isLoaded("cosmeticarmorreworked")) {
            return CosmeticArmorCompat.areBootsHidden(player);
        }
        return false;
    }

    @Override
    public boolean isFishingRod(ItemStack stack) {
        return stack.canPerformAction(ToolActions.FISHING_ROD_CAST);
    }

    @Override
    public void registerArtifactRenderer(Item item, Supplier<ArtifactRenderer> rendererSupplier) {
        if (ModList.get().isLoaded("curios")) {
            CuriosIntegrationClient.registerArtifactRenderer(item, rendererSupplier);
        }
    }

    @Nullable
    @Override
    public ArtifactRenderer getArtifactRenderer(Item item) {
        if (ModList.get().isLoaded("curios")) {
            return CuriosIntegrationClient.getArtifactRenderer(item);
        }
        return null;
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
