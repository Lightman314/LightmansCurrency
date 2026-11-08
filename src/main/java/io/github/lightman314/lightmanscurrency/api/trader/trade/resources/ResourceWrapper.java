package io.github.lightman314.lightmanscurrency.api.trader.trade.resources;

import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;

public abstract class ResourceWrapper<X,T> {

    public abstract ResourceType<?,X> getWrappedResource();
    public abstract ResourceType<T,T> getResultingResource();

    public abstract T wrapResource(TradeContext context,X result);
    public final T wrapResourceUnsafe(TradeContext context,Object result) throws ClassCastException { return this.wrapResource(context,(X)result); }

}