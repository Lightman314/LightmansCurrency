package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.ModelVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.models.VariantModelLocation;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperties;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.builtin.FreezerDoorData;
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
import net.minecraft.world.level.block.Block;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

public class FreezerTraderBlockEntityRenderer implements BlockEntityRenderer<FreezerTraderBlockEntity>{
	
	private FreezerTraderBlockEntityRenderer() { }

	public static FreezerTraderBlockEntityRenderer create(BlockEntityRendererProvider.Context ignored) { return new FreezerTraderBlockEntityRenderer(); }
	
	@Override
	public void render(@Nonnull FreezerTraderBlockEntity blockEntity, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int lightLevel, int id)
	{
		
		//Render the items using the default method
		ItemTraderBlockEntityRenderer.renderItems(blockEntity, partialTicks, poseStack, bufferSource, lightLevel, id);
		
		//Render the door

		if(blockEntity.getBlockState().getBlock() instanceof FreezerBlock freezerBlock)
		{
			poseStack.pushPose();

			Direction facing = freezerBlock.getFacing(blockEntity.getBlockState());
			Vector3f corner = IRotatableBlock.getOffsetVect(facing);
			Vector3f right = IRotatableBlock.getRightVect(facing);
			Vector3f forward = IRotatableBlock.getForwardVect(facing);

			FreezerDoorData doorData = FreezerDoorData.DEFAULT;
			ModelVariant variant = ModelVariantDataManager.getVariant(blockEntity.getCurrentVariant());
			if(variant != null && variant.getTargets().contains(freezerBlock.getBlockID()))
				doorData = variant.getOrDefault(VariantProperties.FREEZER_DOOR_DATA,FreezerDoorData.DEFAULT);

			//Calculate the hinge position
			Vector3f hinge = MathUtil.VectorAdd(corner, MathUtil.VectorMult(right, doorData.hingeX()), MathUtil.VectorMult(forward, doorData.hingeZ()));

			Quaternionf rotation = MathUtil.fromAxisAngleDegree(MathUtil.getYP(), facing.get2DDataValue() * -90f + (doorData.rotation() * blockEntity.getDoorAngle(partialTicks)));

			poseStack.translate(hinge.x(), hinge.y(), hinge.z());
			poseStack.mulPose(rotation);

			Minecraft mc = Minecraft.getInstance();
			ModelResourceLocation doorModel = ModelResourceLocation.standalone(freezerBlock.getDoorModel());
			BakedModel model = mc.getModelManager().getModel(doorModel);

			//Get custom freezer door model
			if(variant != null && variant.isValidTarget((Block)freezerBlock) && variant.getBlockModels().size() == freezerBlock.requiredModels())
			{
				VariantModelLocation newModel = VariantModelLocation.basic(blockEntity.getCurrentVariant(),freezerBlock.getBlockID(),freezerBlock.requiredModels() - 1);
				model = ModelVariantDataManager.getModel(newModel);
			}

			ItemRenderer itemRenderer = mc.getItemRenderer();
			itemRenderer.render(new ItemStack(freezerBlock), ItemDisplayContext.FIXED, false, poseStack, bufferSource, lightLevel, OverlayTexture.NO_OVERLAY, model);

			poseStack.popPose();
		}

	}
	
}
