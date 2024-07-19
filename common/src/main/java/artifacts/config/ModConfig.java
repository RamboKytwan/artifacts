package artifacts.config;

import java.util.List;

public class ModConfig {

    public final ClientConfig client = new ClientConfig();
    public final GeneralConfig general = new GeneralConfig();
    public final ItemConfigs items = new ItemConfigs();

    public final List<ConfigManager> configs = List.of(general, client, items);

    public void setup() {
        configs.forEach(ConfigManager::setup);
    }
}
