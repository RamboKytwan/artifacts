package artifacts.neoforge.client;

import artifacts.client.HeliumFlamingoOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public class HeliumFlamingoOverlayRenderer {

    @SuppressWarnings("unused")
    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!(Minecraft.getInstance().getCameraEntity() instanceof Player)) {
            return;
        }
        if (!Minecraft.getInstance().options.hideGui) {
            if (HeliumFlamingoOverlay.renderOverlay(Minecraft.getInstance().gui.rightHeight, guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight())) {
                Minecraft.getInstance().gui.rightHeight += 10;
            }
        }
    }
}
