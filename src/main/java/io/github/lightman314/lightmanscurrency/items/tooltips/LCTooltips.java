package io.github.lightman314.lightmanscurrency.items.tooltips;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.blockentity.CoinMintBlockEntity;
import io.github.lightman314.lightmanscurrency.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.crafting.CoinMintRecipe.MintType;
import io.github.lightman314.lightmanscurrency.items.TooltipItem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.NonNullSupplier;

public class LCTooltips {

	public static final NonNullSupplier<List<Component>> ATM = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.atm");
	public static final NonNullSupplier<List<Component>> TERMINAL = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.terminal");
	public static final NonNullSupplier<List<Component>> TICKET_MACHINE = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.ticketmachine");
	public static final NonNullSupplier<List<Component>> CASH_REGISTER = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.cashregister");
	
	public static final NonNullSupplier<List<Component>> COIN_MINT = () -> {
		List<Component> result = new ArrayList<>();
		if(canMint())
			result.add(new TranslatableComponent("tooltip.lightmanscurrency.coinmint.mintable").withStyle(TooltipItem.DEFAULT_STYLE));
		if(canMelt())
			result.add(new TranslatableComponent("tooltip.lightmanscurrency.coinmint.meltable").withStyle(TooltipItem.DEFAULT_STYLE));
		return result;
	};
	
	public static boolean canMint() {
		if(Config.SERVER.allowCoinMinting.get())
		{
			try {
				Minecraft mc = Minecraft.getInstance();
				Level level = mc.level;
				for(CoinMintRecipe recipe : CoinMintBlockEntity.getCoinMintRecipes(level))
				{
					if(recipe.getMintType() == MintType.MINT)
					{
						if(recipe.isValid())
							return true;
					}
				}
			} catch(Exception e) { return true; }
		}
		return false;
	}
	
	public static boolean canMelt() {
		if(Config.SERVER.allowCoinMelting.get())
		{
			//Check for a valid melt recipe
			try {
				Minecraft mc = Minecraft.getInstance();
				Level level = mc.level;
				for(CoinMintRecipe recipe : CoinMintBlockEntity.getCoinMintRecipes(level))
				{
					if(recipe.getMintType() == MintType.MELT)
					{
						if(recipe.isValid())
							return true;
					}
				}
			} catch(Exception e) { return true; }
		}
		return false;
	}
	
	public static final NonNullSupplier<List<Component>> ITEM_TRADER = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.item");
	public static final NonNullSupplier<List<Component>> ITEM_TRADER_ARMOR = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.item.armor");
	public static final NonNullSupplier<List<Component>> ITEM_TRADER_TICKET = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.item.ticket");
	public static final NonNullSupplier<List<Component>> ITEM_NETWORK_TRADER = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.trader.network.item");
	public static final NonNullSupplier<List<Component>> ITEM_TRADER_INTERFACE = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.interface.item");
	public static final NonNullSupplier<List<Component>> PAYGATE = () -> TooltipItem.getTooltipLines("tooltip.lightmanscurrency.paygate");
	
	
}
