package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.info;

import com.mojang.datafixers.util.Pair;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public final class InfoTabBuilder {

    private final List<Pair<Integer,Function<InfoClientTab,InfoClientSubTab>>> results = new ArrayList<>();

    public InfoTabBuilder() { }

    void buildTabs(InfoClientTab tab,Consumer<InfoClientSubTab> builder) {
        this.results.sort(Comparator.comparingInt(Pair::getFirst));
        for(var pair : this.results)
            builder.accept(pair.getSecond().apply(tab));
    }

    public void addTab(Function<InfoClientTab,InfoClientSubTab> factory) { this.addTab(0,factory); }
    public void addTab(int sortPriority,Function<InfoClientTab,InfoClientSubTab> factory) {
        this.results.add(Pair.of(sortPriority,factory));
    }

}