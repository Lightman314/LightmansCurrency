package io.github.lightman314.lightmanscurrency.integration.reiplugin;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.gui.screen.NotificationScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.*;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.RecipeTypes;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import io.github.lightman314.lightmanscurrency.common.items.colored.ColoredItem;
import io.github.lightman314.lightmanscurrency.common.menus.TicketStationMenu;
import io.github.lightman314.lightmanscurrency.integration.reiplugin.coin_mint.*;
import io.github.lightman314.lightmanscurrency.integration.reiplugin.ticket_station.*;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.registry.transfer.simple.SimpleTransferHandler;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@REIPluginClient
public class LCClientPlugin implements REIClientPlugin {

    @Override
    public void registerCategories(CategoryRegistry registry) {
        //Coin Mint
        registry.add(CoinMintCategory.INSTANCE);
        registry.addWorkstations(CoinMintCategory.ID,EntryStacks.of(ModBlocks.COIN_MINT.get()));
        //Ticket Station
        registry.add(TicketStationCategory.INSTANCE);
        registry.addWorkstations(TicketStationCategory.ID,EntryStacks.of(ModBlocks.TICKET_STATION.get()));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        //Coin Mint
        registry.registerRecipeFiller(CoinMintRecipe.class,RecipeTypes.COIN_MINT.get(),CoinMintDisplay::new);
        //Ticket Station
        registry.registerRecipeFiller(TicketStationRecipe.class,RecipeTypes.TICKET.get(),TicketStationDisplay::new);
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        //Click area for the Coin Mint is on the arrow
        registry.registerClickArea(screen -> new Rectangle(screen.getGuiLeft() + 80, screen.getGuiTop() + 21, 22, 16), MintScreen.class, CoinMintCategory.ID);
        //Click area for the Ticket Station is on the title text as the arrow is already a clickable button
        registry.registerClickArea(screen -> new Rectangle(screen.getGuiLeft() + 8, screen.getGuiTop() + 6, 100,10), TicketStationScreen.class, TicketStationCategory.ID);
    }

    @Override
    public void registerEntries(EntryRegistry registry) {
        registry.removeEntry(EntryStack.of(VanillaEntryTypes.ITEM,new ItemStack(ModItems.PREPAID_CARD.get())));
        registry.removeEntry(EntryStack.of(VanillaEntryTypes.ITEM,new ItemStack(ModItems.ATM_CARD.get())));
        List<EntryStack<ItemStack>> allCards = new ArrayList<>();
        for(Color color : Color.values())
        {
            ItemStack atmCard = new ItemStack(ModItems.ATM_CARD.get());
            ColoredItem.setItemColor(atmCard,color.hexColor);
            allCards.add(EntryStack.of(VanillaEntryTypes.ITEM,atmCard));
        }
        registry.addEntries(allCards);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        //Coin Mint gets a special transfer handler as it has to manually trigger the recalculation of the loaded recipe
        registry.register(CoinMintTransferHandler.INSTANCE);
        //Ticket Station handler
        registry.register(SimpleTransferHandler.create(
                TicketStationMenu.class,
                TicketStationCategory.ID,
                new SimpleTransferHandler.IntRange(0,2)
        ));
    }

