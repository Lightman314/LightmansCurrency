package io.github.lightman314.lightmanscurrency.integration.computercraft;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.ForgeComputerCraftAPI;
import dan200.computercraft.api.detail.VanillaDetailRegistries;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.events.TraderEvent;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.attachments.builtin.ExternalAuthorizationAttachment;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.AuctionStandBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.CapabilityInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.CashRegisterBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.GachaMachineBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.SlotMachineTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.TerminalBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.traders.input.InputTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaTrader;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.apis.LuaMoneyAPI;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.BasicItemParser;
import io.github.lightman314.lightmanscurrency.integration.computercraft.data.builtin.AncientCoinParser;
import io.github.lightman314.lightmanscurrency.integration.computercraft.detail_providers.AncientCoinDetailProvider;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.CashRegisterPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.TerminalPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.atm.ATMPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.InputTraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.auction.AuctionHousePeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.gacha_machine.GachaMachinePeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.item.ItemTraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.paygate.PaygatePeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.slot_machine.SlotMachinePeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.pocket_upgrades.LCPocketUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LCComputerHelper {

    private static final List<BasicItemParser> itemParsers = new ArrayList<>();
    private static final List<TraderPeripheralSource> peripheralSources = new ArrayList<>();

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
        modBus.addListener(LCComputerHelper::registerPeripheralProviders);
        MinecraftForge.EVENT_BUS.addListener(LCComputerHelper::addTraderAttachments);
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

    public static AccessTrackingPeripheral getPeripheral(TraderBlockEntity<?> be) {
        for(TraderPeripheralSource source : peripheralSources)
        {
            AccessTrackingPeripheral result = source.tryCreate(be);
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

    private static void registerPeripheralProviders(FMLCommonSetupEvent event)
    {
        //Create peripheral capability for all multi-blocks
        ForgeComputerCraftAPI.registerPeripheralProvider(new PeripheralProvider());
    }

    private static void addTraderAttachments(TraderEvent.RegisterAttachmentEvent event)
    {
        if(event.getTrader() instanceof AuctionHouseTrader)
            return;
        event.addAttachment(ExternalAuthorizationAttachment.TYPE);
    }

    private static class PeripheralProvider implements IPeripheralProvider
    {

        @Override
        public LazyOptional<IPeripheral> getPeripheral(Level level, BlockPos blockPos, Direction direction) {
            //Terminal
            BlockState state = level.getBlockState(blockPos);
            if(state.getBlock() instanceof TerminalBlock terminal)
                return TerminalPeripheral.LAZY;
            //Get true block entity from any multi-blocks
            BlockEntity be = level.getBlockEntity(blockPos);
            if(be instanceof CapabilityInterfaceBlockEntity cap)
                be = cap.tryGetCoreBlockEntity();
            //Traders
            if(be instanceof TraderBlockEntity<?> trader)
            {
                IPeripheral result = LCComputerHelper.getPeripheral(trader);
                if(result == null)
                    return LazyOptional.empty();
                return LazyOptional.of(() -> result);
            }
            //Auction Stands
            if(be instanceof AuctionStandBlockEntity ah && LCConfig.SERVER.auctionHouseEnabled.get())
                return AuctionHousePeripheral.LAZY;
            //Cash Register
            if(be instanceof CashRegisterBlockEntity cr)
                return LazyOptional.of(() -> new CashRegisterPeripheral(cr));
            //ATM
            if(state.getBlock() == ModBlocks.ATM.get())
                return ATMPeripheral.LAZY;
            return LazyOptional.empty();
        }

    }

    public static void registerItemParser(BasicItemParser parser) { itemParsers.add(parser); }

    public static void modifyItemParsing(ItemStack input,Map<?,?> table) throws LuaException
    {
        for (BasicItemParser p : itemParsers)
            p.modifyResult(input,table);
    }

}