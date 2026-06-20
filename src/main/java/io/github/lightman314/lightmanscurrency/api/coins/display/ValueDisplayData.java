package io.github.lightman314.lightmanscurrency.api.coins.display;

import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.coins.value.CoinValue;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public abstract class ValueDisplayData {

    public static final String ICON_FALLBACK_KEY = "lightmanscurrency.icon.fallback";

    private ChainData parent = null;

    public final String getChain() { return this.parent == null ? "" : this.parent.chain; }
    public final void setParent(ChainData parent)
    {
        if(this.parent != null)
            return;
        this.parent = parent;
    }
    protected final ChainData getParent() { return this.parent; }

    /**
     * Returns the serializer for this value display type
     */
    public abstract ValueDisplaySerializer getSerializer();

    /**
     * Return a formatted text component of the given value
     * Used for most text displays such as price tooltips, wallet items, etc.
     */
    public abstract Component formatValue(CoinValue value, Component empty);

    /**
     * Add text to a coins items tooltip to make it known to the user how much is it worth.
     */
    public abstract void formatCoinTooltip(ItemStack stack, List<Component> tooltip);

    public MoneyValue parseDisplayInput(double displayInput) { return CoinValue.fromNumber(this.getChain(), (long)displayInput); }

    public static Component getIcon(Item item) { return Component.translatableWithFallback(item.getDescriptionId() + ".icon",ICON_FALLBACK_KEY).withStyle(ChatFormatting.WHITE); }
    public static Component getIcon(String chain) { return Component.translatableWithFallback("lightmanscurrency.money.chain." + chain + ".icon",ICON_FALLBACK_KEY).withStyle(ChatFormatting.WHITE); }

}