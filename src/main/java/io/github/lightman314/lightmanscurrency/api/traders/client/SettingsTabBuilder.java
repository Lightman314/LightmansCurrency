package io.github.lightman314.lightmanscurrency.api.traders.client;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.SettingsSubTab;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class SettingsTabBuilder {

    private final List<Pair<SettingsSubTab,Integer>> tabs = new ArrayList<>();

    public void add(SettingsSubTab tab) { this.add(tab,0); }
    public void add(SettingsSubTab tab, int priority) { this.tabs.add(Pair.of(tab,priority)); }

    public List<SettingsSubTab> getResult() {
        this.tabs.sort(Comparator.comparingInt(Pair::getSecond));
        return this.tabs.stream().map(Pair::getFirst).toList();
    }

}
