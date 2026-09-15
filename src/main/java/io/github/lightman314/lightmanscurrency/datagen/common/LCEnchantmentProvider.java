package io.github.lightman314.lightmanscurrency.datagen.common;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.LCTags;
import io.github.lightman314.lightmanscurrency.api.config.data.ItemListOptionSet;
import io.github.lightman314.lightmanscurrency.api.money.values.source.builtin.ConfiguredSource;
import io.github.lightman314.lightmanscurrency.core.LCEnchantmentDataComponents;
import io.github.lightman314.lightmanscurrency.features.enchantments.LCEnchantments;
import io.github.lightman314.lightmanscurrency.features.enchantments.data.RepairWithMoneyData;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public final class LCEnchantmentProvider {

    private LCEnchantmentProvider() {}

    public static void bootstrap(BootstrapContext<Enchantment> context) {
        HolderGetter<Item> itemLookup = context.lookup(Registries.ITEM);
        HolderGetter<Enchantment> enchantmentLookup = context.lookup(Registries.ENCHANTMENT);
        //Coin Magnet Enchantment
        context.register(LCEnchantments.COIN_MAGNET,
                Enchantment.enchantment(Enchantment.definition(
                        ItemListOptionSet.create(LCConfig.SERVER.walletCanPickup),
                        2,3,
                        Enchantment.dynamicCost(25,25),
                        Enchantment.dynamicCost(75,25),
                        4, EquipmentSlotGroup.ANY))
                        .withSpecialEffect(LCEnchantmentDataComponents.COLLECT_COINS.get(), Unit.INSTANCE)
                        .build(LCEnchantments.COIN_MAGNET.identifier()));

        //Money Mending Enchantment
        context.register(LCEnchantments.MONEY_MENDING,
                Enchantment.enchantment(Enchantment.definition(
                        itemLookup.getOrThrow(ItemTags.DURABILITY_ENCHANTABLE),
                        2,1,
                        Enchantment.dynamicCost(25,25),
                        Enchantment.dynamicCost(75,25),
                        4,EquipmentSlotGroup.ANY))
                        .exclusiveWith(enchantmentLookup.getOrThrow(LCTags.Enchantments.EXCLUSIVE_SET_MENDING))
                        .withSpecialEffect(LCEnchantmentDataComponents.REPAIR_WITH_MONEY.get(),
                                RepairWithMoneyData.builder()
                                        .baseCost(ConfiguredSource.configured(LCConfig.SERVER.moneyMendingRepairCost))
                                        .bonusForEnchantment(Enchantments.INFINITY,ConfiguredSource.configured(LCConfig.SERVER.moneyMendingInfinityCost),1)
                                        .build())
                        .build(LCEnchantments.MONEY_MENDING.identifier()));

    }

}
