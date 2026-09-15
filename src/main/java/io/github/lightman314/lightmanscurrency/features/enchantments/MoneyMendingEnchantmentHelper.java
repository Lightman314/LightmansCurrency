package io.github.lightman314.lightmanscurrency.features.enchantments;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCCapabilities;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.core.LCEnchantmentDataComponents;
import io.github.lightman314.lightmanscurrency.features.enchantments.data.RepairWithMoneyData;
import io.github.lightman314.lightmanscurrency.integration.curios.LCCuriosHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

public final class MoneyMendingEnchantmentHelper {

    private MoneyMendingEnchantmentHelper() {}

    public static final TextEntry TOOLTIP_MONEY_MENDING_COST = TextEntry.tooltip(LCApi.MODID,"money_mending.price");

    public static void runEntityTick(LivingEntity entity) {
        if(entity.level().isClientSide())
            return;
        Optional<EnchantedItemInUse> entry = EnchantmentHelper.getRandomItemWith(LCEnchantmentDataComponents.REPAIR_WITH_MONEY.get(),entity,ItemStack::isDamaged);
        ItemStack item;
        if(entry.isEmpty()) {
            //Attempt to get the item from Curios if all vanilla equipment slots are undamaged or unenchanted
            item = LCCuriosHelper.get().getRandomItem(entity,s -> s.isDamaged() && EnchantmentHelper.has(s,LCEnchantmentDataComponents.REPAIR_WITH_MONEY.get()));
        }
        else
            item = entry.get().itemStack();
        if(item != null && !item.isEmpty()) {
            Pair<RepairWithMoneyData,Integer> dataPair = EnchantmentHelper.getHighestLevel(item,LCEnchantmentDataComponents.REPAIR_WITH_MONEY.get());
            if(dataPair == null)
                return;
            RepairWithMoneyData data = dataPair.getFirst();
            MoneyValue repairCost = getRepairCost(item,data,entity.registryAccess());
            MoneyResourceHandler entityMoney = entity.getCapability(LCCapabilities.Money.ENTITY,null);
            if(entityMoney == null || !entityMoney.containsResource(repairCost))
                return;
            MoneyValue nextCost = repairCost;
            MoneyValue finalCost = MoneyValue.empty();
            int currentDamage = item.getDamageValue();
            int repairAmount = 0;
            while(nextCost != null && entityMoney.containsResource(nextCost) && repairAmount < currentDamage) {
                repairAmount++;
                finalCost = nextCost;
                nextCost = nextCost.addValue(repairCost);
            }
            //Take the payment from the entity
            try(Transaction transaction = Transaction.openRoot()) {
                MoneyValue taken = entityMoney.extract(finalCost,transaction);
                if(taken.equals(finalCost)) {
                    //If the amount taken was correct, commit the transaction and repair the item
                    transaction.commit();
                    item.setDamageValue(currentDamage - repairAmount);
                    //TODO Make Money Mending Sound Effect
                    //if(entity instanceof ServerPlayer sp)
                }
            }
        }
    }

    public static MoneyValue getRepairCost(ItemStack item, RepairWithMoneyData data, HolderLookup.Provider registries) {
        ItemEnchantments enchantments = item.has(DataComponents.STORED_ENCHANTMENTS) ?
                item.get(DataComponents.STORED_ENCHANTMENTS) :
                item.getAllEnchantments(registries.lookupOrThrow(Registries.ENCHANTMENT));
        return data.getRepairCost(item,enchantments);
    }

    public static void addEnchantmentTooltips(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, @Nullable Player player, TooltipFlag flag, Consumer<Component> builder) {
        if(!display.shows(DataComponents.ENCHANTMENTS))
            return;
        if(stack.getItem() == Items.ENCHANTED_BOOK) {
            //Manually display tooltip for enchanted books so that players can identify which type of book it is.
            if(!stack.has(DataComponents.STORED_ENCHANTMENTS))
                return;
            ItemEnchantments enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
            for(var entry : enchantments.entrySet()) {
                try{
                    Enchantment enchantment = entry.getKey().value();
                    if(enchantment != null) {
                        RepairWithMoneyData data = enchantment.effects().get(LCEnchantmentDataComponents.REPAIR_WITH_MONEY.get());
                        if(data != null)
                            builder.accept(infoTooltip(stack,data,enchantments));
                    }
                } catch (IllegalStateException ignored) {}
            }
        }else if(context.registries() != null) {
            //Add tooltip to item with enchantments
            Pair<RepairWithMoneyData,Integer> data = EnchantmentHelper.getHighestLevel(stack,LCEnchantmentDataComponents.REPAIR_WITH_MONEY.get());
            if(data != null)
                builder.accept(infoTooltip(stack,data.getFirst(),stack.getAllEnchantments(context.registries().lookupOrThrow(Registries.ENCHANTMENT))));
        }
    }

    private static Component infoTooltip(ItemStack stack,RepairWithMoneyData data,ItemEnchantments enchantments) {
        return TOOLTIP_MONEY_MENDING_COST.get(data.getRepairCost(stack,enchantments).getText().copy().withStyle(ChatFormatting.YELLOW,ChatFormatting.BOLD));
    }

}
