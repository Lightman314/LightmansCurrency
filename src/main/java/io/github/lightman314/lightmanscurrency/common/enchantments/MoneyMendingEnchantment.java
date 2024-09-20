package io.github.lightman314.lightmanscurrency.common.enchantments;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.common.core.ModEnchantments;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import javax.annotation.Nonnull;

@SuppressWarnings("removal")
public class MoneyMendingEnchantment extends Enchantment {

	public MoneyMendingEnchantment(Rarity rarity, EquipmentSlot... slots) { super(rarity, EnchantmentCategory.BREAKABLE, slots); }

	@Override
	public int getMinCost(int level) { return level * 25; }

	@Override
	public int getMaxCost(int level) { return this.getMinCost(level) + 50; }

	@Override
	public boolean isTreasureOnly() { return true; }

	@Override
	public int getMaxLevel() { return 1; }

	@Override
	protected boolean checkCompatibility(@Nonnull Enchantment otherEnchant) {
		return otherEnchant != Enchantments.MENDING && super.checkCompatibility(otherEnchant);
	}

	public static MoneyValue getRepairCost(@Nonnull ItemStack item)
	{
		MoneyValue baseCost = LCConfig.SERVER.moneyMendingRepairCost.get();
		MoneyValue cost = baseCost;
		Map<Enchantment,Integer> enchantments = item.getAllEnchantments();
		if(enchantments.getOrDefault(Enchantments.INFINITY_ARROWS,0) > 0)
			cost = baseCost.addValue(LCConfig.SERVER.moneyMendingInfinityCost.get());
		return cost == null ? baseCost : cost;
	}
	
	public static void runEntityTick(@Nonnull LivingEntity entity, @Nonnull IMoneyHandler handler)
	{
		//Go through the players inventory searching for items with the money mending enchantment
		Entry<EquipmentSlot,ItemStack> entry = EnchantmentHelper.getRandomItemWith(ModEnchantments.MONEY_MENDING.get(), entity, ItemStack::isDamaged);
		ItemStack item;
		if(entry == null)
		{
			//If we failed to get a vanilla entry, check the curios slots (if applicable)
			if(LCCurios.isLoaded())
				item = LCCurios.getRandomItem(entity,i -> EnchantmentHelper.getEnchantments(i).containsKey(ModEnchantments.MONEY_MENDING.get()));
			else
				item = null;
		}
		else
			item = entry.getValue();
		if(item != null)
		{
			//Only bother calculating the repair cost until we have a confirmed mending target to reduce lag
			//That and the cost can now be modified based on other enchantments such as infinity, etc.
			MoneyValue repairCost = getRepairCost(item);
			MoneyView availableFunds = handler.getStoredMoney();
			if(!availableFunds.containsValue(repairCost))
				return;

			//Repair the item
			MoneyValue nextCost = repairCost;
			MoneyValue finalCost = MoneyValue.empty();
			int currentDamage = item.getDamageValue();
			int repairAmount = 0;
			while (availableFunds.containsValue(nextCost) && repairAmount < currentDamage) {
				repairAmount++;
				finalCost = nextCost;
				nextCost = nextCost.addValue(repairCost);
			}
			//Take the payment from the player
			if(handler.extractMoney(finalCost, true).isEmpty())
			{
				//If payment was successful, repair the item
				handler.extractMoney(finalCost, false);
				item.setDamageValue(currentDamage - repairAmount);
			}
		}
	}

	public static void addEnchantmentTooltip(@Nonnull ItemStack stack, @Nonnull List<Component> tooltip)
	{
		Map<Enchantment,Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
		if(enchantments.getOrDefault(ModEnchantments.MONEY_MENDING.get(),0) > 0)
			tooltip.add(LCText.TOOLTIP_MONEY_MENDING_COST.get(MoneyMendingEnchantment.getRepairCost(stack).getText().withStyle(ChatFormatting.YELLOW,ChatFormatting.BOLD)));
	}
	
}
