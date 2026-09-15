package io.github.lightman314.lightmanscurrency.api.client.gui.widget.trader;

import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface TradeTooltipBuilder {

    void add(Component line);
    default void add(List<Component> lines) { lines.forEach(this::add); }

    void addInfo(Component info);
    default void addInfo(List<Component> lines) { lines.forEach(this::addInfo); }

    default void merge(Results results) {
        this.add(results.getNormalLines());
        this.addInfo(results.getInfoLines());
    }

    Results build();

    static TradeTooltipBuilder create() { return new Implementation(); }

    interface Results extends TradeTooltipBuilder {

        boolean isEmpty();

        default List<Component> getAllLines() {
            List<Component> result = new ArrayList<>(this.getNormalLines());
            result.addAll(this.getInfoLinesWithHeader());
            return Collections.unmodifiableList(result);
        }
        List<Component> getNormalLines();
        List<Component> getInfoLines();
        default List<Component> getInfoLinesWithHeader() {
            List<Component> result = this.getInfoLines();
            if(result.isEmpty())
                return result;
            result = new ArrayList<>(result);
            result.addFirst(TradeData.TOOLTIP_TRADE_INFO_TITLE.getWithStyle(ChatFormatting.YELLOW));
            return Collections.unmodifiableList(result);
        }

    }

    final class Implementation implements Results {

        private Implementation() {}

        private final List<Component> normalLines = new ArrayList<>();
        private final List<Component> infoLines = new ArrayList<>();
        @Override
        public void add(Component line) { this.normalLines.add(line); }
        @Override
        public void addInfo(Component info) { this.infoLines.add(info); }
        @Override
        public boolean isEmpty() { return this.normalLines.isEmpty() && this.infoLines.isEmpty(); }

        @Override
        public List<Component> getNormalLines() { return Collections.unmodifiableList(this.normalLines); }
        @Override
        public List<Component> getInfoLines() {
            return Collections.unmodifiableList(this.infoLines);
        }
        @Override
        public Results build() { return this; }
    }

}