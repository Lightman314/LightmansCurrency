package io.github.lightman314.lightmanscurrency.integration.computercraft;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.events.TraderEvent;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.attachments.builtin.ExternalAuthorizationAttachment;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.CapabilityInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.GachaMachineBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.SlotMachineTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.traders.input.InputTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaTrader;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.apis.LuaMoneyAPI;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.atm.ATMPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.CashRegisterPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.TerminalPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.InputTraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.auction.AuctionHousePeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.gacha_machine.GachaMachinePeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.item.ItemTraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.paygate.PaygatePeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.slot_machine.SlotMachinePeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.pocket_upgrades.LCPocketUpgrades;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class LCComputerHelper {

    public static List<TraderPeripheralSource> peripheralSources = new ArrayList<>();

    public static void setup(IEventBus modBus)
    {
        LCPocketUpgrades.init(modBus);
        //Register globals
        ComputerCraftAPI.registerAPIFactory(LuaMoneyAPI.FACTORY);
        //Register Event Listener
        modBus.addListener(LCComputerHelper::registerCapabilities);
        NeoForge.EVENT_BUS.addListener(LCComputerHelper::addTraderAttachments);
        //Create Trader Peripheral Sources
        //Auction House
        registerTraderPeripheralSource(TraderPeripheralSource.dataOnly((trader) -> {
            if(trader instanceof AuctionHouseTrader)
                return AuctionHousePeripheral.INSTANCE;
            return null;
        }));
        //Item Trader
        registerTraderPeripheralSource(TraderPeripheralSource.simple((be) -> {
            if(be instanceof ItemTraderBlockEntity itbe)
                return new ItemTraderPeripheral(itbe);
            return null;
        },(trader) -> {
            if(trader instanceof ItemTraderData itd)
                return new ItemTraderPeripheral(itd);
            return null;
        }));
        //Paygate
        registerTraderPeripheralSource(TraderPeripheralSource.blockOnly((be) -> {
            if(be instanceof PaygateBlockEntity paygate)
                return new PaygatePeripheral(paygate);
            return null;
        }));
        //Gacha Machine
        registerTraderPeripheralSource(TraderPeripheralSource.simple((be) -> {
            if(be instanceof GachaMachineBlockEntity gacha)
                return new GachaMachinePeripheral(gacha);
            return null;
        },(trader) -> {
            if(trader instanceof GachaTrader gacha)
                return new GachaMachinePeripheral(gacha);
            return null;
        }));
        //Slot Machine
        registerTraderPeripheralSource(TraderPeripheralSource.simple(be -> {
            if(be instanceof SlotMachineTraderBlockEntity slotMachine)
                return new SlotMachinePeripheral(slotMachine);
            return null;
        },trader -> {
            if(trader instanceof SlotMachineTraderData slotMachine)
                return new SlotMachinePeripheral(slotMachine);
            return null;
        }));
    }



    public static void registerTraderPeripheralSource(TraderPeripheralSource source)
    {
        if(!peripheralSources.contains(source))
            peripheralSources.add(source);
    }

    public static IPeripheral getPeripheral(TraderBlockEntity<?> be) {
        for(TraderPeripheralSource source : peripheralSources)
        {
            IPeripheral result = source.tryCreate(be);
            if(result != null)
                return result;
        }
        if(be.getTraderData() instanceof InputTraderData)
            return InputTraderPeripheral.createSimpleInput((TraderBlockEntity<InputTraderData>)be);
        return TraderPeripheral.createSimple((TraderBlockEntity<TraderData>)be);
    }

    public static AccessTrackingPeripheral getPeripheral(TraderData trader) {
        for(TraderPeripheralSource source : peripheralSources)
        {
            AccessTrackingPeripheral result = source.tryCreate(trader);
            if(result != null)
                return result;
        }
        if(trader instanceof InputTraderData it)
            return InputTraderPeripheral.createSimpleInput(it);
        return TraderPeripheral.createSimple(trader);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        //Create peripheral capability for all multi-blocks
        CapabilityInterfaceBlockEntity.easyRegisterCapProvider(event,PeripheralCapability.get());
        //Create peripherals for all traders
        registerTraderCapability(event,ModBlockEntities.ITEM_TRADER);
        registerTraderCapability(event,ModBlockEntities.ARMOR_TRADER);
        registerTraderCapability(event,ModBlockEntities.FREEZER_TRADER);
        registerTraderCapability(event,ModBlockEntities.TICKET_TRADER);
        registerTraderCapability(event,ModBlockEntities.BOOK_TRADER);
        registerTraderCapability(event,ModBlockEntities.SLOT_MACHINE_TRADER);
        registerTraderCapability(event,ModBlockEntities.GACHA_MACHINE);
        registerTraderCapability(event,ModBlockEntities.PAYGATE);
        //Register Terminal Peripheral
        event.registerBlock(PeripheralCapability.get(),(level,pos,state,be,side) -> TerminalPeripheral.INSTANCE,ModBlocks.TERMINAL.get(),ModBlocks.GEM_TERMINAL.get());
        //Register Auction Stand Peripheral
        event.registerBlockEntity(PeripheralCapability.get(),ModBlockEntities.AUCTION_STAND.get(),(be,side) -> {
            if(LCConfig.SERVER.auctionHouseEnabled.get())
                return AuctionHousePeripheral.INSTANCE;
            return null;
        });
        //Register Cash Register Peripheral
        event.registerBlockEntity(PeripheralCapability.get(),ModBlockEntities.CASH_REGISTER.get(),(be,side) -> new CashRegisterPeripheral(be));
        //Register ATM Peripheral
        event.registerBlock(PeripheralCapability.get(),(level,pos,state,be,side) -> ATMPeripheral.INSTANCE,ModBlocks.ATM.get());
    }

    private static void addTraderAttachments(TraderEvent.RegisterAttachmentEvent event)
    {
        if(event.getTrader() instanceof AuctionHouseTrader)
            return;
        event.addAttachment(ExternalAuthorizationAttachment.TYPE);
    }

    public static <T extends TraderBlockEntity<?>> void registerTraderCapability(RegisterCapabilitiesEvent event, Supplier<BlockEntityType<T>> type) { registerTraderCapability(event,type.get()); }
    public static void registerTraderCapability(RegisterCapabilitiesEvent event, BlockEntityType<? extends TraderBlockEntity<?>> type)
    {
        event.registerBlockEntity(PeripheralCapability.get(),type,(be,side) -> {
            if(be instanceof TraderBlockEntity<?> tbe)
                return getPeripheral(tbe);
            return null;
        });
    }

}
