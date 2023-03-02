package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.FreezerTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.FreezerBlock;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FreezerTraderBlockEntityRenderer implements BlockEntityRenderer<FreezerTraderBlockEntity>{

	//public static final Item doorItem = ModItems.FREEZER_DOOR.get();

	public FreezerTraderBlockEntityRenderer(BlockEntityRendererProvider.Context ignored) { }

	@Override
	public void render(@NotNull FreezerTraderBlockEntity tileEntity, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int lightLevel, int id)
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

			Quaternion rotation = Vector3f.YP.rotationDegrees(facing.get2DDataValue() * -90f + (90f * tileEntity.getDoorAngle(partialTicks)));

			poseStack.translate(hinge.x(), hinge.y(), hinge.z());
			poseStack.mulPose(rotation);

			//Attempt at rendering the door model without creating a freezer door item.
			Minecraft mc = Minecraft.getInstance();
			BakedModel model = mc.getModelManager().getModel(freezerBlock.getDoorModel());
			ItemRenderer itemRenderer = mc.getItemRenderer();
			itemRenderer.render(new ItemStack(freezerBlock), TransformType.FIXED, false, poseStack, bufferSource, lightLevel, OverlayTexture.NO_OVERLAY, model);

			poseStack.popPose();
		}

	}

}