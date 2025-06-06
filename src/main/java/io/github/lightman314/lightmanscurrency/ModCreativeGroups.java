package io.github.lightman314.lightmanscurrency;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.ModelVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperties;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.ModRegistries;
import io.github.lightman314.lightmanscurrency.common.core.groups.BundleRequestFilter;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.common.items.MoneyBagItem;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.items.data.MoneyBagData;
import io.github.lightman314.lightmanscurrency.util.ListUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = LightmansCurrency.MODID)
public class ModCreativeGroups {

    public static final ResourceLocation COIN_GROUP_ID = VersionUtil.lcResource("coins");
    public static final ResourceLocation MACHINE_GROUP_ID = VersionUtil.lcResource("machines");
    public static final ResourceLocation TRADER_GROUP_ID = VersionUtil.lcResource("traders");
    public static final ResourceLocation UPGRADE_GROUP_ID = VersionUtil.lcResource("upgrades");

    public static final ResourceLocation EXTRA_GROUP_ID = VersionUtil.lcResource( "extra");

    /**
     * Placeholder function to force the static class loading
     */
    public static void init() { }

    static {

        COIN_GROUP = ModRegistries.CREATIVE_TABS.register("coins", () -> CreativeModeTab.builder()
                .title(LCText.CREATIVE_GROUP_COINS.get())
                .icon(ezIcon(ModBlocks.COINPILE_GOLD))
                .displayItems((parameters,p) -> {
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
                    ezPop(p, ModItems.WALLET_LEATHER);
                    ezPop(p, ModItems.WALLET_COPPER);
                    ezPop(p, ModItems.WALLET_IRON);
                    ezPop(p, ModItems.WALLET_GOLD);
                    ezPop(p, ModItems.WALLET_EMERALD);
                    ezPop(p, ModItems.WALLET_DIAMOND);
                    ezPop(p, ModItems.WALLET_NETHERITE);
                    ezPop(p, ModItems.WALLET_NETHER_STAR);
                    ezPop(p, ModItems.WALLET_ENDER_DRAGON);
                    //Bank Card
                    ezPop(p, ModItems.ATM_CARD);
                    //Perhaps example prepaid card?
                    //Trading Core
                    ezPop(p, ModItems.TRADING_CORE);
                    //Variant Wand
                    ezPop(p, ModItems.VARIANT_WAND);
                    //Event Coins
                    ezPop(p, ModItems.COIN_CHOCOLATE_COPPER);
                    ezPop(p, ModBlocks.COINPILE_CHOCOLATE_COPPER);
                    ezPop(p, ModBlocks.COINBLOCK_CHOCOLATE_COPPER);
                    ezPop(p, ModItems.COIN_CHOCOLATE_IRON);
                    ezPop(p, ModBlocks.COINPILE_CHOCOLATE_IRON);
                    ezPop(p, ModBlocks.COINBLOCK_CHOCOLATE_IRON);
                    ezPop(p, ModItems.COIN_CHOCOLATE_GOLD);
                    ezPop(p, ModBlocks.COINPILE_CHOCOLATE_GOLD);
                    ezPop(p, ModBlocks.COINBLOCK_CHOCOLATE_GOLD);
                    ezPop(p, ModItems.COIN_CHOCOLATE_EMERALD);
                    ezPop(p, ModBlocks.COINPILE_CHOCOLATE_EMERALD);
                    ezPop(p, ModBlocks.COINBLOCK_CHOCOLATE_EMERALD);
                    ezPop(p, ModItems.COIN_CHOCOLATE_DIAMOND);
                    ezPop(p, ModBlocks.COINPILE_CHOCOLATE_DIAMOND);
                    ezPop(p, ModBlocks.COINBLOCK_CHOCOLATE_DIAMOND);
                    ezPop(p, ModItems.COIN_CHOCOLATE_NETHERITE);
                    ezPop(p, ModBlocks.COINPILE_CHOCOLATE_NETHERITE);
                    ezPop(p, ModBlocks.COINBLOCK_CHOCOLATE_NETHERITE);
                    //Ancient Coins
                    for(AncientCoinType type : AncientCoinType.values())
                        p.accept(type.asItem());
                }).build()
        );

        MACHINE_GROUP = ModRegistries.CREATIVE_TABS.register("machines", () -> CreativeModeTab.builder()
                .withTabsBefore(COIN_GROUP_ID)
                .title(LCText.CREATIVE_GROUP_MACHINES.get())
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
                    p.accept(TicketItem.CreateTicket(ModItems.TICKET_MASTER.get(),-1));
                    p.accept(TicketItem.CreateTicket(ModItems.TICKET_PASS.get(),-1));
                    p.accept(TicketItem.CreateTicket(ModItems.TICKET.get(), -1));
                    //Ticket Stub
                    ezPop(p, ModItems.TICKET_STUB);
                    //Golden Tickets (with a creative default UUID)
                    p.accept(TicketItem.CreateTicket(ModItems.GOLDEN_TICKET_MASTER.get(),-2));
                    p.accept(TicketItem.CreateTicket(ModItems.GOLDEN_TICKET_PASS.get(),-2));
                    p.accept(TicketItem.CreateTicket(ModItems.GOLDEN_TICKET.get(),-2));
                    //Golden Ticket Stub
                    ezPop(p, ModItems.GOLDEN_TICKET_STUB);
                    //Coin Chest
                    ezPop(p, ModBlocks.COIN_CHEST);
                    //Coin Jars
                    ezPop(p, ModBlocks.PIGGY_BANK);
                    ezPop(p, ModBlocks.COINJAR_BLUE);
                    //Money Bag
                    p.accept(MoneyBagItem.createItem(ModBlocks.MONEY_BAG.get(), MoneyBagData.EMPTY));
                }).build()
        );

        TRADER_GROUP = ModRegistries.CREATIVE_TABS.register("traders", () -> CreativeModeTab.builder()
                .withTabsBefore(MACHINE_GROUP_ID)
                .title(LCText.CREATIVE_GROUP_TRADING.get())
                .icon(ezIcon(ModBlocks.DISPLAY_CASE.get(Color.WHITE)))
                .displayItems((parameters, p) -> {
                    //Item Traders (normal)
                    ezPop(p, ModBlocks.DISPLAY_CASE);
                    ezPop(p, ModBlocks.SHELF, BundleRequestFilter.VANILLA);
                    ezPop(p, ModBlocks.SHELF_2x2, BundleRequestFilter.VANILLA);
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
                    //Gatcha Machine
                    ezPop(p, ModBlocks.GACHA_MACHINE);
                    //Item Traders (network)
                    ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_1);
                    ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_2);
                    ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_3);
                    ezPop(p, ModBlocks.ITEM_NETWORK_TRADER_4);
                    //Paygate
                    ezPop(p, ModBlocks.PAYGATE);
                    //Command Trader
                    ezPop(p, ModBlocks.COMMAND_TRADER);
                }).build()
        );

        UPGRADE_GROUP = ModRegistries.CREATIVE_TABS.register("upgrades", () -> CreativeModeTab.builder()
                .withTabsBefore(TRADER_GROUP_ID)
                .title(LCText.CREATIVE_GROUP_UPGRADES.get())
                .icon(ezIcon(ModItems.ITEM_CAPACITY_UPGRADE_1))
                .displayItems((parameters, p) -> {
                    ezPop(p, ModItems.UPGRADE_SMITHING_TEMPLATE);
                    ezPop(p, ModItems.ITEM_CAPACITY_UPGRADE_1);
                    ezPop(p, ModItems.ITEM_CAPACITY_UPGRADE_2);
                    ezPop(p, ModItems.ITEM_CAPACITY_UPGRADE_3);
                    ezPop(p, ModItems.ITEM_CAPACITY_UPGRADE_4);
                    ezPop(p, ModItems.SPEED_UPGRADE_1);
                    ezPop(p, ModItems.SPEED_UPGRADE_2);
                    ezPop(p, ModItems.SPEED_UPGRADE_3);
                    ezPop(p, ModItems.SPEED_UPGRADE_4);
                    ezPop(p, ModItems.SPEED_UPGRADE_5);
                    ezPop(p, ModItems.OFFER_UPGRADE_1);
                    ezPop(p, ModItems.OFFER_UPGRADE_2);
                    ezPop(p, ModItems.OFFER_UPGRADE_3);
                    ezPop(p, ModItems.OFFER_UPGRADE_4);
                    ezPop(p, ModItems.OFFER_UPGRADE_5);
                    ezPop(p, ModItems.OFFER_UPGRADE_6);
                    ezPop(p, ModItems.NETWORK_UPGRADE);
                    ezPop(p, ModItems.VOID_UPGRADE);
                    ezPop(p, ModItems.HOPPER_UPGRADE);
                    ezPop(p, ModItems.INTERACTION_UPGRADE_1);
                    ezPop(p, ModItems.INTERACTION_UPGRADE_2);
                    ezPop(p, ModItems.INTERACTION_UPGRADE_3);
                    ezPop(p, ModItems.COIN_CHEST_EXCHANGE_UPGRADE);
                    ezPop(p, ModItems.COIN_CHEST_MAGNET_UPGRADE_1);
                    ezPop(p, ModItems.COIN_CHEST_MAGNET_UPGRADE_2);
                    ezPop(p, ModItems.COIN_CHEST_MAGNET_UPGRADE_3);
                    ezPop(p, ModItems.COIN_CHEST_MAGNET_UPGRADE_4);
                    ezPop(p, ModItems.COIN_CHEST_BANK_UPGRADE);
                    ezPop(p, ModItems.COIN_CHEST_SECURITY_UPGRADE);
                }).build()
        );

        if(WoodType.hasModdedValues())
        {
            EXTRA_GROUP = ModRegistries.CREATIVE_TABS.register("extra", () -> CreativeModeTab.builder()
                    .withTabsBefore(TRADER_GROUP_ID)
                    .withTabsAfter(UPGRADE_GROUP_ID)
                    .title(LCText.CREATIVE_GROUP_EXTRA.get())
                    .icon(ezRandomIcon(ModCreativeGroups::getExtraGroup))
                    .displayItems((parameters,p) -> {
                        ezPop(p, ModBlocks.AUCTION_STAND, BundleRequestFilter.MODDED);
                        ezPop(p, ModBlocks.SHELF, BundleRequestFilter.MODDED);
                        ezPop(p, ModBlocks.SHELF_2x2, BundleRequestFilter.MODDED);
                        ezPop(p, ModBlocks.CARD_DISPLAY, BundleRequestFilter.MODDED);
                        ezPop(p, ModBlocks.BOOKSHELF_TRADER, BundleRequestFilter.MODDED);
                    }).build()
            );
        }
        else
            EXTRA_GROUP = null;

    }

    private static CreativeModeTab getExtraGroup() { return EXTRA_GROUP.get(); }

    @SubscribeEvent
    public static void buildVanillaTabContents(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS)
        {
            event.accept(ModBlocks.PIGGY_BANK.get());
            event.accept(ModBlocks.COINJAR_BLUE.get());
        }
        if(event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS)
        {
            event.accept(ModBlocks.PAYGATE.get());
        }
        if(event.getTabKey() == CreativeModeTabs.COLORED_BLOCKS)
        {
            event.acceptAll(convertToStack(ModBlocks.VENDING_MACHINE.getAllSorted()));
            event.acceptAll(convertToStack(ModBlocks.VENDING_MACHINE_LARGE.getAllSorted()));
            event.acceptAll(convertToStack(ModBlocks.FREEZER.getAllSorted()));
            event.acceptAll(convertToStack(ModBlocks.CARD_DISPLAY.getAllSorted()));
            event.acceptAll(convertToStack(ModBlocks.GACHA_MACHINE.getAllSorted()));
            for(Color c : Color.values())
            {
                ItemStack stack = new ItemStack(ModBlocks.SUS_JAR.get());
                if(c != Color.WHITE)
                    stack.set(DataComponents.DYED_COLOR,new DyedItemColor(c.hexColor,true));
                event.accept(stack);
            }
        }
        if(event.getTabKey() == CreativeModeTabs.OP_BLOCKS && event.hasPermissions())
        {
            event.accept(ModBlocks.COMMAND_TRADER.get());
        }
    }

    private static Supplier<ItemStack> ezIcon(Supplier<? extends ItemLike> item) { return Suppliers.memoize(() -> new ItemStack(item.get())); }
    private static Supplier<ItemStack> ezIcon(ItemLike item) { return Suppliers.memoize(() -> new ItemStack(item)); }
    private static Supplier<ItemStack> ezRandomIcon(@Nonnull Supplier<CreativeModeTab> tabSource) {
        return () -> {
            CreativeModeTab tab = tabSource.get();
            if(tab == null)
                return new ItemStack(ModItems.TRADING_CORE.get());
            return ListUtil.randomItemFromCollection(tab.getDisplayItems(),new ItemStack(ModItems.TRADING_CORE.get()));
        };
    }

    public static void ezPop(CreativeModeTab.Output populator, ItemLike item)  {
        populator.accept(item);
        //Check for variants
        if(item.asItem() instanceof BlockItem bi && bi.getBlock() instanceof IVariantBlock vb && LightmansCurrency.getProxy().isClient())
        {
            List<Pair<ResourceLocation,ModelVariant>> foundVariants = new ArrayList<>();
            for(ResourceLocation variantID : vb.getValidVariants())
            {
                ModelVariant variant = ModelVariantDataManager.getVariant(variantID);
                if(variant != null && variant.getOrDefault(VariantProperties.SHOW_IN_CREATIVE).show())
                    foundVariants.add(Pair.of(variantID,variant));
            }
            if(!foundVariants.isEmpty())
            {
                foundVariants.sort(ModelVariant.COMPARATOR);
                for(Pair<ResourceLocation,ModelVariant> entry : foundVariants)
                {
                    ItemStack stack = new ItemStack(item);
                    stack.set(ModDataComponents.MODEL_VARIANT,entry.getFirst());
                    if(entry.getSecond().get(VariantProperties.SHOW_IN_CREATIVE).locked())
                        stack.set(ModDataComponents.VARIANT_LOCK, Unit.INSTANCE);
                    populator.accept(stack);
                }
            }
        }
    }
    public static void ezPop(CreativeModeTab.Output populator, List<? extends ItemLike> items) { for(ItemLike i : items) ezPop(populator,i); }
    public static void ezPop(CreativeModeTab.Output populator, Supplier<? extends ItemLike> item)  { ezPop(populator,item.get()); }
    public static void ezPop(CreativeModeTab.Output populator, RegistryObjectBundle<? extends ItemLike,?> bundle) { ezPop(populator,bundle.getAllSorted()); }
    public static void ezPop(CreativeModeTab.Output populator, RegistryObjectBundle<? extends ItemLike,?> bundle, BundleRequestFilter filter) { ezPop(populator,bundle.getAllSorted(filter)); }
    public static void ezPop(CreativeModeTab.Output populator, RegistryObjectBiBundle<? extends ItemLike,?,?> bundle) { ezPop(populator,bundle.getAllSorted()); }
    public static void ezPop(CreativeModeTab.Output populator, RegistryObjectBiBundle<? extends ItemLike,?,?> bundle, BundleRequestFilter filter) { ezPop(populator,bundle.getAllSorted(filter)); }

    private static Collection<ItemStack> convertToStack(Collection<? extends ItemLike> list) {
        List<ItemStack> result = new ArrayList<>();
        for (ItemLike item : list) result.add(new ItemStack(item));
        return result;
    }

    public static final Supplier<CreativeModeTab> COIN_GROUP;
    public static final Supplier<CreativeModeTab> MACHINE_GROUP;
    public static final Supplier<CreativeModeTab> TRADER_GROUP;
    public static final Supplier<CreativeModeTab> UPGRADE_GROUP;
    @Nullable
    public static Supplier<CreativeModeTab> EXTRA_GROUP;

}
