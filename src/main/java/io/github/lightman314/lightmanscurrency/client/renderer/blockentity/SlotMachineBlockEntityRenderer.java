package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.ModelVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.models.VariantModelLocation;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.SlotMachineTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.SlotMachineBlock;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

public class SlotMachineBlockEntityRenderer implements BlockEntityRenderer<SlotMachineTraderBlockEntity> {

	private SlotMachineBlockEntityRenderer() { }

	public static SlotMachineBlockEntityRenderer create(BlockEntityRendererProvider.Context ignored) { return new SlotMachineBlockEntityRenderer(); }

	@Override
	public void render(@Nonnull SlotMachineTraderBlockEntity blockEntity, float partialTicks, @Nonnull PoseStack poseStack, @Nonnull MultiBufferSource bufferSource, int lightLevel, int overlay)
	{

		//Render the door
		if(blockEntity.getBlockState().getBlock() instanceof SlotMachineBlock block)
		{

			//LightmansCurrency.LogDebug("Light level is " + lightLevel);

			Minecraft mc = Minecraft.getInstance();

			ResourceLocation lightModel = block.getLightModel();

			ModelVariant variant = ModelVariantDataManager.getVariant(blockEntity.getCurrentVariant());
			BakedModel model = mc.getModelManager().getModel(lightModel);
			if(variant != null && variant.getTargets().contains(block.getBlockID()) && variant.getModels().size() == block.requiredModels())
			{
				VariantModelLocation newModel = VariantModelLocation.basic(blockEntity.getCurrentVariant(),block.getBlockID(),block.requiredModels() - 1);
				model = ModelVariantDataManager.getModel(newModel);
			}

			if(lightModel == null)
				return;

			poseStack.pushPose();

			Direction facing = block.getFacing(blockEntity.getBlockState());
			Vector3f corner = IRotatableBlock.getOffsetVect(facing);
			Vector3f right = IRotatableBlock.getRightVect(facing);
			Vector3f forward = IRotatableBlock.getForwardVect(facing);
			Vector3f offset = MathUtil.VectorAdd(corner, MathUtil.VectorMult(right, 0.5f), MathUtil.VectorMult(forward, 0.5f), new Vector3f(0f,0.5f,0f));

			poseStack.translate(offset.x, offset.y, offset.z);
			poseStack.mulPose(MathUtil.fromAxisAngleDegree(MathUtil.getYP(), facing.get2DDataValue() * -90f));

			ItemRenderer itemRenderer = mc.getItemRenderer();
			itemRenderer.render(new ItemStack(block), ItemDisplayContext.FIXED, false, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, model);

			poseStack.popPose();

		}

	}

}