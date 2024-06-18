package artifacts.fabric.platform;

import artifacts.Artifacts;
import artifacts.client.item.renderer.ArtifactRenderer;
import artifacts.component.AbilityToggles;
import artifacts.component.SwimData;
import artifacts.fabric.client.CosmeticsHelper;
import artifacts.fabric.registry.ModAttributesFabric;
import artifacts.fabric.registry.ModComponents;
import artifacts.fabric.registry.ModDataComponentsFabric;
import artifacts.fabric.trinket.TrinketsHelper;
import artifacts.item.WearableArtifactItem;
import artifacts.platform.PlatformHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.trinkets.TrinketSlot;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Tuple;
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
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
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
        return Stream.concat(TrinketsHelper.findAllEquippedBy(entity).filter(predicate), armor.stream());
    }

    @Override
    public <T> T reduceItems(LivingEntity entity, T init, BiFunction<ItemStack, T, T> f) {
        Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(entity);
        if (component.isPresent()) {
            for (Map<String, TrinketInventory> map : component.get().getInventory().values()) {
                for (TrinketInventory inventory : map.values()) {
                    for (int i = 0; i < inventory.getContainerSize(); i++) {
                        ItemStack item = inventory.getItem(i);
                        if (!item.isEmpty()) {
                            init = f.apply(item, init);
                        }
                    }
                }
            }
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
        if (TrinketsApi.getTrinketComponent(entity).isPresent()) {
            TrinketComponent component = TrinketsApi.getTrinketComponent(entity).get();
            for (Map<String, TrinketInventory> map : component.getInventory().values()) {
                for (TrinketInventory inventory : map.values()) {
                    if (TrinketSlot.canInsert(item, new SlotReference(inventory, 0), entity) && inventory.getItem(0).isEmpty()) {
                        inventory.setItem(0, item);
                        return true;
                    }
                }
            }
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
        return TrinketsApi.getTrinketComponent(entity).stream()
                .flatMap(component -> component.getAllEquipped().stream())
                .filter(tuple -> tuple.getA().inventory().getSlotType().getGroup().equals(
                        hand == InteractionHand.MAIN_HAND ? "hand" : "offhand"
                )).map(Tuple::getB)
                .filter(stack -> stack.is(item))
                .filter(stack -> !CosmeticsHelper.areCosmeticsToggledOffByPlayer(stack))
                .anyMatch(tuple -> true);
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
        TrinketRendererRegistry.registerRenderer(item, new ArtifactTrinketRenderer(rendererSupplier.get()));
    }

    @Nullable
    @Override
    public ArtifactRenderer getArtifactRenderer(Item item) {
        Optional<TrinketRenderer> renderer = TrinketRendererRegistry.getRenderer(item);
        if (renderer.isPresent() && renderer.get() instanceof ArtifactTrinketRenderer artifactTrinketRenderer) {
            return artifactTrinketRenderer.renderer();
        }
        return null;
    }

    private record ArtifactTrinketRenderer(ArtifactRenderer renderer) implements TrinketRenderer {

        @Override
        public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntity> entityModel, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            if (CosmeticsHelper.areCosmeticsToggledOffByPlayer(stack)) {
                return;
            }
            int index = slotReference.index() + (slotReference.inventory().getSlotType().getGroup().equals("hand") ? 0 : 1);
            renderer.render(stack, entity, index, poseStack, multiBufferSource, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
