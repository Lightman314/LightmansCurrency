package io.github.lightman314.lightmanscurrency.api.money.client;

import io.github.lightman314.lightmanscurrency.api.money.input.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;

import javax.annotation.Nullable;
import java.util.List;

public abstract class ClientCurrencyType {

    public final CurrencyType type;
    public final ResourceLocation getType(){ return this.type.getType(); }
    public ClientCurrencyType(CurrencyType type) { this.type = type; }

    /**
     * Only in {@link Dist#CLIENT}<br>
     * Return a list of each {@link MoneyInputHandler} required for this mod!<br>
     * {@link MoneyInputHandler}s are used by {@link io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget MoneyValueWidget} to allow defining prices for your mods' currency<br>
     * See {@link io.github.lightman314.lightmanscurrency.api.money.input.templates.SimpleDisplayInput SimpleDisplayInput} for a simple text template, or {@link io.github.lightman314.lightmanscurrency.api.money.input.builtin.CoinValueInput CoinValueInput} to see how my mod handles this for coins<br>
     * Returns a list as it's possible for a single currency type to contain several variants (such as different coin chains, etc.)
     */
    public abstract List<MoneyInputHandler> getInputHandlers(@Nullable Player player);

    /**
     * Obtains a {@link DisplayEntry} for trade displays for the given Money Value
     * @param value The MoneyValue handled by this CurrencyType
     * @param additionalTooltips
     * @param overrideTooltips
     * @return
     * @throws IllegalStateException if the MoneyValue given is not handled by this CurrencyType
     */
    public abstract DisplayEntry getDisplayEntry(MoneyValue value, @Nullable List<Component> additionalTooltips, boolean overrideTooltips);

    protected final DisplayEntry throwIllegalDisplayException(MoneyValue value, Class<? extends MoneyValue> expectedType)
    {
        throw new IllegalStateException("Non " + expectedType.getSimpleName() + " value passed to ClientCurrencyType#getDisplayEntry of type " + this.getType() + " that does not support that value class (" + value.getClass().getName() + ")!");
    }

}
