package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.FreezerTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.FreezerBlock;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nonnull;

public class FreezerTraderBlockEntityRenderer extends TileEntityRenderer<FreezerTraderBlockEntity> {

	public FreezerTraderBlockEntityRenderer(TileEntityRendererDispatcher dispatcher) { super(dispatcher); }

	@Override
	public void render(@Nonnull FreezerTraderBlockEntity tileEntity, float partialTicks, @Nonnull MatrixStack pose, @Nonnull IRenderTypeBuffer buffer, int lightLevel, int id)
	{

		//Render the items using the default method
		ItemTraderBlockEntityRenderer.renderItems(tileEntity, partialTicks, pose, buffer, lightLevel, id);

		//Render the door

		if(tileEntity.getBlockState().getBlock() instanceof FreezerBlock)
		{
			FreezerBlock freezerBlock = (FreezerBlock)tileEntity.getBlockState().getBlock();
			pose.pushPose();

			Direction facing = freezerBlock.getFacing(tileEntity.getBlockState());
			Vector3f corner = IRotatableBlock.getOffsetVect(facing);
			Vector3f right = IRotatableBlock.getRightVect(facing);
			Vector3f forward = IRotatableBlock.getForwardVect(facing);

			//Calculate the hinge position
			Vector3f hinge = MathUtil.VectorAdd(corner, MathUtil.VectorMult(right, 15.5f/16f), MathUtil.VectorMult(forward, 3.5f/16f));

			Quaternion rotation = Vector3f.YP.rotationDegrees(facing.get2DDataValue() * -90f + (90f * tileEntity.getDoorAngle(partialTicks)));

			pose.translate(hinge.x(), hinge.y(), hinge.z());
			pose.mulPose(rotation);

			//Attempt at rendering the door model without creating a freezer door item.
			Minecraft mc = Minecraft.getInstance();
			IBakedModel model = mc.getModelManager().getModel(freezerBlock.getDoorModel());
			ItemRenderer itemRenderer = mc.getItemRenderer();
			itemRenderer.render(new ItemStack(freezerBlock), ItemCameraTransforms.TransformType.FIXED, false, pose, buffer, lightLevel, OverlayTexture.NO_OVERLAY, model);

			pose.popPose();
		}

	}

}