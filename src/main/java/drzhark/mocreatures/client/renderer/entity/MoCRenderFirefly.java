/*
 * GNU GENERAL PUBLIC LICENSE Version 3
 */
package drzhark.mocreatures.client.renderer.entity;

import drzhark.mocreatures.MoCreatures;
import drzhark.mocreatures.client.model.MoCModelFirefly;
import drzhark.mocreatures.entity.ambient.MoCEntityFirefly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MoCRenderFirefly extends MoCRenderInsect<MoCEntityFirefly> {

    public MoCRenderFirefly(ModelBase modelbase) {
        super(modelbase);
        this.addLayer(new LayerMoCFirefly(this));
    }

    @Override
    protected void preRenderCallback(MoCEntityFirefly entityfirefly, float par2) {
        if (entityfirefly.getIsFlying()) {
            rotateFirefly(entityfirefly);
        } else if (entityfirefly.climbing()) {
            rotateAnimal(entityfirefly);
        }

    }

    protected void rotateFirefly(MoCEntityFirefly entityfirefly) {
        GlStateManager.rotate(40F, -1F, 0.0F, 0.0F);

    }

    @Override
    protected ResourceLocation getEntityTexture(MoCEntityFirefly entityfirefly) {
        return entityfirefly.getTexture();
    }

    private class LayerMoCFirefly implements LayerRenderer {
        private final MoCRenderFirefly mocRenderer;
        private final MoCModelFirefly mocModel = new MoCModelFirefly();

        public LayerMoCFirefly(MoCRenderFirefly p_i46112_1_) {
            this.mocRenderer = p_i46112_1_;
        }

        public void doRenderLayer(MoCEntityFirefly p_177162_1_, float p_177162_2_, float p_177162_3_, float p_177162_4_, float p_177162_5_, float p_177162_6_, float p_177162_7_, float p_177162_8_) {
            this.setTailBrightness(p_177162_1_, p_177162_4_);
            this.mocModel.setModelAttributes(this.mocRenderer.getMainModel());
            this.mocModel.setLivingAnimations(p_177162_1_, p_177162_2_, p_177162_3_, p_177162_4_);
            this.mocModel.render(p_177162_1_, p_177162_2_, p_177162_3_, p_177162_5_, p_177162_6_, p_177162_7_, p_177162_8_);
        }

        protected void setTailBrightness(MoCEntityFirefly entityliving, float par3) {
            this.mocRenderer.bindTexture(MoCreatures.proxy.getModelTexture("firefly_glow.png"));
            float var4 = 1.0F;
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.blendFunc(1, 1);
            char var5 = 61680;
            int var6 = var5 % 65536;
            int var7 = var5 / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, var6, var7);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, var4);
        }

        @Override
        public boolean shouldCombineTextures() {
            return true;
        }

        @Override
        public void doRenderLayer(EntityLivingBase p_177141_1_, float p_177141_2_, float p_177141_3_, float p_177141_4_, float p_177141_5_, float p_177141_6_, float p_177141_7_, float p_177141_8_) {
            this.doRenderLayer((MoCEntityFirefly) p_177141_1_, p_177141_2_, p_177141_3_, p_177141_4_, p_177141_5_, p_177141_6_, p_177141_7_, p_177141_8_);
        }
    }
}
