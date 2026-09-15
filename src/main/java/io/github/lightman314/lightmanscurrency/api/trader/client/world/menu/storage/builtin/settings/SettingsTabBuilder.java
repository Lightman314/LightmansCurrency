package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple.SettingLabel;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple.SimpleSettingBuilder;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple.SimpleSettingCategory;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple.SimpleSettingTab;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public final class SettingsTabBuilder {

    private final List<Pair<Integer,Function<SettingsClientTab,SettingsSubTab>>> results = new ArrayList<>();

    private final Map<SimpleSettingCategory,List<SimpleSettingBuilder>> simpleSettings = new HashMap<>();

    public SettingsTabBuilder() { }

    void buildTabs(SettingsClientTab tab,Consumer<SettingsSubTab> builder) {
        this.results.sort(Comparator.comparingInt(Pair::getFirst));
        for(var pair : this.results)
            builder.accept(pair.getSecond().apply(tab));
    }

    public void addTab(Function<SettingsClientTab,SettingsSubTab> factory) { this.addTab(0,factory); }
    public void addTab(int sortPriority,Function<SettingsClientTab,SettingsSubTab> factory) {
        this.results.add(Pair.of(sortPriority,factory));
    }

    public void addSimpleSetting(SimpleSettingCategory category,SimpleSettingBuilder builder) {
        List<SimpleSettingBuilder> list = this.simpleSettings.computeIfAbsent(category,this::createSimpleCategory);
        list.add(builder);
    }

    //Quick label-builder
    public void addSimpleSettingLabel(SimpleSettingCategory category,TextEntry label) { this.addSimpleSettingLabel(category,label.get()); }
    public void addSimpleSettingLabel(SimpleSettingCategory category,Component label) { this.addSimpleSettingLabel(category,label,0xFF404040); }
    public void addSimpleSettingLabel(SimpleSettingCategory category,Component label,int textColor) { this.addSimpleSetting(category,new SettingLabel(label,textColor)); }

    private List<SimpleSettingBuilder> createSimpleCategory(SimpleSettingCategory category) {
        this.addTab(category.sortPriority(),(parent) -> new SimpleSettingTab(parent,category,this.simpleSettings.get(category)));
        return new ArrayList<>();
    }

}