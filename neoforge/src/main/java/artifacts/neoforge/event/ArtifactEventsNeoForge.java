package artifacts.neoforge.event;

import artifacts.ability.UpgradeToolTierAbility;
import artifacts.component.AbilityToggles;
import artifacts.event.ArtifactEvents;
import artifacts.platform.PlatformServices;
import artifacts.registry.ModAbilities;
import artifacts.registry.ModTags;
import artifacts.util.AbilityHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

public class ArtifactEventsNeoForge {

    public static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, ArtifactEventsNeoForge::onLivingDamage);
        NeoForge.EVENT_BUS.addListener(ArtifactEventsNeoForge::onLivingUpdate);
        NeoForge.EVENT_BUS.addListener(ArtifactEventsNeoForge::onDrinkingHatItemUse);
        NeoForge.EVENT_BUS.addListener(ArtifactEventsNeoForge::onGoldenHookExperienceDrop);
        NeoForge.EVENT_BUS.addListener(ArtifactEventsNeoForge::onKittySlippersChangeTarget);
        NeoForge.EVENT_BUS.addListener(ArtifactEventsNeoForge::onDiggingClawsHarvestCheck);
        NeoForge.EVENT_BUS.addListener(ArtifactEventsNeoForge::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(ArtifactEventsNeoForge::onCurioChanged);
    }

    private static void onCurioChanged(CurioChangeEvent event) {
        ArtifactEvents.onItemChanged(event.getEntity(), event.getFrom(), event.getTo());
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        AbilityToggles abilityToggles = PlatformServices.platformHelper.getAbilityToggles(event.getEntity());
        if (event.getEntity() instanceof ServerPlayer serverPlayer && abilityToggles != null) {
            abilityToggles.sendToClient(serverPlayer);
        }
    }

    private static void onLivingDamage(LivingDamageEvent event) {
        ArtifactEvents.onLivingDamaged(event.getEntity(), event.getSource(), event.getAmount());
    }

    private static void onLivingUpdate(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            onKittySlippersLivingUpdate(entity);
            ArtifactEvents.livingUpdate(entity);
        }
    }

    private static void onDrinkingHatItemUse(LivingEntityUseItemEvent.Start event) {
        event.setDuration(ArtifactEvents.modifyUseDuration(event.getDuration(), event.getItem(), event.getEntity()));
    }

    private static void onGoldenHookExperienceDrop(LivingExperienceDropEvent event) {
        int droppedXp = event.getDroppedExperience();
        int modifiedXp = ArtifactEvents.modifyExperience(droppedXp, event.getEntity(), event.getAttackingPlayer());
        event.setDroppedExperience(modifiedXp);
    }

    private static void onKittySlippersChangeTarget(LivingChangeTargetEvent event) {
        LivingEntity target = event.getNewTarget();
        if (AbilityHelper.hasAbilityActive(ModAbilities.SCARE_CREEPERS.value(), target)
                && event.getEntity() instanceof Mob creeper
                && creeper.getType().is(ModTags.CREEPERS)
        ) {
            event.setCanceled(true);
        }
    }

    private static void onKittySlippersLivingUpdate(LivingEntity entity) {
        if (AbilityHelper.hasAbilityActive(ModAbilities.SCARE_CREEPERS.value(), entity.getLastHurtByMob())
                && entity.getType().is(ModTags.CREEPERS)
        ) {
            entity.setLastHurtByMob(null);
        }
    }

    private static void onDiggingClawsHarvestCheck(PlayerEvent.HarvestCheck event) {
        event.setCanHarvest(event.canHarvest() || UpgradeToolTierAbility.canHarvestWithTier(event.getEntity(), event.getTargetBlock()));
    }
}
