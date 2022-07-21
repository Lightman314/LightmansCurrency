package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import io.github.lightman314.lightmanscurrency.blockentity.FreezerTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class FreezerTraderBlockEntityRenderer implements BlockEntityRenderer<FreezerTraderBlockEntity>{

	public static final Item doorItem = ModItems.FREEZER_DOOR.get();
	
	public FreezerTraderBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }
	
	@Override
	public void render(FreezerTraderBlockEntity tileEntity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int lightLevel, int id)
	{
		
		//Render the items using the default method
		ItemTraderBlockEntityRenderer.renderItems(tileEntity, partialTicks, poseStack, bufferSource, lightLevel, id);
		
		//Render the door
		poseStack.pushPose();
		Vector3f corner = new Vector3f(0f,0f,0f);
		Vector3f right = new Vector3f(1f, 0f, 0f);
		Vector3f forward = new Vector3f(0f, 0f, 1f);
		Block freezerBlock = tileEntity.getBlockState().getBlock();
		Direction facing = Direction.SOUTH;
		if(freezerBlock instanceof IRotatableBlock)
		{
			IRotatableBlock block = (IRotatableBlock)freezerBlock;
			facing = block.getFacing(tileEntity.getBlockState());
			corner = IRotatableBlock.getOffsetVect(facing);
			right = IRotatableBlock.getRightVect(facing);
			forward = IRotatableBlock.getForwardVect(facing);
		}
		//Calculate the hinge position
		Vector3f hinge = MathUtil.VectorAdd(corner, MathUtil.VectorMult(right, 15.5f/16f), MathUtil.VectorMult(forward, 3.5f/16f));
		
		Quaternion rotation = Vector3f.YP.rotationDegrees(facing.get2DDataValue() * -90f + (90f * tileEntity.getDoorAngle(partialTicks)));
		
		poseStack.translate(hinge.x(), hinge.y(), hinge.z());
		poseStack.mulPose(rotation);
		
		ItemStack stack = new ItemStack(doorItem);
		Minecraft.getInstance().getItemRenderer().renderStatic(stack, TransformType.FIXED, lightLevel, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, id);
		
		poseStack.popPose();
		
	}
	
}
