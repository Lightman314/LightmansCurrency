package io.github.lightman314.lightmanscurrency.integration.computercraft;

import dan200.computercraft.api.ForgeComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.peripheral.IPeripheralProvider;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.blockentity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.AuctionStandBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.CapabilityInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.CashRegisterBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.GachaMachineBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.TerminalBlock;
import io.github.lightman314.lightmanscurrency.common.traders.InputTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaTrader;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.CashRegisterPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.TerminalPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.InputTraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.auction.AuctionHousePeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.gacha_machine.GachaMachinePeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.item.ItemTraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.paygate.PaygatePeripheral;
import io.github.lightman314.lightmanscurrency.integration.computercraft.pocket_upgrades.LCPocketUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.ArrayList;
import java.util.List;

public class LCComputerHelper {

    public static List<TraderPeripheralSource> peripheralSources = new ArrayList<>();

    public static void setup(IEventBus modBus)
    {
        LCPocketUpgrades.init(modBus);
        //Register Event Listener
        modBus.addListener(LCComputerHelper::registerPeripheralProviders);
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

    public static IPeripheral getPeripheral(TraderData trader) {
        for(TraderPeripheralSource source : peripheralSources)
        {
            IPeripheral result = source.tryCreate(trader);
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

    private static class PeripheralProvider implements IPeripheralProvider
    {

        @Override
        public LazyOptional<IPeripheral> getPeripheral(Level level, BlockPos blockPos, Direction direction) {
            //Terminal
            BlockState state = level.getBlockState(blockPos);
            if(state.getBlock() instanceof TerminalBlock terminal)
                return LazyOptional.of(TerminalPeripheral::new);
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
                return LazyOptional.of(() -> AuctionHousePeripheral.INSTANCE);
            //Cash Register
            if(be instanceof CashRegisterBlockEntity cr)
                return LazyOptional.of(() -> new CashRegisterPeripheral(cr));
            return LazyOptional.empty();
        }

    }

}