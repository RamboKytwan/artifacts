package artifacts.fabric.mixin.ability.smeltores;

import artifacts.event.ArtifactEvents;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LootTable.class)
public class LootTableMixin {

    @ModifyReturnValue(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At("RETURN"))
    public ObjectArrayList<ItemStack> modifyBlockDrops(ObjectArrayList<ItemStack> original, LootContext context) {
        return ArtifactEvents.getPickaxeHeaterModifiedBlockDrops(original, context, ConventionalBlockTags.ORES, ConventionalItemTags.RAW_MATERIALS);
    }
}
