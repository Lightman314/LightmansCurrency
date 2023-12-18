package io.github.lightman314.lightmanscurrency.common.enchantments;

import java.util.Map.Entry;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.common.core.ModEnchantments;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import javax.annotation.Nonnull;

@SuppressWarnings("removal")
public class MoneyMendingEnchantment extends Enchantment {

	public MoneyMendingEnchantment(Rarity rarity, EquipmentSlot... slots) { super(rarity, EnchantmentCategory.BREAKABLE, slots); }
	
	public int getMinCost(int level) { return level * 25; }

	public int getMaxCost(int level) { return this.getMinCost(level) + 50; }

	public boolean isTreasureOnly() { return true; }

	public int getMaxLevel() { return 1; }
	
	protected boolean checkCompatibility(@Nonnull Enchantment otherEnchant) {
		return otherEnchant != Enchantments.MENDING && super.checkCompatibility(otherEnchant);
	}
	
	public static MoneyValue getRepairCost() { return Config.SERVER.moneyMendingCoinCost.get(); }
	
	public static void runEntityTick(Player player)
	{
		MoneyValue repairCost = MoneyMendingEnchantment.getRepairCost();
		if(!MoneyAPI.canPlayerAfford(player, repairCost))
			return;
		//Go through the players inventory searching for items with the money mending enchantment
		Entry<EquipmentSlot,ItemStack> entry = EnchantmentHelper.getRandomItemWith(ModEnchantments.MONEY_MENDING.get(), player, ItemStack::isDamaged);
		ItemStack item;
		if(entry == null)
		{
			if(LightmansCurrency.isCuriosValid(player))
				item = LCCurios.getMoneyMendingItem(player);
			else
				item = null;
		}
		else
			item = entry.getValue();
		if(item != null)
		{
			//Repair the item
			MoneyValue nextCost = repairCost;
			MoneyValue finalCost = MoneyValue.empty();
			int currentDamage = item.getDamageValue();
			int repairAmount = 0;
			while (MoneyAPI.canPlayerAfford(player, nextCost) && repairAmount < currentDamage) {
				repairAmount++;
				finalCost = nextCost;
				nextCost = nextCost.addValue(repairCost);
			}
			//Take the payment from the player
			if (MoneyAPI.takeMoneyFromPlayer(player, finalCost))
			{
				//If payment was successful, repair the item
				item.setDamageValue(currentDamage - repairAmount);
			}
		}
	}
	
}
