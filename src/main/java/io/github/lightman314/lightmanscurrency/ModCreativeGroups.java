package io.github.lightman314.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.items.TicketItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

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


    @SubscribeEvent
    public static void registerCreativeModeTabs(CreativeModeTabEvent.Register event) {
        //Coin Creative Tab
        COIN_GROUP = event.registerCreativeModeTab(new ResourceLocation(LightmansCurrency.MODID, "coins"), builder ->
            builder.title(Component.translatable("itemGroup.lightmanscurrency.coins"))
                .icon(() -> new ItemStack(ModBlocks.COINPILE_GOLD.get()))
                .displayItems((enabledFlags, p, hasPermissions) -> {
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
            builder.title(Component.translatable("itemGroup.lightmanscurrency.machines"))
                .icon(() -> new ItemStack(ModBlocks.MACHINE_MINT.get()))
                .displayItems((enabledFlags, p, hasPermissions) -> {
                    //Coin Mint
                    ezPop(p, ModBlocks.MACHINE_MINT);
                    //ATM
                    ezPop(p, ModBlocks.MACHINE_ATM);
                    ezPop(p, ModItems.PORTABLE_ATM);
                    //Terminal
                    ezPop(p, ModBlocks.TERMINAL);
                    ezPop(p, ModBlocks.GEM_TERMINAL);
                    ezPop(p, ModItems.PORTABLE_TERMINAL);
                    ezPop(p, ModItems.PORTABLE_GEM_TERMINAL);
                    //Ticket Machine
                    ezPop(p, ModBlocks.TICKET_MACHINE);
                    //Tickets (with a creative default UUID)
                    p.accept(TicketItem.CreateMasterTicket(TicketItem.CREATIVE_TICKET_ID));
                    p.accept(TicketItem.CreateTicket(TicketItem.CREATIVE_TICKET_ID));
                    //Ticket Stub
                    ezPop(p, ModItems.TICKET_STUB);
            }));

        //Trader Creative Tab
        TRADER_GROUP = event.registerCreativeModeTab(new ResourceLocation(LightmansCurrency.MODID, "traders"), List.of(), List.of(MACHINE_GROUP), builder ->
            builder.title(Component.translatable("itemGroup.lightmanscurrency.trading"))
                .icon(() -> new ItemStack(ModBlocks.DISPLAY_CASE.get()))
                .displayItems((enabledFlags, p, hasPermissions) -> {
                    //Item Traders (normal)
                    ezPop(p, ModBlocks.SHELF, Reference::sortByWood);
                    ezPop(p, ModBlocks.DISPLAY_CASE);
                    ezPop(p, ModBlocks.CARD_DISPLAY, Reference::sortByWood);
                    ezPop(p, ModBlocks.VENDING_MACHINE, Reference::sortByColor);
                    ezPop(p, ModBlocks.VENDING_MACHINE_LARGE, Reference::sortByColor);
                    //Item Traders (specialty)
                    ezPop(p, ModBlocks.ARMOR_DISPLAY);
                    ezPop(p, ModBlocks.TICKET_KIOSK);
                    //Item Traders (network)
                    ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_1);
                    ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_2);
                    ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_3);
                    ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_4);
                    //Paygate
                    ezPop(p, ModBlocks.PAYGATE);
            }));

        UPGRADE_GROUP = event.registerCreativeModeTab(new ResourceLocation(LightmansCurrency.MODID, "upgrades"), List.of(), List.of(TRADER_GROUP), builder ->
            builder.title(Component.translatable("itemGroup.lightmanscurrency.upgrades"))
                .icon(() -> new ItemStack(ModItems.ITEM_CAPACITY_UPGRADE_1.get()))
                .displayItems((enabledFlags, p, hasPermissions) -> {
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
            }));
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
            event.acceptAll(convertToStack(ModBlocks.VENDING_MACHINE.getAllSorted(Reference::sortByColor)));
            event.acceptAll(convertToStack(ModBlocks.VENDING_MACHINE_LARGE.getAllSorted(Reference::sortByColor)));
        }
    }

    public static void ezPop(CreativeModeTab.Output populator, RegistryObject<? extends ItemLike> item)  { populator.accept(item.get()); }
    public static <L> void ezPop(CreativeModeTab.Output populator, RegistryObjectBundle<? extends ItemLike, L> bundle, Comparator<L> sorter) { bundle.getAllSorted(sorter).forEach(populator::accept); }

    private static Collection<ItemStack> convertToStack(Collection<? extends ItemLike> list) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemLike item : list) result.add(new ItemStack(item));
        return result;
    }

}
