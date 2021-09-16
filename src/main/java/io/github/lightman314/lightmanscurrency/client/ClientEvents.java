package io.github.lightman314.lightmanscurrency.client;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.WalletScreen;
import io.github.lightman314.lightmanscurrency.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageOpenWallet;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.CurrencySoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientEvents {

	
	public static final KeyBinding KEY_WALLET = new KeyBinding("key.wallet", GLFW.GLFW_KEY_V, "key.categories.inventory");
	
	@SubscribeEvent
	public void onKeyInput(InputEvent.KeyInputEvent event)
	{
		Minecraft minecraft = Minecraft.getInstance();
		if(minecraft.currentScreen instanceof WalletScreen)
		{
			if(event.getAction() == GLFW.GLFW_PRESS && event.getKey() == KEY_WALLET.getKey().getKeyCode())
			{
				minecraft.player.closeScreen();
			}
		}
		else if(minecraft.player != null && minecraft.currentScreen == null)
		{
			ClientPlayerEntity player = minecraft.player;
			if(KEY_WALLET.isPressed())
			{
				if(!LightmansCurrency.getWalletStack(player).isEmpty())
				{
					LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenWallet());
					
					minecraft.getSoundHandler().play(SimpleSound.master(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 1.25f + player.world.rand.nextFloat() * 0.5f, 0.75f));
					
					ItemStack wallet = LightmansCurrency.getWalletStack(player);
					if(!WalletItem.isEmpty(wallet))
						minecraft.getSoundHandler().play(SimpleSound.master(CurrencySoundEvents.COINS_CLINKING, 1f, 0.4f));
				}
			}
		}
	}
	
	//Item Tooltip event for special lore for item trader items
	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event)
	{
		ItemStack item = event.getItemStack();
		if(item.hasTag())
		{
			CompoundNBT itemTag = item.getTag();
			if(itemTag.contains("LC_DisplayItem") && itemTag.getBoolean("LC_DisplayItem"))
			{
				
				List<ITextComponent> addedTags = new ArrayList<>();
				
				//Add original name info
				if(itemTag.contains("LC_CustomName", Constants.NBT.TAG_STRING))
				{
					ITextComponent originalName = event.getToolTip().get(0);
					event.getToolTip().set(0, new StringTextComponent("§6" + itemTag.getString("LC_CustomName")));
					addedTags.add(new TranslationTextComponent("tooltip.lightmanscurrency.trader.originalname", originalName));
				}
				//Add stock info
				if(itemTag.contains("LC_StockAmount", Constants.NBT.TAG_INT))
				{
					int stockAmount = itemTag.getInt("LC_StockAmount");
					addedTags.add(new TranslationTextComponent("tooltip.lightmanscurrency.trader.stock", stockAmount >= 0 ? new StringTextComponent("§6" + stockAmount) : new TranslationTextComponent("tooltip.lightmanscurrency.trader.stock.infinite")));
				}
				
				if(addedTags.size() > 0)
				{
					event.getToolTip().add(new TranslationTextComponent("tooltip.lightmanscurrency.trader.info"));
					addedTags.forEach(tag -> event.getToolTip().add(tag));
				}
			}
		}
	}
	
}
