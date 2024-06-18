package artifacts.item;

import artifacts.Artifacts;
import artifacts.ability.ArtifactAbility;
import artifacts.ability.AttributeModifierAbility;
import artifacts.ability.IncreaseEnchantmentLevelAbility;
import artifacts.config.value.Value;
import artifacts.registry.ModDataComponents;
import artifacts.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class WearableArtifactItem extends Item {

    private final Holder<SoundEvent> equipSound;
    private final float equipSoundPitch;

    public WearableArtifactItem(Item.Properties properties, Holder<SoundEvent> equipSound, float equipSoundPitch) {
        super(properties);
        this.equipSound = equipSound;
        this.equipSoundPitch = equipSoundPitch;
    }

    public SoundEvent getEquipSound() {
        return equipSound.value();
    }

    public float getEquipSoundPitch() {
        return equipSoundPitch;
    }

    public static class Builder {

        private final String itemName;
        private Holder<SoundEvent> equipSound = SoundEvents.ARMOR_EQUIP_GENERIC;
        private float equipSoundPitch = 1;
        private final List<ArtifactAbility> abilities = new ArrayList<>();
        private final Item.Properties properties = new Item.Properties();

        public Builder(String itemName) {
            this.itemName = itemName;
        }

        public Builder equipSound(SoundEvent equipSound) {
            return equipSound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(equipSound));
        }

        public Builder equipSound(Holder<SoundEvent> equipSound) {
            this.equipSound = equipSound;
            return this;
        }

        public Builder equipSoundPitch(float pitch) {
            this.equipSoundPitch = pitch;
            return this;
        }

        public Builder addAttributeModifier(Holder<Attribute> attribute, Value<Double> amount, AttributeModifier.Operation operation) {
            return addAttributeModifier(attribute, amount, operation, true);
        }

        public Builder addAttributeModifier(Holder<Attribute> attribute, Value<Double> amount, AttributeModifier.Operation operation, boolean ignoreCooldown) {
            return addAbility(new AttributeModifierAbility(attribute, amount, operation, Artifacts.id(itemName + '/' + attribute.unwrapKey().orElseThrow().location().getPath()), ignoreCooldown));
        }

        public Builder increasesEnchantment(ResourceKey<Enchantment> enchantment, Value<Integer> amount) {
            return addAbility(new IncreaseEnchantmentLevelAbility(enchantment, amount));
        }

        public Builder addAbility(ArtifactAbility ability) {
            this.abilities.add(ability);
            return this;
        }

        public Builder properties(Consumer<Properties> consumer) {
            consumer.accept(this.properties);
            return this;
        }

        public WearableArtifactItem build() {
            //noinspection UnstableApiUsage
            properties.arch$tab(ModItems.CREATIVE_TAB.supplier());
            properties.component(ModDataComponents.ABILITIES.value(), abilities);
            properties.stacksTo(1).rarity(Rarity.RARE).fireResistant();
            return new WearableArtifactItem(properties, equipSound, equipSoundPitch);
        }
    }
}
