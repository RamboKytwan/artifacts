package artifacts.fabric.platform;

import artifacts.Artifacts;
import artifacts.client.item.renderer.ArtifactRenderer;
import artifacts.component.AbilityToggles;
import artifacts.component.SwimData;
import artifacts.fabric.client.CosmeticsHelper;
import artifacts.fabric.integration.TrinketsIntegration;
import artifacts.fabric.registry.ModAttributesFabric;
import artifacts.fabric.registry.ModComponents;
import artifacts.fabric.registry.ModDataComponentsFabric;
import artifacts.item.WearableArtifactItem;
import artifacts.platform.PlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class FabricPlatformHelper implements PlatformHelper {

    @Override
    public Stream<ItemStack> findAllEquippedBy(LivingEntity entity, Predicate<ItemStack> predicate) {
        List<ItemStack> armor = new ArrayList<>(4);
        for (ItemStack stack : entity.getArmorAndBodyArmorSlots()) {
            if (predicate.test(stack)) {
                armor.add(stack);
            }
        }

        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            return Stream.concat(TrinketsIntegration.findAllEquippedBy(entity).filter(predicate), armor.stream());
        }
        return armor.stream();
    }

    @Override
    public void iterateEquippedItems(LivingEntity entity, Consumer<ItemStack> consumer) {
        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            TrinketsIntegration.iterateEquippedTrinkets(entity, consumer);
        }
        for (ItemStack item : entity.getArmorAndBodyArmorSlots()) {
            if (!item.isEmpty()) {
                consumer.accept(item);
            }
        }
    }

    @Override
    public <T> T reduceItems(LivingEntity entity, T init, BiFunction<ItemStack, T, T> f) {
        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            init = TrinketsIntegration.reduceTrinkets(entity, init, f);
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
        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            return TrinketsIntegration.equipTrinket(entity, item);
        }
        return false;
    }

    @Nullable
    @Override
    public AbilityToggles getAbilityToggles(LivingEntity entity) {
        return ModComponents.ABILITY_TOGGLES.getNullable(entity);
    }

    @Nullable
    @Override
    public SwimData getSwimData(LivingEntity entity) {
        return ModComponents.SWIM_DATA.getNullable(entity);
    }

    @Override
    public Holder<Attribute> getSwimSpeedAttribute() {
        return ModAttributesFabric.SWIM_SPEED;
    }

    @Override
    public Holder<Attribute> registerAttribute(String name, Attribute attribute) {
        return Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, Artifacts.id(name), attribute);
    }

    @Override
    public void processWearableArtifactBuilder(WearableArtifactItem.Builder builder) {
        builder.properties(properties -> properties.component(ModDataComponentsFabric.COSMETICS_ENABLED.get(), true));
    }

    @Override
    public void registerAdditionalDataComponents() {
        ModDataComponentsFabric.register();
    }

    @Override
    public void addCosmeticToggleTooltip(List<MutableComponent> tooltip, ItemStack stack) {
        if (CosmeticsHelper.areCosmeticsToggledOffByPlayer(stack)) {
            tooltip.add(
                    Component.translatable("%s.tooltip.cosmetics_disabled".formatted(Artifacts.MOD_ID))
                            .withStyle(ChatFormatting.ITALIC)
            );
        } else {
            tooltip.add(
                    Component.translatable("%s.tooltip.cosmetics_enabled".formatted(Artifacts.MOD_ID))
                            .withStyle(ChatFormatting.ITALIC)
            );
        }
    }

    @Override
    public boolean isEyeInWater(Player player) {
        return player.isEyeInFluid(FluidTags.WATER);
    }

    @Override
    public boolean isVisibleOnHand(LivingEntity entity, InteractionHand hand, Item item) {
        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            return TrinketsIntegration.isVisibleOnHand(entity, hand, item);
        }
        return false;
    }

    @Override
    public boolean areBootsHidden(LivingEntity entity) {
        return false;
    }

    @Override
    public boolean isFishingRod(ItemStack stack) {
        return stack.getItem() instanceof FishingRodItem;
    }

    @Override
    public void registerArtifactRenderer(Item item, Supplier<ArtifactRenderer> rendererSupplier) {
        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            TrinketsIntegration.registerArtifactRenderer(item, rendererSupplier);
        }
    }

    @Nullable
    @Override
    public ArtifactRenderer getArtifactRenderer(Item item) {
        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            return TrinketsIntegration.getArtifactRenderer(item);
        }
        return null;
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
