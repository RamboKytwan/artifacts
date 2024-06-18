package artifacts.fabric;

import artifacts.Artifacts;
import artifacts.fabric.event.SwimEventsFabric;
import artifacts.fabric.integration.CompatHandler;
import artifacts.fabric.integration.TrinketsIntegration;
import artifacts.fabric.registry.ModFeatures;
import artifacts.fabric.registry.ModLootTablesFabric;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.fabricmc.loader.api.FabricLoader;

public class ArtifactsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Artifacts.init();
        registerTrinkets();

        SwimEventsFabric.register();
        ModFeatures.register();

        LootTableEvents.MODIFY.register(ModLootTablesFabric::onLootTableLoad);

        runCompatibilityHandlers();
    }

    private void registerTrinkets() {
        if (FabricLoader.getInstance().isModLoaded("trinkets")) {
            TrinketsIntegration.registerTrinkets();
        }
    }

    private void runCompatibilityHandlers() {
        FabricLoader.getInstance().getEntrypoints("artifacts:compat_handlers", CompatHandler.class).stream()
                .filter(handler -> FabricLoader.getInstance().isModLoaded(handler.getModId()))
                .forEach(handler -> {
                    String modName = FabricLoader.getInstance().getModContainer(handler.getModId())
                            .map(container -> container.getMetadata().getName())
                            .orElse(handler.getModId());
                    Artifacts.LOGGER.info("Running compat handler for " + modName);

                    handler.run();
                });
    }
}
