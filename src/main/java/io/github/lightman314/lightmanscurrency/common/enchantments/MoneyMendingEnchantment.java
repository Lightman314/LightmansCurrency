package io.github.lightman314.lightmanscurrency.common.enchantments;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.core.ModEnchantments;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menus.wallet.WalletMenuBase;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.enchantments.SPacketMoneyMendingClink;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

@SuppressWarnings("removal")
public class MoneyMendingEnchantment extends Enchantment {

	public MoneyMendingEnchantment(Rarity rarity, EquipmentSlotType... slots) { super(rarity, EnchantmentType.BREAKABLE, slots); }

	public int getMinCost(int level) { return level * 25; }

	public int getMaxCost(int level) { return this.getMinCost(level) + 50; }

	public boolean isTreasureOnly() { return true; }

	public int getMaxLevel() { return 1; }

	protected boolean checkCompatibility(@Nonnull Enchantment otherEnchant) {
		return otherEnchant != Enchantments.MENDING && super.checkCompatibility(otherEnchant);
	}

	public static long getRepairCost() { return Config.SERVER.moneyMendingCoinCost.get().getRawValue(); }

	public static void runEntityTick(LivingEntity entity)
	{
		IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(entity);
		if(walletHandler != null)
		{
			ItemStack wallet = walletHandler.getWallet();
			if(WalletItem.isWallet(wallet))
			{
				NonNullList<ItemStack> walletInventory = WalletItem.getWalletInventory(wallet);
				long currentWalletValue = MoneyUtil.getValue(walletInventory);
				final long repairCost = MoneyMendingEnchantment.getRepairCost();
				if(repairCost > currentWalletValue)
					return;
				//Go through the players inventory searching for items with the money mending enchantment
				Map.Entry<EquipmentSlotType,ItemStack> entry = EnchantmentHelper.getRandomItemWith(ModEnchantments.MONEY_MENDING.get(), entity, ItemStack::isDamaged);
				if(entry != null)
				{
					//Repair the item
					ItemStack item = entry.getValue();
					int currentDamage = item.getDamageValue();
					long repairAmount = Math.min(currentDamage, currentWalletValue / repairCost);
					item.setDamageValue(currentDamage - (int)repairAmount);
					currentWalletValue -= repairAmount * repairCost;
					//Remove the coins from the players inventory
					Inventory newWalletInventory = new Inventory(walletInventory.size());
					for(ItemStack coinStack : MoneyUtil.getCoinsOfValue(currentWalletValue))
					{
						AtomicReference<ItemStack> leftovers = new AtomicReference<>(InventoryUtil.TryPutItemStack(newWalletInventory, coinStack));
						if(!leftovers.get().isEmpty())
						{
							if(entity instanceof PlayerEntity)
							{
								//Force the extra coins into the players inventory
								ItemHandlerHelper.giveItemToPlayer((PlayerEntity)entity, leftovers.get());
							}
							else
							{
								//Put the extra coins in the entities inventory
								entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(entityInventory -> leftovers.set(ItemHandlerHelper.insertItemStacked(entityInventory, leftovers.get(), false)));
								//If no inventory, or not enough room, force the extra coins into the world
								if(!leftovers.get().isEmpty())
									InventoryUtil.dumpContents(entity.level, entity.blockPosition(), leftovers.get());
							}
						}

					}
					WalletItem.putWalletInventory(wallet, InventoryUtil.buildList(newWalletInventory));
					walletHandler.setWallet(wallet);
					if(entity instanceof PlayerEntity)
					{
						PlayerEntity player = (PlayerEntity)entity;
						//Reload the wallets contents if the wallet menu is open.
						if(player.containerMenu instanceof WalletMenuBase)
						{
							WalletMenuBase menu = (WalletMenuBase)player.containerMenu;
							menu.reloadWalletContents();
						}

						//Send Money Mending clink message
						LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new SPacketMoneyMendingClink());

					}
				}
			}
		}
	}

}