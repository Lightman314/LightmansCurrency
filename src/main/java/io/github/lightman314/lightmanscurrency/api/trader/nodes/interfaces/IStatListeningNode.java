package io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.stats.StatKey;

public interface IStatListeningNode {

    <V,A> void afterStatAdded(StatKey<V,A> key, A addValue);

}
