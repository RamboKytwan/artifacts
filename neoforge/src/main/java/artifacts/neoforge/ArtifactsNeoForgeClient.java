package artifacts.neoforge;

import artifacts.Artifacts;
import artifacts.ArtifactsClient;
import artifacts.client.item.ArtifactRenderers;
import artifacts.neoforge.client.ArmRenderHandler;
import artifacts.neoforge.client.ArtifactCooldownOverlayRenderer;
import artifacts.neoforge.client.HeliumFlamingoOverlayRenderer;
import artifacts.neoforge.client.UmbrellaArmPoseHandler;
import artifacts.mixin.accessors.client.LivingEntityRendererAccessor;
import artifacts.registry.ModItems;
import artifacts.registry.ModLootTables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiOverlaysEvent;
import net.neoforged.neoforge.client.gui.overlay.VanillaGuiOverlay;
import top.theillusivec4.curios.client.render.CuriosLayer;

import java.util.Set;

public class ArtifactsNeoForgeClient {

    public ArtifactsNeoForgeClient(IEventBus modBus) {
        ArtifactsClient.init();

        modBus.addListener(this::onClientSetup);
        modBus.addListener(this::onRegisterGuiOverlays);
        modBus.addListener(this::onAddLayers);

        ArmRenderHandler.setup();
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(
                () -> ItemProperties.register(
                        ModItems.UMBRELLA.get(),
                        Artifacts.id("blocking"),
                        (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1 : 0
                )
        );
        ArtifactRenderers.register();
        UmbrellaArmPoseHandler.setup();
    }

    public void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.AIR_LEVEL.id(), Artifacts.id("helium_flamingo_charge"), HeliumFlamingoOverlayRenderer::render);
        event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), Artifacts.id("artifact_cooldowns"), ArtifactCooldownOverlayRenderer::render);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void onAddLayers(EntityRenderersEvent.AddLayers event) {
        Set<EntityType<?>> entities = ModLootTables.ENTITY_EQUIPMENT.keySet();
        loop:
        for (EntityType<?> entity : entities) {
            EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(entity);
            if (renderer == null) {
                continue;
            }
            LivingEntityRenderer livingEntityRenderer = (LivingEntityRenderer<?, ?>) renderer;
            for (RenderLayer<?, ?> layer : ((LivingEntityRendererAccessor<?, ?>) livingEntityRenderer).getLayers()) {
                if (layer instanceof CuriosLayer<?, ?>) {
                    continue loop;
                }
            }
            livingEntityRenderer.addLayer(new CuriosLayer<>(livingEntityRenderer));
        }
    }
}
