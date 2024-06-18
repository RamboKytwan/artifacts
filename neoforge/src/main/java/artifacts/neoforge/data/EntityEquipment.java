package artifacts.neoforge.data;

import artifacts.loot.ConfigValueChance;
import artifacts.registry.ModItems;
import artifacts.registry.ModLootTables;
import com.google.common.collect.Sets;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.HashSet;
import java.util.Set;

public class EntityEquipment {

    private final LootTables lootTables;
    private final Set<EntityType<?>> entityTypes = new HashSet<>();

    public EntityEquipment(LootTables lootTables) {
        this.lootTables = lootTables;
    }

    public void addLootTables() {
        entityTypes.clear();

        addItems(EntityType.ZOMBIE,
                ModItems.COWBOY_HAT.value(),
                ModItems.BUNNY_HOPPERS.value(),
                ModItems.SCARF_OF_INVISIBILITY.value()
        );
        addItems(EntityType.HUSK,
                ModItems.VAMPIRIC_GLOVE.value(),
                ModItems.THORN_PENDANT.value()
        );
        addItems(EntityType.DROWNED,
                ModItems.SNORKEL.value(),
                ModItems.FLIPPERS.value()
        );
        addEquipment(EntityType.SKELETON, LootPool.lootPool()
                .add(item(ModItems.NIGHT_VISION_GOGGLES.value()))
                .add(LootTables.drinkingHat(1))
                .add(item(ModItems.FLAME_PENDANT.value()))
        );
        addItems(EntityType.STRAY,
                ModItems.SNOWSHOES.value(),
                ModItems.STEADFAST_SPIKES.value()
        );
        addItems(EntityType.WITHER_SKELETON,
                ModItems.FIRE_GAUNTLET.value(),
                ModItems.ANTIDOTE_VESSEL.value()
        );
        addItems(EntityType.PIGLIN,
                ModItems.GOLDEN_HOOK.value(),
                ModItems.UNIVERSAL_ATTRACTOR.value(),
                ModItems.OBSIDIAN_SKULL.value()
        );
        addItems(EntityType.ZOMBIFIED_PIGLIN,
                ModItems.GOLDEN_HOOK.value(),
                ModItems.UNIVERSAL_ATTRACTOR.value(),
                ModItems.OBSIDIAN_SKULL.value()
        );
        addItems(EntityType.PIGLIN_BRUTE,
                ModItems.ONION_RING.value(),
                ModItems.STRIDER_SHOES.value()
        );

        if (!entityTypes.equals(ModLootTables.ENTITY_EQUIPMENT.keySet())) {
            throw new IllegalStateException(Sets.symmetricDifference(entityTypes, ModLootTables.ENTITY_EQUIPMENT.keySet()).toString());
        }
    }

    public void addItems(EntityType<?> entityType, Item... items) {
        if (!ModLootTables.ENTITY_EQUIPMENT.containsKey(entityType)) {
            throw new IllegalArgumentException("Missing entity equipment entity: %s".formatted(BuiltInRegistries.ENTITY_TYPE.getKey(entityType)));
        }
        LootPool.Builder pool = LootPool.lootPool();
        for (Item item : items) {
            pool.add(item(item));
        }
        addEquipment(entityType, pool);
    }

    protected static LootPoolSingletonContainer.Builder<?> item(Item item) {
        return LootItem.lootTableItem(item).setWeight(1);
    }

    public void addEquipment(EntityType<?> entityType, LootPool.Builder pool) {
        entityTypes.add(entityType);
        LootTable.Builder builder = LootTable.lootTable();
        builder.withPool(pool.when(ConfigValueChance.entityEquipmentChance()));
        lootTables.addLootTable(ModLootTables.entityEquipmentLootTable(entityType).location().getPath(), provider -> builder, LootContextParamSets.ALL_PARAMS);
    }
}
