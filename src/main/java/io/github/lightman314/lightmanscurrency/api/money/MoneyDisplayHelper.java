package io.github.lightman314.lightmanscurrency.api.money;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.ListHelper;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueHelper;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class MoneyDisplayHelper {

    private MoneyDisplayHelper() {}

    /**
     * String variant of {@link #contentsAsMultiLineText(MoneyResourceHandler, ChatFormatting...)}<br>
     * Use text version when applicable, this is primarily here for debug purposes tbh
     */
    public static String contentsAsString(MoneyResourceHandler handler)
    {
        StringBuilder builder = new StringBuilder();
        for(Component line : contentsAsMultiLineText(handler))
        {
            if(!builder.isEmpty())
                builder.append('\n');
            builder.append(line.getString());
        }
        return builder.toString();
    }

    /**
     * Returns a list of {@link Component} entries for the contents of the given {@link MoneyResourceHandler}<br>
     * Utilizes {@link } where relevant
     * @param handler The Money Resource Handler whose contents should be displayed as text
     * @param style Any Chat Formatting Styles that should be applied
     */
    public static List<Component> contentsAsMultiLineText(MoneyResourceHandler handler, ChatFormatting... style)
    {
        List<Component> text = new ArrayList<>();
        List<MoneyValue> values = handler.getAllResources();
        for(MoneyValueHelper helper : LCRegistries.Money.VALUE_HELPER)
            helper.appendTextTooltips(text::add,values);
        return text;
    }

    /**
     * Gets a time-based cycling value from the Money Resource Handler's available money
     * @see MoneyResourceHandler#getAllResources()
     * @see ListHelper#cyclingValueFromList(List, Object)
     */
    public static MoneyValue getCyclingValue(MoneyResourceHandler handler) {
        return ListHelper.cyclingValueFromList(handler.getAllResources(),MoneyValue.empty());
    }

    /**
     * Gets the text of a time-based cycling value from the Money Resource Handlers available money<br>
     * Default empty text of "Empty Storage"
     * @see #getCyclingValue(MoneyResourceHandler)
     * @see #getCyclingValueText(MoneyResourceHandler,String)
     * @see #getCyclingValueText(MoneyResourceHandler,Component)
     */
    public static Component getCyclingValueText(MoneyResourceHandler handler) { return getCyclingValueText(handler, LCText.Money.GUI_MONEY_STORAGE_EMPTY.get()); }
    /**
     * Gets the text of a time-based cycling value from the Money Resource Handlers available money<br>
     * Will return the given emptyText if no money is available to display
     * @see #getCyclingValue(MoneyResourceHandler)
     * @see #getCyclingValueText(MoneyResourceHandler)
     * @see #getCyclingValueText(MoneyResourceHandler,Component)
     */
    public static Component getCyclingValueText(MoneyResourceHandler handler, String emptyText) { return getCyclingValueText(handler,Component.literal(emptyText)); }
    /**
     * Gets the text of a time-based cycling value from the Money Resource Hand'ers available money<br>
     * Will return the given emptyText if no money is available to display
     * @see #getCyclingValue(MoneyResourceHandler)
     * @see #getCyclingValueText(MoneyResourceHandler)
     * @see #getCyclingValueText(MoneyResourceHandler,String)
     */
    public static Component getCyclingValueText(MoneyResourceHandler handler, Component emptyText) { return getCyclingValue(handler).getText(emptyText); }

    /**
     * Gets the text of a time-based cycling line from the Money Resource Handlers available money<br>
     * Default empty text of "Empty Storage"
     * @see #contentsAsMultiLineText(MoneyResourceHandler, ChatFormatting...)
     * @see #getCyclingValueLine(MoneyResourceHandler,String)
     * @see #getCyclingValueLine(MoneyResourceHandler,Component)
     */
    public static Component getCyclingValueLine(MoneyResourceHandler handler) { return getCyclingValueText(handler,LCText.Money.GUI_MONEY_STORAGE_EMPTY.get()); }
    /**
     * Gets the text of a time-based cycling line from the Money Resource Handlers available money<br>
     * Will return the given emptyText if no money is available to display
     * @see #contentsAsMultiLineText(MoneyResourceHandler, ChatFormatting...)
     * @see #getCyclingValueLine(MoneyResourceHandler)
     * @see #getCyclingValueLine(MoneyResourceHandler,Component)
     */
    public static Component getCyclingValueLine(MoneyResourceHandler handler,String emptyText) { return getCyclingValueLine(handler,Component.literal(emptyText));  }
    /**
     * Gets the text of a time-based cycling line from the Money Resource Handlers available money<br>
     * Will return the given emptyText if no money is available to display
     * @see #contentsAsMultiLineText(MoneyResourceHandler, ChatFormatting...)
     * @see #getCyclingValueLine(MoneyResourceHandler)
     * @see #getCyclingValueLine(MoneyResourceHandler,String)
     */
    public static Component getCyclingValueLine(MoneyResourceHandler handler,Component emptyText) { return ListHelper.cyclingValueFromList(contentsAsMultiLineText(handler),emptyText); }

}
