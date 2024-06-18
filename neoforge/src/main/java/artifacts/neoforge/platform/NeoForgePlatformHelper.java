package artifacts.neoforge.platform;

import artifacts.client.item.renderer.ArtifactRenderer;
import artifacts.component.AbilityToggles;
import artifacts.component.SwimData;
import artifacts.item.WearableArtifactItem;
import artifacts.neoforge.integration.CosmeticArmorCompat;
import artifacts.neoforge.registry.ModAttachmentTypes;
import artifacts.platform.PlatformHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
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
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        return Stream.concat(CuriosApi.getCuriosInventory(entity)
                .map(inv -> inv.findCurios(predicate))
                .orElse(List.of()).stream()
                .map(SlotResult::stack), armor.stream());
    }

    @Override
    public void iterateEquippedItems(LivingEntity entity, Consumer<ItemStack> consumer) {
        Optional<ICuriosItemHandler> itemHandler = CuriosApi.getCuriosInventory(entity);
        if (itemHandler.isPresent()) {
            for (ICurioStacksHandler stacksHandler : itemHandler.get().getCurios().values()) {
                for (int i = 0; i < stacksHandler.getStacks().getSlots(); i++) {
                    ItemStack item = stacksHandler.getStacks().getStackInSlot(i);
                    if (!item.isEmpty()) {
                        consumer.accept(item);
                    }
                }
            }
        }
        for (ItemStack item : entity.getArmorAndBodyArmorSlots()) {
            if (!item.isEmpty()) {
                consumer.accept(item);
            }
        }
    }

    @Override
    public <T> T reduceItems(LivingEntity entity, T init, BiFunction<ItemStack, T, T> f) {
        Optional<ICuriosItemHandler> itemHandler = CuriosApi.getCuriosInventory(entity);
        if (itemHandler.isPresent()) {
            for (ICurioStacksHandler stacksHandler : itemHandler.get().getCurios().values()) {
                for (int i = 0; i < stacksHandler.getStacks().getSlots(); i++) {
                    ItemStack item = stacksHandler.getStacks().getStackInSlot(i);
                    if (!item.isEmpty()) {
                        init = f.apply(item, init);
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
        Optional<ICuriosItemHandler> optional = CuriosApi.getCuriosInventory(entity);
        if (optional.isPresent()) {
            ICuriosItemHandler handler = optional.get();
            for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                for (int i = 0; i < entry.getValue().getSlots(); i++) {
                    SlotContext slotContext = new SlotContext(entry.getKey(), entity, i, false, true);
                    //noinspection ConstantConditions
                    if (CuriosApi.isStackValid(slotContext, item) && entry.getValue().getStacks().getStackInSlot(i).isEmpty()) {
                        entry.getValue().getStacks().setStackInSlot(i, item);
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
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> Optional.ofNullable(handler.getCurios().get("hands")))
                .map(stacksHandler -> {
                    int startSlot = hand == InteractionHand.MAIN_HAND ? 0 : 1;
                    for (int slot = startSlot; slot < stacksHandler.getSlots(); slot += 2) {
                        ItemStack stack = stacksHandler.getCosmeticStacks().getStackInSlot(slot);
                        if (stack.isEmpty() && stacksHandler.getRenders().get(slot)) {
                            stack = stacksHandler.getStacks().getStackInSlot(slot);
                        }

                        if (stack.getItem() == item) {
                            return true;
                        }
                    }
                    return false;
                }).orElse(false);
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
        CuriosRendererRegistry.register(item, () -> new ArtifactCurioRenderer(rendererSupplier.get()));
    }

    @Nullable
    @Override
    public ArtifactRenderer getArtifactRenderer(Item item) {
        Optional<ICurioRenderer> renderer = CuriosRendererRegistry.getRenderer(item);
        if (renderer.isPresent() && renderer.get() instanceof ArtifactCurioRenderer artifactTrinketRenderer) {
            return artifactTrinketRenderer.renderer();
        }
        return null;
    }

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    private record ArtifactCurioRenderer(ArtifactRenderer renderer) implements ICurioRenderer {

        @Override
        public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack, SlotContext slotContext, PoseStack poseStack, RenderLayerParent<T, M> renderLayerParent, MultiBufferSource multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            renderer.render(stack, slotContext.entity(), slotContext.index(), poseStack, multiBufferSource, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
        }
    }
}
