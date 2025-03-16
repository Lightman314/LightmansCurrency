package io.github.lightman314.lightmanscurrency.api.money.coins.display;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class ValueDisplayData {

    public static final String ICON_FALLBACK_KEY = "lightmanscurrency.icon.fallback";

    private ChainData parent = null;
    @Nonnull
    public final String getChain() { return this.parent == null ? "" : this.parent.chain; }
    public final void setParent(@Nonnull ChainData parent)
    {
        if(this.parent != null)
            return;
        this.parent = parent;
    }
    protected final ChainData getParent() { return this.parent; }

    /**
     * Returns the type of this value display
     */
    @Nonnull
    public final ResourceLocation getType() { return this.getSerializer().getType(); }

    /**
     * Returns the serializer for this value display type
     */
    @Nonnull
    public abstract ValueDisplaySerializer getSerializer();

    /**
     * Return a formatted text component of the given value
     * Used for most text displays such as price tooltips, wallet contents, etc.
     */
    @Nonnull
    public abstract MutableComponent formatValue(@Nonnull CoinValue value, @Nonnull MutableComponent empty);

    /**
     * Add text to a coins items tooltip to make it known to the user how much is it worth.
     */
    public abstract void formatCoinTooltip(@Nonnull ItemStack stack, @Nonnull List<Component> tooltip);

    @Nonnull
    public MoneyValue parseDisplayInput(double displayInput) { return CoinValue.fromNumber(this.getChain(), (long)displayInput); }

    @Nonnull
    public static Component getIcon(Item item) { return EasyText.translatableWithFallback(item.getDescriptionId() + ".icon",ICON_FALLBACK_KEY).withStyle(ChatFormatting.WHITE); }
    @Nonnull
    public static Component getIcon(String chain) { return EasyText.translatableWithFallback("lightmanscurrency.money.chain." + chain + ".icon",ICON_FALLBACK_KEY).withStyle(ChatFormatting.WHITE); }

}