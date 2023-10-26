package io.github.lightman314.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.common.enchantments.LCEnchantmentCategories;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class ModCreativeGroups {

    private static final CustomCreativeTab COIN_GROUP = CustomCreativeTab.build(LightmansCurrency.MODID + ".coins", () -> ModBlocks.COINPILE_GOLD);
    @Nonnull
    public static CustomCreativeTab getCoinGroup() { return COIN_GROUP; }
    private static final CustomCreativeTab MACHINE_GROUP = CustomCreativeTab.build(LightmansCurrency.MODID + ".machines", () -> ModBlocks.COIN_MINT);
    @Nonnull
    public static CustomCreativeTab getMachineGroup() { return MACHINE_GROUP; }
    private static final CustomCreativeTab TRADING_GROUP = CustomCreativeTab.build(LightmansCurrency.MODID + ".trading", () -> ModBlocks.DISPLAY_CASE);
    @Nonnull
    public static CustomCreativeTab getTradingGroup() { return TRADING_GROUP; }
    private static final CustomCreativeTab UPGRADE_GROUP = CustomCreativeTab.build(LightmansCurrency.MODID + ".upgrades", () -> ModItems.ITEM_CAPACITY_UPGRADE_1);
    @Nonnull
    public static CustomCreativeTab getUpgradeGroup() { return UPGRADE_GROUP; }
    private static CustomCreativeTab EXTRA_GROUP = null;
    @Nonnull
    public static CustomCreativeTab getExtraGroup() {
        if(EXTRA_GROUP == null)
            EXTRA_GROUP = CustomCreativeTab.build2(LightmansCurrency.MODID + ".extra", ezRandomIcon(ModCreativeGroups::getExtraGroup));
        return EXTRA_GROUP;
    }

    public static NonNullFunction<WoodType,CreativeModeTab> getExtraOr(NonNullSupplier<CreativeModeTab> normalSource) { return w -> w.isModded() ? getExtraGroup() : normalSource.get(); }

    public static void setupCreativeTabs() {
        COIN_GROUP.setEnchantmentCategories(LCEnchantmentCategories.WALLET_CATEGORY, LCEnchantmentCategories.WALLET_PICKUP_CATEGORY);
        COIN_GROUP.startInit().add(
                //Coin -> Coin Pile -> Coin Block by type
                ModItems.COIN_COPPER, ModBlocks.COINPILE_COPPER, ModBlocks.COINBLOCK_COPPER,
                ModItems.COIN_IRON, ModBlocks.COINPILE_IRON, ModBlocks.COINBLOCK_IRON,
                ModItems.COIN_GOLD, ModBlocks.COINPILE_GOLD, ModBlocks.COINBLOCK_GOLD,
                ModItems.COIN_EMERALD, ModBlocks.COINPILE_EMERALD, ModBlocks.COINBLOCK_EMERALD,
                ModItems.COIN_DIAMOND, ModBlocks.COINPILE_DIAMOND, ModBlocks.COINBLOCK_DIAMOND,
                ModItems.COIN_NETHERITE, ModBlocks.COINPILE_NETHERITE, ModBlocks.COINBLOCK_NETHERITE,
                //Wallets
                ModItems.WALLET_COPPER, ModItems.WALLET_IRON, ModItems.WALLET_GOLD,
                ModItems.WALLET_EMERALD, ModItems.WALLET_DIAMOND, ModItems.WALLET_NETHERITE,
                //Trading Core
                ModItems.TRADING_CORE
        ).build();

        MACHINE_GROUP.startInit().add(
                //Coin Mint
                ModBlocks.COIN_MINT,
                //ATM
                ModBlocks.ATM, ModItems.PORTABLE_ATM,
                //Cash Register
                ModBlocks.CASH_REGISTER,
                //Terminal
                ModBlocks.TERMINAL, ModItems.PORTABLE_TERMINAL,
                ModBlocks.GEM_TERMINAL, ModItems.PORTABLE_GEM_TERMINAL,
                //Trader Interface
                ModBlocks.ITEM_TRADER_INTERFACE,
                //Tax Collector
                ModBlocks.TAX_COLLECTOR,
                //Auction Stands
                ModBlocks.AUCTION_STAND,
                //Ticket Machine
                ModBlocks.TICKET_STATION,
                //Tickets
                ModItems.TICKET_MASTER,
                ModItems.TICKET_PASS,
                ModItems.TICKET,
                ModItems.TICKET_STUB,
                //Coin Chest
                ModBlocks.COIN_CHEST,
                //Coin Jars
                ModBlocks.PIGGY_BANK,
                ModBlocks.COINJAR_BLUE,
                ModBlocks.SUS_JAR
        ).build();

        TRADING_GROUP.startInit().add(
                //Item Traders (normal)
                ModBlocks.SHELF,
                ModBlocks.SHELF_2x2,
                ModBlocks.DISPLAY_CASE,
                ModBlocks.CARD_DISPLAY,
                ModBlocks.VENDING_MACHINE,
                ModBlocks.FREEZER,
                ModBlocks.VENDING_MACHINE_LARGE,
                //Item Traders (specialty)
                ModBlocks.ARMOR_DISPLAY, ModBlocks.TICKET_KIOSK, ModBlocks.BOOKSHELF_TRADER,
                //Slot Machine Trader
                ModBlocks.SLOT_MACHINE,
                //Item Traders (network)
                ModBlocks.ITEM_NETWORK_TRADER_1, ModBlocks.ITEM_NETWORK_TRADER_2,
                ModBlocks.ITEM_NETWORK_TRADER_3, ModBlocks.ITEM_NETWORK_TRADER_4,
                //Paygate
                ModBlocks.PAYGATE
        ).build();

        UPGRADE_GROUP.startInit().add(
                //Item Capacity
                ModItems.ITEM_CAPACITY_UPGRADE_1, ModItems.ITEM_CAPACITY_UPGRADE_2, ModItems.ITEM_CAPACITY_UPGRADE_3,
                //Speed
                ModItems.SPEED_UPGRADE_1, ModItems.SPEED_UPGRADE_2, ModItems.SPEED_UPGRADE_3,
                ModItems.SPEED_UPGRADE_4, ModItems.SPEED_UPGRADE_5,
                //Extra
                ModItems.NETWORK_UPGRADE, ModItems.HOPPER_UPGRADE,
                ModItems.COIN_CHEST_EXCHANGE_UPGRADE,
                ModItems.COIN_CHEST_MAGNET_UPGRADE_1, ModItems.COIN_CHEST_MAGNET_UPGRADE_2,
                ModItems.COIN_CHEST_MAGNET_UPGRADE_3, ModItems.COIN_CHEST_MAGNET_UPGRADE_4,
                ModItems.COIN_CHEST_SECURITY_UPGRADE
        ).build();

        if(EXTRA_GROUP != null)
        {
            EXTRA_GROUP.startInit().add(
                ModBlocks.AUCTION_STAND,
                ModBlocks.SHELF,
                ModBlocks.CARD_DISPLAY,
                ModBlocks.BOOKSHELF_TRADER
            ).build();
        }

    }

    private static Supplier<ItemStack> ezRandomIcon(Supplier<CreativeModeTab> tabSource) {
        return () -> {
            CreativeModeTab tab = tabSource.get();
            NonNullList<ItemStack> list = NonNullList.create();
            tab.fillItemList(list);
            return list.get((int)((TimeUtil.getCurrentTime() / 1000) % list.size()));
        };
    }

}
