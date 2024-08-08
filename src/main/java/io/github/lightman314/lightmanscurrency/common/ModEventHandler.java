package io.github.lightman314.lightmanscurrency.common;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.capability.money.CapabilityMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.capability.money.CapabilityMoneyViewer;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.CapabilityInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.util.BlockEntityBlockHelper;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import javax.annotation.Nonnull;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD,modid = LightmansCurrency.MODID)
public class ModEventHandler {

    @SubscribeEvent
    private static void registerCapabilityProviders(@Nonnull RegisterCapabilitiesEvent event)
    {
        //Item Handlers
        //Register Item Handler for Item Traders
        TraderBlockEntity.easyRegisterCapProvider(event, Capabilities.ItemHandler.BLOCK, (t, s) -> {
            if(t instanceof ItemTraderData itemTrader)
                return itemTrader.getItemHandler(s);
            return null;
        }, BlockEntityBlockHelper.getBlocksForBlockEntities(BlockEntityBlockHelper.ITEM_TRADER_TYPE,BlockEntityBlockHelper.FREEZER_TRADER_TYPE,BlockEntityBlockHelper.ARMOR_TRADER_TYPE,BlockEntityBlockHelper.TICKET_KIOSK_TYPE,BlockEntityBlockHelper.BOOKSHELF_TRADER_TYPE));
        //Register Item Handler for Slot Machine
        TraderBlockEntity.easyRegisterCapProvider(event, Capabilities.ItemHandler.BLOCK, (t,s) -> {
            if(t instanceof SlotMachineTraderData slotMachine)
                return slotMachine.getItemHandler(s);
            return null;
        }, ModBlocks.SLOT_MACHINE.get());
        //Register Item Handlers for capability interface blocks
        CapabilityInterfaceBlockEntity.easyRegisterCapProvider(event,Capabilities.ItemHandler.BLOCK);
        //Register Item Handlers for the Item Trader Interface
        IRotatableBlock.registerRotatableCapability(event,Capabilities.ItemHandler.BLOCK, ModBlockEntities.TRADER_INTERFACE_ITEM.get(), (be, relativeSide) -> be.getItemHandler().getHandler(relativeSide));

        //Item Handler for Coin Chest
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.COIN_CHEST.get(),(be,direction) -> be.getItemHandler());
        //Item Viewer for Coin Jar
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.COIN_JAR.get(), (b,s) -> b.getViewer());
        //Item Handler for Coin Mint
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.COIN_MINT.get(), (mint,side) -> mint.getItemHandler());

        //Money-related capabilities
        //Money Viewer for Wallets
        event.registerItem(CapabilityMoneyViewer.MONEY_VIEWER_ITEM,(stack, c) -> WalletItem.getDataWrapper(stack),
                ModItems.WALLET_COPPER.get(),ModItems.WALLET_IRON.get(),ModItems.WALLET_GOLD.get(),
                ModItems.WALLET_EMERALD.get(),ModItems.WALLET_DIAMOND.get(),ModItems.WALLET_NETHERITE.get());
        //Money Handler/Viewers for Players
        event.registerEntity(CapabilityMoneyHandler.MONEY_HANDLER_ENTITY, EntityType.PLAYER, (player, c) -> MoneyAPI.API.GetPlayersMoneyHandler(player));
        event.registerEntity(CapabilityMoneyViewer.MONEY_VIEWER_ENTITY, EntityType.PLAYER, (player,c) -> MoneyAPI.API.GetPlayersMoneyHandler(player));

    }

}
