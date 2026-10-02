package io.github.lightman314.lightmanscurrency.api.stats.interfaces;

import io.github.lightman314.lightmanscurrency.api.stats.StatKey;

public interface StatListener {

    <V,T> void addToStat(StatKey<V,T> key, T addValue);

}
