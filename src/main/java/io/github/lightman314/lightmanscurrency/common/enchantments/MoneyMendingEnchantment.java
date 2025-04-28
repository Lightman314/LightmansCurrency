package io.github.lightman314.lightmanscurrency.common.enchantments;

import java.util.List;
import java.util.Optional;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.common.core.ModEnchantments;
import io.github.lightman314.lightmanscurrency.common.enchantments.data.RepairWithMoneyData;
import io.github.lightman314.lightmanscurrency.common.items.TooltipItem;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCurios;
import io.github.lightman314.lightmanscurrency.network.message.wallet.SPacketPlayCoinSound;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.*;

import javax.annotation.Nonnull;

public class MoneyMendingEnchantment {

	public static MoneyValue getRepairCost(@Nonnull ItemStack item, @Nonnull RepairWithMoneyData data, @Nonnull HolderLookup.Provider lookup)
	{
		MoneyValue baseCost = data.getBaseCost();

		ItemEnchantments enchantments = item.getItem() == Items.ENCHANTED_BOOK ? item.getOrDefault(DataComponents.STORED_ENCHANTMENTS,ItemEnchantments.EMPTY) : item.getAllEnchantments(lookup.lookupOrThrow(Registries.ENCHANTMENT));
		MoneyValue cost = data.getRepairCost(item,enchantments);

		return cost == null ? baseCost : cost;
	}
	
	public static void runEntityTick(@Nonnull LivingEntity entity, @Nonnull IMoneyHandler handler)
	{
		//Go through the players inventory searching for items with the money mending enchantment
		Optional<EnchantedItemInUse> entry = EnchantmentHelper.getRandomItemWith(ModEnchantments.REPAIR_WITH_MONEY.get(), entity, ItemStack::isDamaged);
		ItemStack item = null;
		if(entry.isEmpty())
		{
			//If we failed to get a vanilla entry, check the curios slots (if applicable)
			if(LCCurios.isLoaded())
				item = LCCurios.getRandomItem(entity,s -> s.isDamaged() && EnchantmentHelper.has(s, ModEnchantments.REPAIR_WITH_MONEY.get()));
		}
		else
			item = entry.get().itemStack();
		if(item != null)
		{
			Pair<RepairWithMoneyData,Integer> dataPair = EnchantmentHelper.getHighestLevel(item, ModEnchantments.REPAIR_WITH_MONEY.get());
			if(dataPair == null)
			{
				LightmansCurrency.LogWarning("Money Mending item was missing its RepairWithMoney data");
				return;
			}
			RepairWithMoneyData data = dataPair.getFirst();
			//Only bother calculating the repair cost until we have a confirmed mending target to reduce lag
			//That and the cost can now be modified based on other enchantments such as infinity, etc.
			MoneyValue repairCost = getRepairCost(item, data, entity.registryAccess());
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
				//Make Money Mending Sound Effect
				if(entity instanceof ServerPlayer player)
					SPacketPlayCoinSound.INSTANCE.sendTo(player);
			}
		}
	}

	public static void addEnchantmentTooltips(@Nonnull ItemStack stack, @Nonnull List<Component> tooltip, @Nonnull Item.TooltipContext context)
	{
		if(context.registries() == null)
			return;
		if(stack.getItem() == Items.ENCHANTED_BOOK)
		{
			//Manually display tootip for enchanted books so that players can identify which type of book it is.
			if(!stack.has(DataComponents.STORED_ENCHANTMENTS))
				return;
			ItemEnchantments enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
			for(var entry : enchantments.entrySet())
			{
				try {
					Enchantment enchantment = entry.getKey().value();
					ResourceKey<Enchantment> key = entry.getKey().unwrapKey().orElse(null);
					if(enchantment != null)
					{
						RepairWithMoneyData data = entry.getKey().value().effects().get(ModEnchantments.REPAIR_WITH_MONEY.get());
						if(data != null)
							TooltipItem.insertTooltip(tooltip,infoTooltip(stack,data,context.registries()));
					}
				}catch (Throwable t) { LightmansCurrency.LogDebug("Error checking item enchantments.",t);}
			}
		}
		else
		{
			//Add tooltip to item with enchantments
			Pair<RepairWithMoneyData,Integer> data = EnchantmentHelper.getHighestLevel(stack,ModEnchantments.REPAIR_WITH_MONEY.get());
			if(data != null)
				TooltipItem.insertTooltip(tooltip,infoTooltip(stack,data.getFirst(),context.registries()));
		}
	}

	private static Component infoTooltip(@Nonnull ItemStack stack, @Nonnull RepairWithMoneyData data, @Nonnull HolderLookup.Provider lookup)
	{
		return LCText.TOOLTIP_MONEY_MENDING_COST.get(MoneyMendingEnchantment.getRepairCost(stack, data, lookup).getText().withStyle(ChatFormatting.YELLOW,ChatFormatting.BOLD));
	}
	
}
