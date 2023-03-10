package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ItemTraderBlockEntityRenderer extends TileEntityRenderer<ItemTraderBlockEntity> {
	
	public ItemTraderBlockEntityRenderer(TileEntityRendererDispatcher dispatcher) { super(dispatcher); }
	
	@Override
	public void render(@Nonnull ItemTraderBlockEntity blockEntity, float partialTicks, @Nonnull MatrixStack pose, @Nonnull IRenderTypeBuffer buffer, int lightLevel, int id)
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
	
	public static void renderItems(ItemTraderBlockEntity blockEntity, float partialTicks, MatrixStack pose, IRenderTypeBuffer buffer, int lightLevel, int id)
	{
		ItemTraderData trader = blockEntity.getTraderData();
		if(trader == null)
			return;
		for(int tradeSlot = 0; tradeSlot < trader.getTradeCount() && tradeSlot < blockEntity.maxRenderIndex(); tradeSlot++)
		{
			
			ItemTradeData trade = trader.getTrade(tradeSlot);
			List<ItemStack> renderItems = GetRenderItems(trade);
			if(renderItems.size() > 0)
			{
				
				ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
				
				//Get positions
				List<Vector3f> positions = blockEntity.GetStackRenderPos(tradeSlot, renderItems.size() > 1);
				
				//Get rotation
				List<Quaternion> rotation = blockEntity.GetStackRenderRot(tradeSlot, partialTicks);
				
				//Get scale
				float scale = blockEntity.GetStackRenderScale(tradeSlot);

				for(int pos = 0; pos < Config.CLIENT.itemRenderLimit.get() && pos < positions.size() && pos < trader.getTradeStock(tradeSlot); pos++)
				{
					
					pose.pushPose();
					
					Vector3f position = positions.get(pos);
					
					//Translate, rotate, and scale the matrix stack
					pose.translate(position.x(), position.y(), position.z());
					for(Quaternion rot : rotation)
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
						
						itemRenderer.renderStatic(renderItems.get(0), ItemCameraTransforms.TransformType.FIXED, lightLevel, id, pose, buffer);
						
						pose.popPose();
						
						//Render second item
						pose.pushPose();
						
						//Slightly offset in the Z to prevent z-fighting if there's an overlap
						pose.translate(-0.25, -0.25, 0.001d);
						pose.scale(0.5f, 0.5f, 0.5f);
						
						itemRenderer.renderStatic(renderItems.get(1),  ItemCameraTransforms.TransformType.FIXED, lightLevel, id, pose, buffer);
						
						pose.popPose();
					}
					else
						itemRenderer.renderStatic(renderItems.get(0),  ItemCameraTransforms.TransformType.FIXED, lightLevel, id, pose, buffer);
				
					pose.popPose();
					
				}
				
			}
			
		}
	}

	private static long rotationTime = 0;
	public static long getRotationTime() { return rotationTime; }
	public static Quaternion getRotation(float partialTicks) { return Vector3f.YP.rotationDegrees( (ItemTraderBlockEntityRenderer.getRotationTime() + partialTicks) * 2.0F); }

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if(event.phase == TickEvent.Phase.START)
			rotationTime++;
	}

	
}