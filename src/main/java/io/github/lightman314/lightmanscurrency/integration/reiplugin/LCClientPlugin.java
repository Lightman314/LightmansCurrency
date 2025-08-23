package io.github.lightman314.lightmanscurrency.integration.reiplugin;

import com.mojang.datafixers.util.Pair;
import dev.architectury.event.CompoundEventResult;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.NotificationScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.RecipeTypes;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import io.github.lightman314.lightmanscurrency.common.menus.TicketStationMenu;
import io.github.lightman314.lightmanscurrency.integration.reiplugin.coin_mint.CoinMintCategory;
import io.github.lightman314.lightmanscurrency.integration.reiplugin.coin_mint.CoinMintDisplay;
import io.github.lightman314.lightmanscurrency.integration.reiplugin.coin_mint.CoinMintTransferHandler;
import io.github.lightman314.lightmanscurrency.integration.reiplugin.ticket_station.TicketStationCategory;
import io.github.lightman314.lightmanscurrency.integration.reiplugin.ticket_station.TicketStationDisplay;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;

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
        //Focus Stack Provider
        registry.registerFocusedStack(((screen, mouse) -> {
            if(screen instanceof EasyMenuScreen<?> s)
            {
                ScreenPosition mousePos = ScreenPosition.of(mouse.x,mouse.y);
                Pair<ItemStack,ScreenArea> item = s.getHoveredItem(mousePos);
                if(item != null && !item.getFirst().isEmpty())
                    return CompoundEventResult.interruptTrue(EntryStack.of(VanillaEntryTypes.ITEM,item.getFirst()));
                Pair<FluidStack,ScreenArea> fluid = s.getHoveredFluid(mousePos);
                if(fluid != null && !fluid.getFirst().isEmpty())
                {
                    FluidStack f = fluid.getFirst();
                    return CompoundEventResult.interruptTrue(EntryStack.of(VanillaEntryTypes.FLUID,dev.architectury.fluid.FluidStack.create(f.getFluid(),f.getAmount(),f.getComponentsPatch())));
                }
            }
            return CompoundEventResult.pass();
        }));
    }

    @Override
    public void registerEntries(EntryRegistry registry) {
        registry.removeEntry(EntryStack.of(VanillaEntryTypes.ITEM,new ItemStack(ModItems.PREPAID_CARD.get())));
        registry.removeEntry(EntryStack.of(VanillaEntryTypes.ITEM,new ItemStack(ModItems.ATM_CARD.get())));
        List<EntryStack<ItemStack>> allCards = new ArrayList<>();
        for(Color color : Color.values())
        {
            ItemStack atmCard = new ItemStack(ModItems.ATM_CARD.get());
            atmCard.set(DataComponents.DYED_COLOR,new DyedItemColor(color.hexColor,true));
            allCards.add(EntryStack.of(VanillaEntryTypes.ITEM,atmCard));
        }
        registry.addEntries(allCards);
        registry.removeEntry(EntryStack.of(VanillaEntryTypes.ITEM,new ItemStack(ModItems.GACHA_BALL.get())));
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
        this.registerExclusionZones(zones,TraderScreen.class);
        this.registerExclusionZones(zones,SlotMachineScreen.class);
        this.registerExclusionZones(zones,GachaMachineScreen.class);
        this.registerExclusionZones(zones,TraderStorageScreen.class);
        this.registerExclusionZones(zones,ATMScreen.class);
        this.registerExclusionZones(zones,TaxCollectorScreen.class);
        this.registerExclusionZones(zones,CoinChestScreen.class);
        this.registerExclusionZones(zones,EjectionRecoveryScreen.class);
        this.registerExclusionZones(zones,PlayerTradeScreen.class);
        this.registerExclusionZones(zones,TraderInterfaceScreen.class);
        this.registerExclusionZones(zones,WalletScreen.class);
        this.registerExclusionZones(zones,WalletBankScreen.class);
        this.registerExclusionZones(zones,TeamManagerScreen.class);
        this.registerExclusionZones(zones,NotificationScreen.class);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void registerCollapsibleEntries(CollapsibleEntryRegistry registry) {
        //Display Case
        registry.group(VersionUtil.lcResource("rei_groups/display_case"),LCText.REI_GROUP_DISPLAY_CASE.get(),isInBundle(ModBlocks.DISPLAY_CASE));
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
        registry.group(VersionUtil.lcResource("rei_groups/atm_card"),LCText.REI_GROUP_ATM_CARD.get(),isItem(ModItems.ATM_CARD));

        //Ancient Coins
        registry.group(VersionUtil.lcResource("rei_groups/ancient_coins"),LCText.REI_GROUP_ANCIENT_COINS.get(),isItem(ModItems.COIN_ANCIENT));

        //Gacha Machines
        registry.group(VersionUtil.lcResource("rei_groups/gacha_machines"),LCText.REI_GROUP_GACHA_MACHINE.get(),isInBundle(ModBlocks.GACHA_MACHINE));

    }

    private <T extends EasyMenuScreen<?>>void registerExclusionZones(@Nonnull ExclusionZones zones, @Nonnull Class<T> clazz)
    {
        zones.register(clazz,screen -> {
            List<Rectangle> areas = new ArrayList<>();
            ScreenArea screenArea = screen.getArea();
            for(var child : screen.children())
            {
                if(child instanceof EasyWidget widget && widget.visible)
                {
                    ScreenArea area = widget.getArea();
                    if(screenArea.isOutside(area))
                        areas.add(new Rectangle(area.x,area.y,area.width,area.height));
                }
            }
            return areas;
        });
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
