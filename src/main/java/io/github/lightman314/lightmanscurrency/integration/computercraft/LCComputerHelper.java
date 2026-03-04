package io.github.lightman314.lightmanscurrency.integration.computercraft;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.events.TraderEvent;
import io.github.lightman314.lightmanscurrency.api.stats.StatType;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.*;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.CapabilityInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.nodes.GachaNode;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.nodes.GachaStorageNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.ItemStorageNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.ItemTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.nodes.PaygateTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.nodes.TicketStubNode;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.nodes.SlotMachineNode;
import io.github.lightman314.lightmanscurrency.integration.computercraft.apis.LuaMoneyAPI;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.BasicItemParser;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.builtin.AncientCoinParser;
import io.github.lightman314.lightmanscurrency.integration.computercraft.detail_providers.AncientCoinDetailProvider;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.MoneyChestPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.atm.ATMPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.CashRegisterPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.TerminalPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.auction.AuctionHousePeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.gacha.GachaNodeMethods;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.gacha.GachaStorageNodeMethods;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.item.ItemStorageNodeMethods;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.item.ItemTradeNodeMethods;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.TraderNodeMethods;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes.builtin.*;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.paygate.PaygateTradeNodeMethods;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.paygate.TicketStubNodeMethods;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.slot_machine.SlotMachineNodeMethods;
import io.github.lightman314.lightmanscurrency.integration.computercraft.pocket_upgrades.LCPocketUpgrades;
import io.github.lightman314.lightmanscurrency.integration.computercraft.stats.StatReaders;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;


public class LCComputerHelper {

    private static final List<BasicItemParser> itemParsers = new ArrayList<>();
    private static final List<TraderPeripheralSource> peripheralSources = new ArrayList<>();
    private static final List<TraderNodeMethods.Source> nodeSources = new ArrayList<>();
    private static final List<Function<StatType.Instance<?,?>,Object>> statReaders = new ArrayList<>();

    public static void setup(IEventBus modBus)
    {
        LCPocketUpgrades.init(modBus);
        //Register globals
        ComputerCraftAPI.registerAPIFactory(LuaMoneyAPI.FACTORY);
        //Register detail providers
        VanillaDetailRegistries.ITEM_STACK.addProvider(AncientCoinDetailProvider.INSTANCE);
        //Register Item Parsers
        registerItemParser(AncientCoinParser.INSTANCE);
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
        //Register Nodes
        registerTraderNodeMethod(TraderNodeMethods.easySource(AlliesNode.TYPE,AllyNodeMethods::new));
        registerTraderNodeMethod(TraderNodeMethods.easySource(BankNode.TYPE,BankNodeMethods::new));
        registerTraderNodeMethod(TraderNodeMethods.easySource(DisplayNode.TYPE,DisplayNodeMethods::new));
        registerTraderNodeMethod(TraderNodeMethods.easySource(InputNode.TYPE,InputNodeMethods::new));
        registerTraderNodeMethod(TraderNodeMethods.easySource(LoggerNode.TYPE,LoggerNodeMethods::new));
        registerTraderNodeMethod(TraderNodeMethods.easySource(TaxesNode.TYPE,TaxesNodeMethods::new));

        registerTraderNodeMethod(TraderNodeMethods.easySource(GachaNode.TYPE,GachaNodeMethods::new));
        registerTraderNodeMethod(TraderNodeMethods.easySource(GachaStorageNode.TYPE,GachaStorageNodeMethods::new));

        registerTraderNodeMethod(TraderNodeMethods.easySource(ItemStorageNode.TYPE,ItemStorageNodeMethods::new));
        registerTraderNodeMethod(TraderNodeMethods.easySource(ItemTradeNode.TYPE,ItemTradeNodeMethods::new));

        registerTraderNodeMethod(TraderNodeMethods.easySource(PaygateTradeNode.TYPE,PaygateTradeNodeMethods::new));
        registerTraderNodeMethod(TraderNodeMethods.easySource(TicketStubNode.TYPE,TicketStubNodeMethods::new));

        registerTraderNodeMethod(TraderNodeMethods.easySource(SlotMachineNode.TYPE,SlotMachineNodeMethods::new));

        //Register Custom Stat Displays
        registerStatDisplay(StatReaders::parseStat);

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
        TraderData trader = be.getTraderData();
        if(trader != null)
            return new TraderPeripheral(trader);
        return null;
    }

    public static AccessTrackingPeripheral getPeripheral(TraderData trader) {
        for(TraderPeripheralSource source : peripheralSources)
        {
            AccessTrackingPeripheral result = source.tryCreate(trader);
            if(result != null)
                return result;
        }
        return new TraderPeripheral(trader);
    }

    public static void registerTraderNodeMethod(TraderNodeMethods.Source source) { nodeSources.add(source); }

    public static List<TraderNodeMethods<?>> getTraderNodeMethods(TraderPeripheral peripheral)
    {
        List<TraderNodeMethods<?>> results = new ArrayList<>();
        for(TraderNodeMethods.Source s : nodeSources)
            s.addTraderNodeMethods(peripheral,results::add);
        return results;
    }

    public static void registerStatDisplay(Function<StatType.Instance<?,?>,Object> parser) {
        statReaders.add(parser);
    }

    public static Object getStatDisplay(StatType.Instance<?,?> entry) {
        for(var parser : statReaders)
        {
            Object result = parser.apply(entry);
            if(result != null)
                return result;
        }
        Object display = entry.getDisplay();
        Object result = entry.toString();
        if(display instanceof Component text)
            result = text.getString();
        if(display instanceof Number || display instanceof Boolean)
            result = display;
        return result;
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
        //Register Money Chest Peripheral
        event.registerBlockEntity(PeripheralCapability.get(),ModBlockEntities.COIN_CHEST.get(),(be,side) -> new MoneyChestPeripheral(be));
    }

    private static void addTraderAttachments(TraderEvent.RegisterNodesEvent event)
    {
        if(event.getTrader() instanceof AuctionHouseTrader)
            return;
        event.addNode(MachineAccessNode.TYPE);
    }

    public static <T extends TraderBlockEntity<?>> void registerTraderCapability(RegisterCapabilitiesEvent event, Supplier<BlockEntityType<T>> type) { registerTraderCapability(event,type.get()); }
    public static void registerTraderCapability(RegisterCapabilitiesEvent event, BlockEntityType<? extends TraderBlockEntity<?>> type) {
        event.registerBlockEntity(PeripheralCapability.get(),type,(be,side) -> getPeripheral(be));
    }

    public static void registerItemParser(BasicItemParser parser) { itemParsers.add(parser); }
    public static void modifyItemParsing(ItemStack input, Map<?,?> table) throws LuaException
    {
        for(BasicItemParser p : itemParsers)
            p.modifyResult(input,table);
    }

}
