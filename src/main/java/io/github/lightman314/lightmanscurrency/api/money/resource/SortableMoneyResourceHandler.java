package io.github.lightman314.lightmanscurrency.api.money.resource;

import io.github.lightman314.lightmanscurrency.api.helpers.ListHelper;
import io.github.lightman314.lightmanscurrency.api.money.MoneyDisplayHelper;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public interface SortableMoneyResourceHandler extends MoneyResourceHandler {

    /**
     * Sorting priority for inserting money.<br>
     * Higher values are given to last.
     */
    default int insertSortPriority() { return 0; }

    /**
     * Sorting priority for extracting money.<br>
     * Higher values are extracted from last.
     */
    default int extractSortPriorty() { return this.insertSortPriority() * -1; }

    /**
     * The title used to note where the money in this resource handler is sourced from.
     */
    Component getMoneyCategoryTitle();

    /**
     * Adds text to the tooltip detailing the contents of the Money Resource Handler.<br>
     * Typically formatted as: <code><br>Title<br>Contents 1<br>Contents 2<br>etc.</code>
     * @see #defaultTooltipFormat(Consumer, Component, MoneyResourceHandler)
     */
    default void formatTooltip(Consumer<Component> builder)
    {
        defaultTooltipFormat(builder,this.getMoneyCategoryTitle(),this);
    }

    /**
     * Applies the default tooltip formatting to the given tooltip builder
     * @param builder The tooltip builder used to add new lines to the tooltip
     * @param title The title of the source
     * @param contents The MoneyResourceHandler that actually contains the contents
     */
    static void defaultTooltipFormat(Consumer<Component> builder, Component title, MoneyResourceHandler contents)
    {
        if(contents.isEmpty())
            return;
        builder.accept(title);
        ListHelper.consumeAll(builder,MoneyDisplayHelper.contentsAsMultiLineText(contents));
    }

    static void sortInsertFirst(List<SortableMoneyResourceHandler> list) { list.sort(Comparator.comparingInt(SortableMoneyResourceHandler::insertSortPriority)); }
    static void sortExtractFirst(List<SortableMoneyResourceHandler> list) { list.sort(Comparator.comparingInt(SortableMoneyResourceHandler::extractSortPriorty)); }

    static SortableMoneyResourceHandler wrapHandler(MoneyResourceHandler handler, Component title) { return wrapHandler(handler,title,0,0); }
    static SortableMoneyResourceHandler wrapHandler(MoneyResourceHandler handler, Component title, int priority) { return wrapHandler(handler,title,priority,priority * -1); }
    static SortableMoneyResourceHandler wrapHandler(MoneyResourceHandler handler, Component title, int insertPriority, int extractPriority) { return new Wrapper(handler,title,insertPriority,extractPriority); }

    interface Deferred extends SortableMoneyResourceHandler, DeferredMoneyResourceHandler {  }

    final class Wrapper implements SortableMoneyResourceHandler
    {
        private final MoneyResourceHandler handler;
        private final Component title;
        private final int insertPriority;
        private final int extractPriority;
        private Wrapper(MoneyResourceHandler handler,Component title,int insertPriority,int extractPriority) {
            this.handler = handler;
            this.title = title;
            this.insertPriority = insertPriority;
            this.extractPriority = extractPriority;
        }

        @Override
        public int insertSortPriority() { return this.insertPriority; }
        @Override
        public int extractSortPriorty() { return this.extractPriority; }
        @Override
        public Component getMoneyCategoryTitle() {return this.title; }
        @Override
        public List<MoneyValue> getAllResources() { return this.handler.getAllResources(); }
        @Override
        public MoneyValue getResource(MoneyKey key) { return this.handler.getResource(key); }
        @Override
        public boolean isEmpty() { return this.handler.isEmpty(); }
        @Override
        public boolean containsResource(MoneyValue value) { return this.handler.containsResource(value); }
        @Override
        public MoneyValue capValue(MoneyValue value) { return this.handler.capValue(value); }
        @Override
        public MoneyValue insert(MoneyValue value, TransactionContext transaction) { return this.handler.insert(value,transaction); }
        @Override
        public MoneyValue extract(MoneyValue value, TransactionContext transaction) { return this.handler.extract(value,transaction); }

    }

}
