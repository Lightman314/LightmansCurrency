package io.github.lightman314.lightmanscurrency.api.trader.data;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.RegistryHelper;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.NodeCollector;


public abstract class TraderType {

    public abstract void addNodes(NodeCollector collector);

    @Override
    public int hashCode() { return RegistryHelper.hash(LCRegistries.Trader.TRADER_TYPES,this); }
    @Override
    public String toString() { return RegistryHelper.toString("TraderType",LCRegistries.Trader.TRADER_TYPES,this); }


}