package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.FreezerTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.FreezerBlock;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

public class FreezerTraderBlockEntityRenderer implements BlockEntityRenderer<FreezerTraderBlockEntity>{
	
	public FreezerTraderBlockEntityRenderer(BlockEntityRendererProvider.Context ignored) { }
	
	@Override
	public void render(@Nonnull FreezerTraderBlockEntity tileEntity, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int lightLevel, int id)
	{
		
		//Render the items using the default method
		ItemTraderBlockEntityRenderer.renderItems(tileEntity, partialTicks, poseStack, bufferSource, lightLevel, id);
		
		//Render the door

		if(tileEntity.getBlockState().getBlock() instanceof FreezerBlock freezerBlock)
		{
			poseStack.pushPose();

			Direction facing = freezerBlock.getFacing(tileEntity.getBlockState());
			Vector3f corner = IRotatableBlock.getOffsetVect(facing);
			Vector3f right = IRotatableBlock.getRightVect(facing);
			Vector3f forward = IRotatableBlock.getForwardVect(facing);

			//Calculate the hinge position
			Vector3f hinge = MathUtil.VectorAdd(corner, MathUtil.VectorMult(right, 15.5f/16f), MathUtil.VectorMult(forward, 3.5f/16f));

			Quaternionf rotation = MathUtil.fromAxisAngleDegree(MathUtil.getYP(), facing.get2DDataValue() * -90f + (90f * tileEntity.getDoorAngle(partialTicks)));

			poseStack.translate(hinge.x(), hinge.y(), hinge.z());
			poseStack.mulPose(rotation);

			Minecraft mc = Minecraft.getInstance();
			BakedModel model = mc.getModelManager().getModel(ModelResourceLocation.standalone(freezerBlock.getDoorModel()));
			ItemRenderer itemRenderer = mc.getItemRenderer();
			itemRenderer.render(new ItemStack(freezerBlock), ItemDisplayContext.FIXED, false, poseStack, bufferSource, lightLevel, OverlayTexture.NO_OVERLAY, model);

			poseStack.popPose();
		}

	}
	
}
