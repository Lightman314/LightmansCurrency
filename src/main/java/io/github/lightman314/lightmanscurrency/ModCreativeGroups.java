package io.github.lightman314.lightmanscurrency;

import com.google.common.base.Suppliers;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.groups.BundleRequestFilter;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = LightmansCurrency.MODID)
public class ModCreativeGroups {

    private static CreativeModeTab COIN_GROUP;
    public static CreativeModeTab getCoinGroup() { return COIN_GROUP; }
    private static CreativeModeTab MACHINE_GROUP;
    public static CreativeModeTab getMachineGroup() { return MACHINE_GROUP; }
    private static CreativeModeTab TRADER_GROUP;
    public static CreativeModeTab getTraderGroup() { return TRADER_GROUP; }
    private static CreativeModeTab UPGRADE_GROUP;
    public static CreativeModeTab getUpgradeGroup() { return UPGRADE_GROUP; }

    private static CreativeModeTab EXTRA_GROUP;
    private static CreativeModeTab getExtraGroup() { return EXTRA_GROUP; }


    @SubscribeEvent
    public static void registerCreativeModeTabs(CreativeModeTabEvent.Register event) {
        //Coin Creative Tab
        COIN_GROUP = event.registerCreativeModeTab(new ResourceLocation(LightmansCurrency.MODID, "coins"), builder ->
            builder.title(EasyText.translatable("itemGroup.lightmanscurrency.coins"))
                .icon(ezIcon(ModBlocks.COINPILE_GOLD))
                .displayItems((parameters, p) -> {
                    //Coin -> Coin Pile -> Coin Block by type
                    ezPop(p, ModItems.COIN_COPPER);
                    ezPop(p, ModBlocks.COINPILE_COPPER);
                    ezPop(p, ModBlocks.COINBLOCK_COPPER);
                    ezPop(p, ModItems.COIN_IRON);
                    ezPop(p, ModBlocks.COINPILE_IRON);
                    ezPop(p, ModBlocks.COINBLOCK_IRON);
                    ezPop(p, ModItems.COIN_GOLD);
                    ezPop(p, ModBlocks.COINPILE_GOLD);
                    ezPop(p, ModBlocks.COINBLOCK_GOLD);
                    ezPop(p, ModItems.COIN_EMERALD);
                    ezPop(p, ModBlocks.COINPILE_EMERALD);
                    ezPop(p, ModBlocks.COINBLOCK_EMERALD);
                    ezPop(p, ModItems.COIN_DIAMOND);
                    ezPop(p, ModBlocks.COINPILE_DIAMOND);
                    ezPop(p, ModBlocks.COINBLOCK_DIAMOND);
                    ezPop(p, ModItems.COIN_NETHERITE);
                    ezPop(p, ModBlocks.COINPILE_NETHERITE);
                    ezPop(p, ModBlocks.COINBLOCK_NETHERITE);
                    //Wallets
                    ezPop(p, ModItems.WALLET_COPPER);
                    ezPop(p, ModItems.WALLET_IRON);
                    ezPop(p, ModItems.WALLET_GOLD);
                    ezPop(p, ModItems.WALLET_EMERALD);
                    ezPop(p, ModItems.WALLET_DIAMOND);
                    ezPop(p, ModItems.WALLET_NETHERITE);
                    //Trading Core
                    ezPop(p, ModItems.TRADING_CORE);
            }));

        //Misc Machine Creative Tab
        MACHINE_GROUP = event.registerCreativeModeTab(new ResourceLocation(LightmansCurrency.MODID, "machines"), List.of(), List.of(COIN_GROUP), builder ->
            builder.title(EasyText.translatable("itemGroup.lightmanscurrency.machines"))
                .icon(ezIcon(ModBlocks.COIN_MINT))
                .displayItems((parameters, p) -> {
                    //Coin Mint
                    ezPop(p, ModBlocks.COIN_MINT);
                    //ATM
                    ezPop(p, ModBlocks.ATM);
                    ezPop(p, ModItems.PORTABLE_ATM);
                    //Cash Register
                    ezPop(p, ModBlocks.CASH_REGISTER);
                    //Terminal
                    ezPop(p, ModBlocks.TERMINAL);
                    ezPop(p, ModBlocks.GEM_TERMINAL);
                    ezPop(p, ModItems.PORTABLE_TERMINAL);
                    ezPop(p, ModItems.PORTABLE_GEM_TERMINAL);
                    //Trader Interface
                    ezPop(p, ModBlocks.ITEM_TRADER_INTERFACE);
                    //Tax Block
                    ezPop(p, ModBlocks.TAX_COLLECTOR);
                    //Auction Stands
                    ezPop(p, ModBlocks.AUCTION_STAND, BundleRequestFilter.VANILLA);
                    //Ticket Machine
                    ezPop(p, ModBlocks.TICKET_STATION);
                    //Tickets (with a creative default UUID)
                    p.accept(TicketItem.CreateMasterTicket(TicketItem.CREATIVE_TICKET_ID, TicketItem.CREATIVE_TICKET_COLOR));
                    p.accept(TicketItem.CreatePass(TicketItem.CREATIVE_TICKET_ID, TicketItem.CREATIVE_TICKET_COLOR));
                    p.accept(TicketItem.CreateTicket(TicketItem.CREATIVE_TICKET_ID, TicketItem.CREATIVE_TICKET_COLOR));
                    //Ticket Stub
                    ezPop(p, ModItems.TICKET_STUB);
                    //Coin Chest
                    ezPop(p, ModBlocks.COIN_CHEST);
                    //Coin Jars
                    ezPop(p, ModBlocks.PIGGY_BANK);
                    ezPop(p, ModBlocks.COINJAR_BLUE);
            }));

        //Trader Creative Tab
        TRADER_GROUP = event.registerCreativeModeTab(new ResourceLocation(LightmansCurrency.MODID, "traders"), List.of(), List.of(MACHINE_GROUP), builder ->
            builder.title(EasyText.translatable("itemGroup.lightmanscurrency.trading"))
                .icon(ezIcon(ModBlocks.DISPLAY_CASE))
                .displayItems((parameters, p) -> {
                    //Item Traders (normal)
                    ezPop(p, ModBlocks.SHELF, BundleRequestFilter.VANILLA);
                    ezPop(p, ModBlocks.DISPLAY_CASE);
                    ezPop(p, ModBlocks.CARD_DISPLAY, BundleRequestFilter.VANILLA);
                    ezPop(p, ModBlocks.VENDING_MACHINE);
                    ezPop(p, ModBlocks.FREEZER);
                    ezPop(p, ModBlocks.VENDING_MACHINE_LARGE);
                    //Item Traders (specialty)
                    ezPop(p, ModBlocks.ARMOR_DISPLAY);
                    ezPop(p, ModBlocks.TICKET_KIOSK);
                    ezPop(p, ModBlocks.BOOKSHELF_TRADER, BundleRequestFilter.VANILLA);
                    //Slot Machine Trader
                    ezPop(p, ModBlocks.SLOT_MACHINE);
                    //Item Traders (network)
                    ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_1);
                    ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_2);
                    ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_3);
                    ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_4);
                    //Paygate
                    ezPop(p, ModBlocks.PAYGATE);
            }));

        UPGRADE_GROUP = event.registerCreativeModeTab(new ResourceLocation(LightmansCurrency.MODID, "upgrades"), List.of(), List.of(TRADER_GROUP), builder ->
            builder.title(EasyText.translatable("itemGroup.lightmanscurrency.upgrades"))
                .icon(ezIcon(ModItems.ITEM_CAPACITY_UPGRADE_1))
                .displayItems((parameters, p) -> {
                    ezPop(p, ModItems.ITEM_CAPACITY_UPGRADE_1);
                    ezPop(p, ModItems.ITEM_CAPACITY_UPGRADE_2);
                    ezPop(p, ModItems.ITEM_CAPACITY_UPGRADE_3);
                    ezPop(p, ModItems.SPEED_UPGRADE_1);
                    ezPop(p, ModItems.SPEED_UPGRADE_2);
                    ezPop(p, ModItems.SPEED_UPGRADE_3);
                    ezPop(p, ModItems.SPEED_UPGRADE_4);
                    ezPop(p, ModItems.SPEED_UPGRADE_5);
                    ezPop(p, ModItems.NETWORK_UPGRADE);
                    ezPop(p, ModItems.HOPPER_UPGRADE);
                    ezPop(p, ModItems.COIN_CHEST_EXCHANGE_UPGRADE);
                    //ezPop(p, ModItems.COIN_CHEST_BANK_UPGRADE);
                    ezPop(p, ModItems.COIN_CHEST_MAGNET_UPGRADE_1);
                    ezPop(p, ModItems.COIN_CHEST_MAGNET_UPGRADE_2);
                    ezPop(p, ModItems.COIN_CHEST_MAGNET_UPGRADE_3);
                    ezPop(p, ModItems.COIN_CHEST_MAGNET_UPGRADE_4);
                    ezPop(p, ModItems.COIN_CHEST_SECURITY_UPGRADE);
            }));

        if(WoodType.hasModdedValues())
        {
            EXTRA_GROUP = event.registerCreativeModeTab(new ResourceLocation(LightmansCurrency.MODID, "extra"), List.of(UPGRADE_GROUP), List.of(TRADER_GROUP), builder ->
            builder.title(EasyText.translatable("itemGroup.lightmanscurrency.extra"))
                .icon(ezRandomIcon(ModCreativeGroups::getExtraGroup))
                .displayItems((parameters,p) -> {
                    ezPop(p, ModBlocks.AUCTION_STAND, BundleRequestFilter.MODDED);
                    ezPop(p, ModBlocks.SHELF, BundleRequestFilter.MODDED);
                    ezPop(p, ModBlocks.CARD_DISPLAY, BundleRequestFilter.MODDED);
                    ezPop(p, ModBlocks.BOOKSHELF_TRADER, BundleRequestFilter.MODDED);
                }).build()
            );
        }
        else
            EXTRA_GROUP = null;

    }

    @SubscribeEvent
    public static void buildVanillaTabContents(CreativeModeTabEvent.BuildContents event) {
        if(event.getTab() == CreativeModeTabs.FUNCTIONAL_BLOCKS)
        {
            event.accept(ModBlocks.PIGGY_BANK);
            event.accept(ModBlocks.COINJAR_BLUE);
        }
        if(event.getTab() == CreativeModeTabs.REDSTONE_BLOCKS)
        {
            event.accept(ModBlocks.PAYGATE);
        }
        if(event.getTab() == CreativeModeTabs.COLORED_BLOCKS)
        {
            event.acceptAll(convertToStack(ModBlocks.VENDING_MACHINE.getAllSorted()));
            event.acceptAll(convertToStack(ModBlocks.VENDING_MACHINE_LARGE.getAllSorted()));
            event.acceptAll(convertToStack(ModBlocks.FREEZER.getAllSorted()));
            if(ModBlocks.SUS_JAR.get().asItem() instanceof DyeableLeatherItem susItem)
            {
                for(Color c : Color.values())
                {
                    ItemStack stack = new ItemStack(ModBlocks.SUS_JAR.get());
                    if(c != Color.WHITE)
                        susItem.setColor(stack, c.hexColor);
                    event.accept(stack);
                }
            }
        }
    }

    private static Supplier<ItemStack> ezIcon(RegistryObject<? extends ItemLike> item) { return Suppliers.memoize(() -> new ItemStack(item.get())); }
    private static Supplier<ItemStack> ezRandomIcon(Supplier<CreativeModeTab> tabSource) {
        return () -> {
            CreativeModeTab tab = tabSource.get();
            List<ItemStack> items = tab.getDisplayItems().stream().toList();
            return items.get((int)((TimeUtil.getCurrentTime() / 1000) % items.size()));
        };
    }

    public static void ezPop(CreativeModeTab.Output populator, RegistryObject<? extends ItemLike> item)  { populator.accept(item.get()); }
    public static void ezPop(CreativeModeTab.Output populator, RegistryObjectBundle<? extends ItemLike,?> bundle) { bundle.getAllSorted().forEach(populator::accept); }
    public static void ezPop(CreativeModeTab.Output populator, RegistryObjectBundle<? extends ItemLike,?> bundle, BundleRequestFilter filter) { bundle.getAllSorted(filter).forEach(populator::accept); }
    public static void ezPop(CreativeModeTab.Output populator, RegistryObjectBiBundle<? extends ItemLike,?,?> bundle) { bundle.getAllSorted().forEach(populator::accept); }
    public static void ezPop(CreativeModeTab.Output populator, RegistryObjectBiBundle<? extends ItemLike,?,?> bundle, BundleRequestFilter filter) { bundle.getAllSorted(filter).forEach(populator::accept); }

    private static Collection<ItemStack> convertToStack(Collection<? extends ItemLike> list) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemLike item : list) result.add(new ItemStack(item));
        return result;
    }

}
