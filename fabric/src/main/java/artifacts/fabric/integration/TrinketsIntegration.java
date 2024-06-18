package artifacts.fabric.integration;

import artifacts.client.item.renderer.ArtifactRenderer;
import artifacts.fabric.client.CosmeticsHelper;
import artifacts.fabric.trinket.WearableArtifactTrinket;
import artifacts.item.WearableArtifactItem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.trinkets.api.*;
import dev.emi.trinkets.api.client.TrinketRenderer;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TrinketsIntegration {

    public static void registerTrinkets() {
        BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof WearableArtifactItem)
                .forEach(item -> TrinketsApi.registerTrinket(item, new WearableArtifactTrinket((WearableArtifactItem) item)));
    }

    public static boolean equipTrinket(LivingEntity entity, ItemStack stack) {
        return TrinketItem.equipItem(entity, stack);
    }

    public static Stream<ItemStack> findAllEquippedBy(LivingEntity entity) {
        return TrinketsApi.getTrinketComponent(entity)
                .map(TrinketComponent::getAllEquipped)
                .orElse(List.of())
                .stream()
                .map(Tuple::getB);
    }

    public static void iterateEquippedTrinkets(LivingEntity entity, Consumer<ItemStack> consumer) {
        TrinketsApi.getTrinketComponent(entity).ifPresent(component -> {
            for (Map<String, TrinketInventory> map : component.getInventory().values()) {
                for (TrinketInventory inventory : map.values()) {
                    for (int i = 0; i < inventory.getContainerSize(); i++) {
                        ItemStack item = inventory.getItem(i);
                        if (!item.isEmpty()) {
                            consumer.accept(item);
                        }
                    }
                }
            }
        });
    }

    public static <T> T reduceTrinkets(LivingEntity entity, T init, BiFunction<ItemStack, T, T> f) {
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
        return init;
    }

    public static boolean isVisibleOnHand(LivingEntity entity, InteractionHand hand, Item item) {
        return TrinketsApi.getTrinketComponent(entity).stream()
                .flatMap(component -> component.getAllEquipped().stream())
                .filter(tuple -> tuple.getA().inventory().getSlotType().getGroup().equals(
                        hand == InteractionHand.MAIN_HAND ? "hand" : "offhand"
                )).map(Tuple::getB)
                .filter(stack -> stack.is(item))
                .filter(stack -> !CosmeticsHelper.areCosmeticsToggledOffByPlayer(stack))
                .anyMatch(tuple -> true);
    }

    public static void registerArtifactRenderer(Item item, Supplier<ArtifactRenderer> rendererSupplier) {
        TrinketRendererRegistry.registerRenderer(item, new ArtifactTrinketRenderer(rendererSupplier.get()));
    }

    @Nullable
    public static ArtifactRenderer getArtifactRenderer(Item item) {
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
}
