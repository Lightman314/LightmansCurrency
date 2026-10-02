package io.github.lightman314.lightmanscurrency.client.features.rendering;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.api.helpers.MathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import org.joml.Quaternionf;

public class BlockStateRenderer extends PictureInPictureRenderer<BlockStateRenderState> {

    public BlockStateRenderer(MultiBufferSource.BufferSource bufferSource) { super(bufferSource); }

    @Override
    public Class<BlockStateRenderState> getRenderStateClass() { return BlockStateRenderState.class; }

    @Override
    protected void renderToTexture(BlockStateRenderState renderState,PoseStack pose) {
        //Center the pose stack, cause apparently this is off a little for... reasons?
        //I honestly don't know, I just know this offset magically makes the block appear in the correct place
        //Not sure why the z offset is even needed if I'm being honest
        pose.translate(0,-0.5f,-0.5f);
        //Rotate, Then offset back to the corner
        //Flip the block right-side up
        pose.mulPose(new Quaternionf().fromAxisAngleDeg(MathHelper.XP,180f));
        pose.mulPose(renderState.rotation);
        pose.translate(-0.5f,-0.5f,-0.5f);

        //Collect the state
        BlockModelRenderState state = renderState.state;
        //Set up the lighting perhaps? May not be needed
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.LEVEL);
        FeatureRenderDispatcher featureRenderDispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
        SubmitNodeStorage submitNodeStorage = featureRenderDispatcher.getSubmitNodeStorage();
        state.submit(pose,submitNodeStorage,LightCoordsUtil.FULL_BRIGHT,OverlayTexture.NO_OVERLAY,0);
        featureRenderDispatcher.renderAllFeatures();
    }

    @Override
    protected String getTextureLabel() { return "lightmanscurrency:block_state"; }

}