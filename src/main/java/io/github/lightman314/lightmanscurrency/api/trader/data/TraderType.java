package io.github.lightman314.lightmanscurrency.api.trader.data;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.AbstractType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.NodeCollector;
import net.minecraft.core.Registry;

public abstract class TraderType extends AbstractType<TraderType> {

    public abstract void addNodes(NodeCollector collector);

    @Override
    protected Registry<TraderType> getRegistry() { return LCRegistries.Trader.TRADER_TYPES; }
    @Override
    protected String getName() { return "TraderType"; }
    @Override
    protected final TraderType getEntry() { return super.getEntry(); }

}