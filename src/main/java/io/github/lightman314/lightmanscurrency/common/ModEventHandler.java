package io.github.lightman314.lightmanscurrency.common;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.capability.CapabilityMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.capability.CapabilityMoneyViewer;
import io.github.lightman314.lightmanscurrency.api.variants.block.CapabilityVariantData;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.capability.implementations.MoneyViewWrapper;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.util.BlockEntityBlockHelper;
import io.github.lightman314.lightmanscurrency.common.items.cards.ATMCardMoneyHandler;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.items.cards.PrepaidCardMoneyHandler;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaTrader;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;

@EventBusSubscriber(modid = LightmansCurrency.MODID)
public class ModEventHandler {

    @SubscribeEvent
    private static void registerRegistries(NewRegistryEvent event)
    {
        event.register(LCRegistries.EJECTION_DATA);
        event.register(LCRegistries.CUSTOM_DATA);
        event.register(LCRegistries.NOTIFICATION_TYPES);
        event.register(LCRegistries.NOTIFICATION_CATEGORIES);
        event.register(LCRegistries.TRADER_TYPES);
        event.register(LCRegistries.TRADER_NODE);
        event.register(LCRegistries.TRADE_RULE);
        event.register(LCRegistries.ITEM_TRADE);
        event.register(LCRegistries.OWNER_TYPES);
        event.register(LCRegistries.BANK_REFERENCE);
        event.register(LCRegistries.STAT_TYPES);
        event.register(LCRegistries.LAZY_PACKETS);
        event.register(LCRegistries.ICON_TYPE);
        event.register(LCRegistries.ATM_ICON_TYPE);
    }

    @SubscribeEvent
    private static void registerCapabilityProviders(RegisterCapabilitiesEvent event)
    {
        //Item Handlers
        //Register Item Handler for Item Traders
        TraderBlockEntity.easyRegisterCapProvider(event,Capabilities.ItemHandler.BLOCK,(t, s) -> {
            if(t instanceof ItemTraderData itemTrader)
                return itemTrader.getItemHandler(s);
            return null;
        },BlockEntityBlockHelper.getBlocksForBlockEntities(BlockEntityBlockHelper.ITEM_TRADER_TYPE,BlockEntityBlockHelper.FREEZER_TRADER_TYPE,BlockEntityBlockHelper.ARMOR_TRADER_TYPE,BlockEntityBlockHelper.TICKET_KIOSK_TYPE,BlockEntityBlockHelper.BOOKSHELF_TRADER_TYPE));
        //Register Item Handler for Slot Machine
        TraderBlockEntity.easyRegisterCapProvider(event,Capabilities.ItemHandler.BLOCK,(t,s) -> {
            if(t instanceof SlotMachineTraderData slotMachine)
                return slotMachine.getItemHandler(s);
            return null;
        },ModBlocks.SLOT_MACHINE.get());
        //Register Item Handler for Gacha Machine
        TraderBlockEntity.easyRegisterCapProvider(event,Capabilities.ItemHandler.BLOCK,(t,s) -> {
            if(t instanceof GachaTrader gachaMachine)
                return gachaMachine.getItemHandler(s);
            return null;
        },BlockEntityBlockHelper.getBlocksForBlockEntity(BlockEntityBlockHelper.GACHA_MACHINE_TYPE));

        //Register Item Handlers for the Item Trader Interface
        IRotatableBlock.registerRotatableCapability(event,Capabilities.ItemHandler.BLOCK,ModBlockEntities.TRADER_INTERFACE_ITEM.get(), (be, relativeSide) -> be.getItemHandler().getHandler(relativeSide));

        //Item Handler & Money Viewer for Coin Chest
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.COIN_CHEST.get(),(be,direction) -> be.getStorage());
        event.registerBlockEntity(CapabilityMoneyViewer.MONEY_VIEWER_BLOCK, ModBlockEntities.COIN_CHEST.get(),(be,direction) -> be.moneyViewer);
        //Item & Money Viewer for Coin Jar
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.COIN_JAR.get(), (b,s) -> b.getViewer());
        event.registerBlockEntity(CapabilityMoneyViewer.MONEY_VIEWER_BLOCK,ModBlockEntities.COIN_JAR.get(),(be,s) -> be.getMoneyViewer());
        //Item Handler for Coin Mint
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.COIN_MINT.get(), (mint,side) -> mint.getStorage());

        //Item Viewer for the Money Bag
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.MONEY_BAG.get(), (b,s) -> b.viewer);
        event.registerBlockEntity(CapabilityMoneyViewer.MONEY_VIEWER_BLOCK, ModBlockEntities.MONEY_BAG.get(), (b,s) -> b.moneyViewer);

        //Money-related capabilities
        //Money Viewer for Wallets
        event.registerItem(CapabilityMoneyViewer.MONEY_VIEWER_ITEM,(stack, c) -> MoneyViewWrapper.forInventory(WalletItem.getWalletInventory(stack),IClientTracker.forClient()),
                ModItems.WALLET_COPPER.get(),ModItems.WALLET_IRON.get(),ModItems.WALLET_GOLD.get(),
                ModItems.WALLET_EMERALD.get(),ModItems.WALLET_DIAMOND.get(),ModItems.WALLET_NETHERITE.get(),
                ModItems.WALLET_NETHER_STAR.get());
        //Money Handler/Viewers for Players
        event.registerEntity(CapabilityMoneyHandler.MONEY_HANDLER_ENTITY, EntityType.PLAYER, (player, c) -> MoneyAPI.getApi().GetPlayersMoneyHandler(player));
        event.registerEntity(CapabilityMoneyViewer.MONEY_VIEWER_ENTITY, EntityType.PLAYER, (player,c) -> MoneyAPI.getApi().GetPlayersMoneyHandler(player));

        //Money Handler for the Bank Card
        event.registerItem(CapabilityMoneyHandler.MONEY_HANDLER_ITEM, (stack,c) -> new ATMCardMoneyHandler(stack),ModItems.ATM_CARD.get());
        event.registerItem(CapabilityMoneyHandler.MONEY_HANDLER_ITEM, (stack,c) -> new PrepaidCardMoneyHandler(stack),ModItems.PREPAID_CARD.get());

        //Register variant capabilities for my blocks
        CapabilityVariantData.registerNormalBlock(event,ModBlocks.TERMINAL.get(),ModBlocks.GEM_TERMINAL.get());
        CapabilityVariantData.registerLCMultiBlock(event,ModBlocks.ATM.get());

    }

}
