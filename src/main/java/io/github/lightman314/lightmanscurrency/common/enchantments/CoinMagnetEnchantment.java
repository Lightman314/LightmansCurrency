package io.github.lightman314.lightmanscurrency.common.enchantments;

import java.util.List;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.common.core.ModEnchantments;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;

public class CoinMagnetEnchantment {
	
	public static void runEntityTick(@Nonnull WalletHandler walletHandler, @Nonnull LivingEntity entity) {
		if(entity.isSpectator())
			return;
		ItemStack wallet = walletHandler.getWallet();
		//Don't do anything if the stack is not a waller
		//Or if the wallet cannot pick up coins
		if(!WalletItem.isWallet(wallet) || !WalletItem.CanPickup((WalletItem)wallet.getItem()))
			return;
		//Get the level (-1 to properly calculate range)
		Holder<Enchantment> cmEnchant = LookupHelper.lookupEnchantment(entity.registryAccess(),ModEnchantments.COIN_MAGNET);
		int enchantLevel = cmEnchant != null ? wallet.getEnchantmentLevel(cmEnchant) : 0;

		//Don't do anything if the Coin Magnet enchantment is not present.
		if(enchantLevel <= 0)
			return;
		//Calculate the search radius
		float range = getCollectionRange(enchantLevel);
		Level level = entity.level();

		AABB searchBox = new AABB(entity.xo - range, entity.yo - range, entity.zo - range, entity.xo + range, entity.yo + range, entity.zo + range);
		boolean updateWallet = false;
		for(Entity e : level.getEntities(entity, searchBox, e -> coinMagnetEntityFilter(e,entity)))
		{
			ItemEntity ie = (ItemEntity)e;
			ItemStack coinStack = ie.getItem();
			ItemStack leftovers = WalletItem.PickupCoin(wallet, coinStack);
			if(!InventoryUtil.ItemsFullyMatch(leftovers, coinStack))
			{
				updateWallet = true;
				if(leftovers.isEmpty())
					ie.discard();
				else
					ie.setItem(leftovers);

				WalletItem.playCollectSound(entity,wallet);

			}
		}
		if(updateWallet)
		{
			walletHandler.setWallet(wallet);
			WalletMenuBase.OnWalletUpdated(entity);
		}
	}

	public static boolean coinMagnetEntityFilter(Entity entity, @Nonnull LivingEntity potentialPickup) {
		if(entity instanceof ItemEntity item)
		{
			//Deny if the item is reserved for a given player/entity
			if(item.getTarget() != null && !item.getTarget().equals(potentialPickup.getUUID()))
				return false;
			return CoinAPI.API.IsAllowedInCoinContainer(item.getItem(), false);
		}
		return false;
	}
	
	public static float getCollectionRange(int enchantLevel) {
		enchantLevel -= 1;
		if(enchantLevel < 0)
			return 0f;
		return LCConfig.SERVER.coinMagnetBaseRange.get() + (LCConfig.SERVER.coinMagnetLeveledRange.get() * Math.min(enchantLevel, LCConfig.SERVER.coinMagnetCalculationCap.get()));
	}
	
	public static Component getCollectionRangeDisplay(int enchantLevel) {
		float range = getCollectionRange(enchantLevel);
		String display = range % 1f > 0f ? String.valueOf(range) : String.valueOf(Math.round(range));
		return Component.literal(display).withStyle(ChatFormatting.GREEN);
	}

	public static void addWalletTooltips(List<Component> tooltips, int enchantLevel, ItemStack wallet) {
		if(wallet.getItem() instanceof WalletItem)
		{
			if(enchantLevel > 0 && WalletItem.CanPickup((WalletItem)wallet.getItem()))
			{
				tooltips.add(LCText.TOOLTIP_WALLET_PICKUP_MAGNET.get(getCollectionRangeDisplay(enchantLevel)).withStyle(ChatFormatting.YELLOW));
			}
		}
	}
	
}
