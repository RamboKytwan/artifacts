package artifacts.client.render.entity;

import artifacts.client.render.entity.model.MimicChestLayerModel;
import artifacts.client.render.entity.model.MimicModel;
import artifacts.common.config.ModConfig;
import artifacts.common.entity.MimicEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class MimicChestLayer extends RenderLayer<MimicEntity, MimicModel> {

    private final MimicChestLayerModel chestModel;
    public final Material vanillaChestMaterial;
    public final List<Material> chestMaterials;

    public static final List<String> QUARK_CHEST_TYPES = Arrays.asList(
            "oak",
            "spruce",
            "birch",
            "jungle",
            "acacia",
            "dark_oak",
            "warped",
            "crimson",
            "azalea",
            "blossom"
    );

    public MimicChestLayer(RenderLayerParent<MimicEntity, MimicModel> entityRenderer, EntityModelSet modelSet) {
        super(entityRenderer);

        Calendar calendar = Calendar.getInstance();
        boolean isChristmas = calendar.get(Calendar.MONTH) == Calendar.DECEMBER && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26
                || calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DATE) == 1;

        chestModel = new MimicChestLayerModel(modelSet.bakeLayer(MimicChestLayerModel.LAYER_LOCATION));
        chestMaterials = new ArrayList<>();
        vanillaChestMaterial = isChristmas ? Sheets.CHEST_XMAS_LOCATION : Sheets.CHEST_LOCATION;

        if (!isChristmas && ModList.get().isLoaded("lootr")) {
            ResourceLocation chestLocation = new ResourceLocation("lootr", "chest");
            chestMaterials.add(new Material(Sheets.CHEST_SHEET, chestLocation));
        } else {
            if (!isChristmas && ModList.get().isLoaded("quark")) {
                for (String chestType : QUARK_CHEST_TYPES) {
                    ResourceLocation chestLocation = new ResourceLocation("quark", String.format("model/chest/%s/normal", chestType));
                    chestMaterials.add(new Material(Sheets.CHEST_SHEET, chestLocation));
                }
            }

            chestMaterials.add(vanillaChestMaterial);
        }
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, MimicEntity mimic, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!mimic.isInvisible()) {
            matrixStack.pushPose();

            matrixStack.mulPose(Vector3f.XP.rotationDegrees(180));
            matrixStack.translate(-0.5, -1.5, -0.5);

            getParentModel().copyPropertiesTo(chestModel);
            chestModel.prepareMobModel(mimic, limbSwing, limbSwingAmount, partialTicks);
            chestModel.setupAnim(mimic, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            VertexConsumer builder = getChestMaterial(mimic).buffer(buffer, RenderType::entityCutout);
            chestModel.renderToBuffer(matrixStack, builder, packedLight, LivingEntityRenderer.getOverlayCoords(mimic, 0), 1, 1, 1, 1);

            matrixStack.popPose();
        }
    }

    private Material getChestMaterial(MimicEntity mimic) {
        if (!ModConfig.client.useModdedMimicTextures.get()) {
            return vanillaChestMaterial;
        }
        if (chestMaterials.size() == 1) {
            return chestMaterials.get(0);
        }
        return chestMaterials.get((int) (Math.abs(mimic.getUUID().getMostSignificantBits()) % chestMaterials.size()));
    }
}
