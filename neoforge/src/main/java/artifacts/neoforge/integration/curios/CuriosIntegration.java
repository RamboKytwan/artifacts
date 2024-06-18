package artifacts.neoforge.integration.curios;

import artifacts.event.ArtifactEvents;
import artifacts.item.WearableArtifactItem;
import artifacts.neoforge.curio.WearableArtifactCurio;
import artifacts.registry.ModItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.event.CurioChangeEvent;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CuriosIntegration {

    public static void setup(IEventBus modBus) {
        modBus.addListener(CuriosIntegration::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(CuriosIntegration::onCurioChanged);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        if (ModList.get().isLoaded("curios")) {
            ModItems.ITEMS.forEach(entry -> {
                if (entry.get() instanceof WearableArtifactItem item) {
                    CuriosApi.registerCurio(item, new WearableArtifactCurio(item));
                }
            });
        }
    }

    private static void onCurioChanged(CurioChangeEvent event) {
        ArtifactEvents.onItemChanged(event.getEntity(), event.getFrom(), event.getTo());
    }

    public static Stream<ItemStack> findAllEquippedBy(LivingEntity entity, Predicate<ItemStack> predicate) {
        return CuriosApi.getCuriosInventory(entity)
                .map(inv -> inv.findCurios(predicate))
                .orElse(List.of()).stream()
                .map(SlotResult::stack);
    }

    public static void iterateEquippedCurios(LivingEntity entity, Consumer<ItemStack> consumer) {
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
    }

    public static <T> T reduceCurios(LivingEntity entity, T init, BiFunction<ItemStack, T, T> f) {
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
        return init;
    }

    public static boolean tryEquipInFirstSlot(LivingEntity entity, ItemStack item) {
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

    public static boolean isVisibleOnHand(LivingEntity entity, InteractionHand hand, Item item) {
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
}
