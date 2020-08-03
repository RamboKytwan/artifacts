package artifacts.client.render.model.curio;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.RendererModel;
import net.minecraft.entity.LivingEntity;

public class DrinkingHatModel extends BipedModel<LivingEntity> {

    protected RendererModel hatShade;

    public DrinkingHatModel() {
        super(0.5F, 0, 64, 64);

        setVisible(false);
        bipedHead.showModel = true;
        bipedHeadwear.showModel = true;

        hatShade = new RendererModel(this, 0, 52);
        RendererModel straw = new RendererModel(this, 0, 50);
        RendererModel canLeft = new RendererModel(this, 0, 41);
        RendererModel canRight = new RendererModel(this, 12, 41);
        RendererModel strawLeft = new RendererModel(this, 0, 32);
        RendererModel strawRight = new RendererModel(this, 17, 32);

        hatShade.addBox(-4, -6, -8, 8, 1, 4);
        straw.addBox(-6, -1, -5, 12, 1, 1);
        canLeft.addBox(4, -11, -1, 3, 6, 3);
        canRight.addBox(-7, -11, -1, 3, 6, 3);
        strawLeft.addBox(5, -4, -3, 1, 1, 8);
        strawRight.addBox(-6, -4, -3, 1, 1, 8);

        bipedHead.addChild(hatShade);
        bipedHead.addChild(straw);
        bipedHead.addChild(canLeft);
        bipedHead.addChild(canRight);
        bipedHead.addChild(strawLeft);
        bipedHead.addChild(strawRight);

        strawLeft.rotateAngleX = 0.7853F;
        strawRight.rotateAngleX = 0.7853F;
    }
}