    @Override
    public void registerExclusionZones(ExclusionZones zones) {
        //Trader Screen (bottom left corner where the "back to terminal
        zones.register(TraderScreen.class,screen -> Lists.newArrayList(new Rectangle(screen.getGuiLeft() - 20, screen.getGuiTop() + screen.getYSize() - 60, 20, 60)));
        //Slot Machine Screen (same as trader screen)
        zones.register(SlotMachineScreen.class, screen -> Lists.newArrayList(new Rectangle(screen.getGuiLeft() - 20, screen.getGuiTop() + screen.getYSize() - 60, 20, 60)));
        //Trader Storage Screen Exclusion Zone
        zones.register(TraderStorageScreen.class, screen -> Lists.newArrayList(
                new Rectangle(screen.getGuiLeft() - 25, screen.getGuiTop(), 25, screen.getYSize()),
                new Rectangle(screen.getGuiLeft() + screen.getXSize(), screen.getGuiTop(), 25, screen.getYSize())
        ));
        //ATM Screen (left edge)
        zones.register(ATMScreen.class, screen -> Lists.newArrayList(new Rectangle(screen.getGuiLeft() - 25, screen.getGuiTop(), 25, screen.getYSize())));
        //Tax Collector Screen
        zones.register(TaxCollectorScreen.class, screen -> Lists.newArrayList(
                //Left Edge
                new Rectangle(screen.getGuiLeft() - 25, screen.getGuiTop(), 25, screen.getYSize()),
                //Collect Money button in top-right corner
                new Rectangle(screen.getGuiLeft() + screen.getXSize(), screen.getGuiTop(), 20, 20)
        ));
        //Coin Chest Screen (left edge)
        zones.register(CoinChestScreen.class, screen -> Lists.newArrayList(new Rectangle(screen.getGuiLeft() - 25, screen.getGuiTop(), 25, screen.getYSize())));
        //Ejection Screen (left/right buttons)
        zones.register(EjectionRecoveryScreen.class,screen -> Lists.newArrayList(new Rectangle(screen.getGuiLeft() - 20, screen.getGuiTop(), screen.getXSize() + 40, 20)));
        //Player Trader Screen (chat toggle in top-right)
        zones.register(PlayerTradeScreen.class,screen -> Lists.newArrayList(new Rectangle(screen.getGuiLeft() + screen.getXSize(), screen.getGuiTop(), 20, 20)));
        //Trader Interface Screen (two buttons in top-right)
        zones.register(TraderInterfaceScreen.class,screen -> Lists.newArrayList(new Rectangle(screen.getGuiLeft() + screen.getXSize(), screen.getGuiTop(), 20, 40)));
        //Wallet Screen (3 icon buttons at top left)
        zones.register(WalletScreen.class,screen -> Lists.newArrayList(new Rectangle(screen.getGuiLeft() - 20, screen.getGuiTop(), 20, 60)));
        //Wallet Bank Screen
        zones.register(WalletBankScreen.class,screen -> Lists.newArrayList(
                //Tabs on left edge
                new Rectangle(screen.getGuiLeft() - 25,screen.getGuiTop(), 25, 25 * screen.getTabCount()),
                //Button on rop-right
                new Rectangle(screen.getGuiLeft() + screen.getXSize(), screen.getGuiTop(), 20, 20)
        ));
        //Team Management Screen (entire outside edge)
        zones.register(TeamManagerScreen.class, screen -> Lists.newArrayList(new Rectangle(screen.getGuiLeft() - 25, screen.getGuiTop() - 25, screen.getXSize() + 50, screen.getYSize() + 50)));
        //Notification Screen (left edge)
        zones.register(NotificationScreen.class, screen -> Lists.newArrayList(new Rectangle(screen.getGuiLeft() - 25, screen.getGuiTop(), 25, screen.getYSize())));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void registerCollapsibleEntries(CollapsibleEntryRegistry registry) {
        //Shelf
        registry.group(VersionUtil.lcResource("rei_groups/shelves"),LCText.REI_GROUP_SHELF.get(),isInBundle(ModBlocks.SHELF));
        //Double Shelf
        registry.group(VersionUtil.lcResource("rei_groups/double_shelves"),LCText.REI_GROUP_SHELF_2x2.get(),isInBundle(ModBlocks.SHELF_2x2));
        //Card Displays
        ModBlocks.CARD_DISPLAY.forEachKey1(woodType ->
                registry.group(VersionUtil.lcResource(woodType.generateID("rei_groups/card_displays")),LCText.REI_GROUP_CARD_DISPLAY.get(woodType).get(),isInBundle(ModBlocks.CARD_DISPLAY,woodType))
        );
        //Vending Machine
        registry.group(VersionUtil.lcResource("rei_groups/vending_machine"),LCText.REI_GROUP_VENDING_MACHINE.get(),isInBundle(ModBlocks.VENDING_MACHINE));
        //Large Vending Machines
        registry.group(VersionUtil.lcResource("rei_groups/large_vending_machine"),LCText.REI_GROUP_LARGE_VENDING_MACHINE.get(),isInBundle(ModBlocks.VENDING_MACHINE_LARGE));
        //Freezers
        registry.group(VersionUtil.lcResource("rei_groups/freezer"),LCText.REI_GROUP_FREEZER.get(),isInBundle(ModBlocks.FREEZER));
        //Bookshelves
        registry.group(VersionUtil.lcResource("rei_groups/bookshelves"),LCText.REI_GROUP_BOOKSHELF_TRADER.get(),isInBundle(ModBlocks.BOOKSHELF_TRADER));
        //Auction Stands
        registry.group(VersionUtil.lcResource("rei_groups/auction_stand"),LCText.REI_GROUP_AUCTION_STAND.get(),isInBundle(ModBlocks.AUCTION_STAND));

        //Jar of Sus
        registry.group(VersionUtil.lcResource("rei_groups/jar_of_sus"),LCText.REI_GROUP_JAR_OF_SUS.get(),isItem(ModBlocks.SUS_JAR));

        //ATM Card
        registry.group(VersionUtil.lcResource("rei_groups/atm_card"), LCText.REI_GROUP_ATM_CARD.get(), isItem(ModItems.ATM_CARD));

    }

    private Predicate<? extends EntryStack<?>> isItem(@Nonnull Supplier<? extends ItemLike> item)
    {
        return entryStack -> {
            if(entryStack.getValue() instanceof ItemStack stack)
                return item.get().asItem() == stack.getItem();
            return false;
        };
    }

    private Predicate<? extends EntryStack<?>> isInBundle(@Nonnull RegistryObjectBundle<? extends ItemLike,?> bundle)
    {
        return entryStack -> {
            if(entryStack.getValue() instanceof ItemStack stack)
                return bundle.getAll().stream().anyMatch(i -> i.asItem() == stack.getItem());
            return false;
        };
    }

    private <T> Predicate<? extends EntryStack<?>> isInBundle(@Nonnull RegistryObjectBiBundle<? extends ItemLike,T,?> bundle, T subSection)
    {
        return entryStack -> {
            if(entryStack.getValue() instanceof ItemStack stack)
                return bundle.getAll(subSection).stream().anyMatch(i -> i.asItem() == stack.getItem());
            return false;
        };
    }
}