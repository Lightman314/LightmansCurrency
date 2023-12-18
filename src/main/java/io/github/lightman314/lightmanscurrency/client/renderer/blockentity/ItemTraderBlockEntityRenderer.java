package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.ItemPositionData;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ItemTraderBlockEntityRenderer implements BlockEntityRenderer<ItemTraderBlockEntity>{

	public ItemTraderBlockEntityRenderer(BlockEntityRendererProvider.Context ignored) { }
	
	@Override
	public void render(@NotNull ItemTraderBlockEntity blockEntity, float partialTicks, @NotNull PoseStack pose, @NotNull MultiBufferSource buffer, int lightLevel, int id)
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

	@SuppressWarnings("deprecation")
	public static void renderItems(ItemTraderBlockEntity blockEntity, float partialTicks, PoseStack pose, MultiBufferSource buffer, int lightLevel, int id)
	{
		try{
			TraderData rawTrader = blockEntity.getRawTraderData();
			if(!(rawTrader instanceof ItemTraderData trader))
				return;
			ItemPositionData positionData = blockEntity.GetRenderData();
			final int maxIndex = positionData.isEmpty() ? blockEntity.maxRenderIndex() : positionData.getEntryCount();
			final int renderLimit = Config.CLIENT.itemRenderLimit.get();
			BlockState state = blockEntity.getBlockState();
			ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
			for(int tradeSlot = 0; tradeSlot < trader.getTradeCount() && tradeSlot < maxIndex; tradeSlot++)
			{

				ItemTradeData trade = trader.getTrade(tradeSlot);
				List<ItemStack> renderItems = GetRenderItems(trade);
				if(renderItems.size() > 0)
				{

					//Get positions
					List<Vector3f> positions;
					if(positionData.isEmpty())
						positions = blockEntity.GetStackRenderPos(tradeSlot, renderItems.size() > 1);
					else
						positions = positionData.getPositions(state, tradeSlot);

					//Get rotation
					List<Quaternionf> rotation;
					if(positionData.isEmpty())
						rotation = blockEntity.GetStackRenderRot(tradeSlot, partialTicks);
					else
						rotation = positionData.getRotation(state, tradeSlot, partialTicks);

					//Get scale
					float scale;
					if(positionData.isEmpty())
						scale = blockEntity.GetStackRenderScale(tradeSlot);
					else
						scale = positionData.getScale(tradeSlot);

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

							itemRenderer.renderStatic(renderItems.get(0), ItemDisplayContext.FIXED, lightLevel, OverlayTexture.NO_OVERLAY, pose, buffer, blockEntity.getLevel(), id);

							pose.popPose();

							//Render second item
							pose.pushPose();

							//Slightly offset in the Z to prevent z-fighting if there's an overlap
							pose.translate(-0.25, -0.25, 0.001d);
							pose.scale(0.5f, 0.5f, 0.5f);

							itemRenderer.renderStatic(renderItems.get(1), ItemDisplayContext.FIXED, lightLevel, OverlayTexture.NO_OVERLAY, pose, buffer, blockEntity.getLevel(), id);

							pose.popPose();
						}
						else
							itemRenderer.renderStatic(renderItems.get(0), ItemDisplayContext.FIXED, lightLevel, OverlayTexture.NO_OVERLAY, pose, buffer, blockEntity.getLevel(), id);

						pose.popPose();
					}
				}
			}
		} catch(Throwable t) { LightmansCurrency.LogError("Error rendering an Item Trader!", t); }
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
