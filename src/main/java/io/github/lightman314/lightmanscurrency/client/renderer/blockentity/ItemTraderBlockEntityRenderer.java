package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.ItemPositionData;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models.CustomModelDataManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.ModelVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperties;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ItemTraderBlockEntityRenderer implements BlockEntityRenderer<ItemTraderBlockEntity> {

	private ItemTraderBlockEntityRenderer() { }

	public static ItemTraderBlockEntityRenderer create(BlockEntityRendererProvider.Context context) { return new ItemTraderBlockEntityRenderer(); }

	@Override
	public void render(@Nonnull ItemTraderBlockEntity blockEntity, float partialTicks, @Nonnull PoseStack pose, @Nonnull MultiBufferSource buffer, int lightLevel, int id)
	{
		renderItems(blockEntity, partialTicks, pose, buffer, lightLevel, id);
	}

	public static List<ItemStack> GetRenderItems(ItemTradeData trade) {
		List<ItemStack> result = new ArrayList<>();
		for(int i = 0; i < 2; ++i)
		{
			ItemStack item = trade.getSellItem(i);
			if(!item.isEmpty())
				result.add(item);
		}
		return result;
	}

	public static void renderItems(ItemTraderBlockEntity blockEntity, float partialTicks, PoseStack pose, MultiBufferSource buffer, int lightLevel, int overlay)
	{
		try{
			TraderData rawTrader = blockEntity.getRawTraderData();
			if(!(rawTrader instanceof ItemTraderData trader))
				return;
			ItemPositionData positionData = blockEntity.GetRenderData();
			//Get custom position data from the Model Variant
			ModelVariant variant = ModelVariantDataManager.getVariant(blockEntity.getCurrentVariant());
			if(variant != null && variant.has(VariantProperties.ITEM_POSITION_DATA))
				positionData = Objects.requireNonNullElse(variant.get(VariantProperties.ITEM_POSITION_DATA).get(),positionData);
			if(positionData.isEmpty())
				return;
			final int maxIndex = positionData.getEntryCount();
			final int renderLimit = LCConfig.CLIENT.itemRenderLimit.get();
			BlockState state = blockEntity.getBlockState();
			ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
			Level level = blockEntity.getLevel();
			BlockPos blockPos = blockEntity.getBlockPos();
			int blockLight = level.getBrightness(LightLayer.BLOCK,blockPos);
			int skyLight = level.getBrightness(LightLayer.SKY,blockPos);
			for(int tradeSlot = 0; tradeSlot < trader.getTradeCount() && tradeSlot < maxIndex; tradeSlot++)
			{

				ItemTradeData trade = trader.getTrade(tradeSlot);
				List<ItemStack> renderItems = GetRenderItems(trade);
				if(!renderItems.isEmpty())
				{

					//Get positions
					List<Vector3f> positions = positionData.getPositions(state, tradeSlot);

					//Get rotation
					List<Quaternionf> rotation = positionData.getRotation(state, tradeSlot, partialTicks);

					int minLight = positionData.getMinLight(tradeSlot);
					int itemLight = lightLevel;
					if(blockLight < minLight)
						itemLight = LightTexture.pack(Math.min(minLight,level.getMaxLightLevel()),skyLight);

					//Get scale
					float scale = positionData.getScale(tradeSlot);

					for(int pos = 0; pos < renderLimit && pos < positions.size() && pos < trader.getTradeStock(tradeSlot); pos++)
					{

						pose.pushPose();

						Vector3f position = positions.get(pos);

						//Translate, rotate, and scale the matrix stack
						pose.translate(position.x(), position.y(), position.z());
						for(Quaternionf rot : rotation)
						{
							pose.mulPose(rot);
						}
						pose.scale(scale, scale, scale);

						//Render the item
						if(renderItems.size() > 1)
						{
							//Render first item
							pose.pushPose();

							//Don't base translation off of scale, as we've already been scaled down.
							pose.translate(0.25, 0.25, 0d);
							pose.scale(0.5f, 0.5f, 0.5f);

							renderItem(blockEntity,itemRenderer,renderItems.get(0),itemLight,pose,buffer,overlay);

							pose.popPose();

							//Render second item
							pose.pushPose();

							//Slightly offset in the Z to prevent z-fighting if there's an overlap
							pose.translate(-0.25, -0.25, 0.001d);
							pose.scale(0.5f, 0.5f, 0.5f);

							renderItem(blockEntity,itemRenderer,renderItems.get(1),itemLight,pose,buffer,overlay);

							pose.popPose();
						}
						else
							renderItem(blockEntity,itemRenderer,renderItems.get(0),itemLight,pose,buffer,overlay);

						pose.popPose();
					}
				}
			}
		} catch(Throwable t) { LightmansCurrency.LogError("Error rendering an Item Trader!", t); }
	}

	private static void renderItem(ItemTraderBlockEntity be, ItemRenderer renderer, ItemStack item, int lightLevel, PoseStack pose, MultiBufferSource buffer, int id)
	{
		//Get custom scale for the item
		float scale = LCConfig.CLIENT.itemScaleOverrides.get().getCustomScale(item);
		pose.scale(scale,scale,scale);
		//Check for custom model for the item
		ModelResourceLocation customModel = CustomModelDataManager.getCustomModel(be,item);
		if(customModel == null)
			renderer.renderStatic(item, ItemDisplayContext.FIXED, lightLevel, OverlayTexture.NO_OVERLAY, pose, buffer, be.getLevel(), id);
		else
		{
			BakedModel model = Minecraft.getInstance().getModelManager().getModel(customModel);
			renderer.render(item, ItemDisplayContext.FIXED, false, pose, buffer, lightLevel, OverlayTexture.NO_OVERLAY, model);
		}
	}

	private static long rotationTime = 0;
	public static long getRotationTime() { return rotationTime; }
	public static Quaternionf getRotation(float partialTicks) { return new Quaternionf().fromAxisAngleDeg(new Vector3f(0f, 1f,0f), (ItemTraderBlockEntityRenderer.getRotationTime() + partialTicks) * 2.0F); }

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if(event.phase == TickEvent.Phase.START)
			rotationTime++;
	}
}