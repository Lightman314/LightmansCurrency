package io.github.lightman314.lightmanscurrency.api.stats.interfaces;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface StatViewer {

    TextEntry GUI_STAT_LABEL = TextEntry.gui(LCApi.MODID,"stats.label");
    TextEntry GUI_STATS_EMPTY = TextEntry.gui(LCApi.MODID,"stats.empty");

    Set<StatKey<?,?>> getKeys();
    default List<StatKey<?,?>> getSortedKeys() {
        List<StatKey<?,?>> list = new ArrayList<>(this.getKeys());
        list.sort(StatKey.SORTER);
        return list;
    }
    <T> T getStat(StatKey<T,?> key);
    Component getStatValueText(StatKey<?,?> key);
    @Nullable
    List<Component> getStatValueTooltip(StatKey<?,?> key);

}
