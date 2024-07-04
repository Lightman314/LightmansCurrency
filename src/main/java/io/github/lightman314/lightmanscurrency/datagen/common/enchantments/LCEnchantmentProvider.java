package io.github.lightman314.lightmanscurrency.datagen.common.enchantments;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModEnchantments;
import io.github.lightman314.lightmanscurrency.common.enchantments.data.RepairWithMoneyData;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class LCEnchantmentProvider extends DatapackBuiltinEntriesProvider {

    public LCEnchantmentProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, new RegistrySetBuilder().add(Registries.ENCHANTMENT,LCEnchantmentProvider::bootstrap), Set.of(LightmansCurrency.MODID));
    }

    private static void bootstrap(@Nonnull BootstrapContext<Enchantment> context)
    {
        HolderGetter<Item> itemLookup = context.lookup(Registries.ITEM);
        HolderGetter<Enchantment> enchantmentLookup = context.lookup(Registries.ENCHANTMENT);
        context.register(ModEnchantments.COIN_MAGNET,
                Enchantment.enchantment(
                        Enchantment.definition(
                                itemLookup.getOrThrow(LCTags.Items.WALLET_PICKUP),
                                2, 3,
                                Enchantment.dynamicCost(25,25),
                                Enchantment.dynamicCost(75,25),
                                4,
                                EquipmentSlotGroup.ANY
                        )).withSpecialEffect(ModEnchantments.COLLECT_COINS.get(), Unit.INSTANCE).build(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"coin_magnet")));
        context.register(ModEnchantments.MONEY_MENDING,
                Enchantment.enchantment(
                                Enchantment.definition(
                                        itemLookup.getOrThrow(ItemTags.DURABILITY_ENCHANTABLE),
                                        2, 1,
                                        Enchantment.dynamicCost(25,25),
                                        Enchantment.dynamicCost(75,25),
                                        4,
                                        EquipmentSlotGroup.ANY
                                ))
                        .exclusiveWith(enchantmentLookup.getOrThrow(LCTags.Enchantments.EXCUSIVE_SET_MENDING))
                        .withSpecialEffect(ModEnchantments.REPAIR_WITH_MONEY.get(), new RepairWithMoneyData("","")).build(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"money_mending")));

    }

}
