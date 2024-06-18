package artifacts.fabric.mixin.compat.trinkets;

import artifacts.event.ArtifactEvents;
import dev.emi.trinkets.api.SlotType;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Unique
    private final Map<String, ItemStack> lastEquippedTrinkets = new HashMap<>();

    @SuppressWarnings("ConstantConditions")
    public LivingEntityMixin() {
        super(null, null);
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void tick(CallbackInfo info) {
        // noinspection ConstantConditions
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.isRemoved()) {
            return;
        }

        TrinketsApi.getTrinketComponent(entity).ifPresent(trinkets -> {
            Map<String, ItemStack> newlyEquippedTrinkets = new HashMap<>();
            trinkets.forEach((ref, stack) -> {
                TrinketInventory inventory = ref.inventory();

                ItemStack oldStack = getOldStack(inventory.getSlotType(), ref.index());
                ItemStack newStack = inventory.getItem(ref.index());
                if (!ItemStack.matches(newStack, oldStack)) {
                    ArtifactEvents.onItemChanged(entity, oldStack, newStack);
                }

                String newRef = inventory.getSlotType().getGroup() + "/" + inventory.getSlotType().getName() + "/" + ref.index();
                newlyEquippedTrinkets.put(newRef, newStack.copy());
            });

            lastEquippedTrinkets.clear();
            lastEquippedTrinkets.putAll(newlyEquippedTrinkets);
        });
    }

    @Unique
    private ItemStack getOldStack(SlotType type, int index) {
        return lastEquippedTrinkets.getOrDefault(type.getGroup() + "/" + type.getName() + "/" + index, ItemStack.EMPTY);
    }
}
