package io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.nodes;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.integration.computercraft.LCPeripheralMethod;
import io.github.lightman314.lightmanscurrency.integration.computercraft.peripheral.trader.TraderPeripheral;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class TraderNodeMethods<T extends TraderNode> {

    protected final TraderPeripheral peripheral;
    public final TraderNodeType<T> type;
    public TraderNodeMethods(TraderPeripheral peripheral, TraderNodeType<T> type) {
        this.peripheral = peripheral;
        this.type = type;
    }

    protected final PlayerReference getFakePlayer(IComputerAccess computer) { return this.peripheral.getFakePlayer(computer); }

    protected final TraderData getTrader() throws LuaException { return this.peripheral.getTrader(); }
    protected final TraderData safeGetTrader() { return this.peripheral.safeGetTrader(); }

    protected final DataContext<Object> dataContext() { return this.peripheral.dataContext(); }

    protected final int getPermissionLevel(IComputerAccess computer,String permission) { return this.peripheral.getPermissionLevel(computer,permission); }
    protected final boolean hasPermissions(IComputerAccess computer,String permission) { return this.peripheral.hasPermissions(computer,permission); }

    public void addToTypes(Consumer<String> consumer) {
        ResourceLocation key = LCRegistries.TRADER_NODE.getKey(this.type);
        consumer.accept(key.getNamespace().equals(LightmansCurrency.MODID) ? "node_" + key.getPath() : key.withPrefix("node_").toString());
    }

    @Nullable
    protected T safeGetNode()
    {
        TraderData trader = this.peripheral.safeGetTrader();
        if(trader != null)
            return trader.getNode(this.type);
        return null;
    }
    protected T getNode() throws LuaException
    {
        T node = this.peripheral.getTrader().getNode(this.type);
        if(node == null)
            throw new LuaException("An unexpected error occurred trying to get the traders " + LCRegistries.TRADER_NODE.getKey(this.type) + " node!");
        return node;
    }

    public abstract void registerNodeMethods(LCPeripheralMethod.Registration registration);

    public interface Source {
        void addTraderNodeMethods(TraderPeripheral peripheral,Consumer<TraderNodeMethods<?>> builder);
    }

    public static <T extends TraderNode> Source easySource(TraderNodeType<T> type, Function<TraderPeripheral,TraderNodeMethods<T>> factory) {
        return (peripheral, builder) -> {
                TraderData trader = peripheral.safeGetTrader();
                if(trader != null && trader.hasNode(type))
                    builder.accept(factory.apply(peripheral));
        };
    }

}
