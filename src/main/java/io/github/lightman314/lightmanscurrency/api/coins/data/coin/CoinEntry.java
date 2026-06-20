package io.github.lightman314.lightmanscurrency.api.coins.data.coin;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.coins.display.ValueDisplayData;
import net.minecraft.IdentifierException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;

/**
 * Data pertaining to this particular coin.
 * Should only be accessed in real-time from the {@link ChainData} managing it.
 * This should not be stored locally as it's values may become obsolete if the coin config is reloaded.
 */
public class CoinEntry {

    private final Item coin;
    private final boolean sideChain;
    public final boolean isSideChain() { return this.sideChain; }
    private long internalValue = 0;

    public long getInternalValue() { return this.internalValue; }
    public void setInternalValue(long value)
    {
        if(this.internalValue > 0)
        {
            LightmansCurrency.LogError("Should not be overriding a coin entries defined core value once it's already been defined!");
            return;
        }
        this.internalValue = value;
    }

    private boolean exchangeRatesSet = false;
    private Pair<CoinEntry,Integer> lowerExchange = null;
    @Nullable
    public Pair<CoinEntry,Integer> getLowerExchange() { return this.lowerExchange; }
    private Pair<CoinEntry,Integer> upperExchange = null;
    @Nullable
    public Pair<CoinEntry,Integer> getUpperExchange() { return this.upperExchange; }

    public void defineExchanges(@Nullable Pair<CoinEntry,Integer> lowerExchange, @Nullable Pair<CoinEntry,Integer> upperExchange)
    {
        if(this.exchangeRatesSet)
        {
            LightmansCurrency.LogWarning("Attempted to define a coin entries exchange rates after they've already been defined.");
            return;
        }
        this.lowerExchange = lowerExchange;
        this.upperExchange = upperExchange;
        this.exchangeRatesSet = true;
    }

    public int getExchangeRate() { return 0; }
    public final Component getName() { return new ItemStack(this.coin).getHoverName(); }
    public final Item getCoin() { return this.coin; }
    public CoinEntry(Item coin) { this(coin, false); }
    protected CoinEntry(Item coin, boolean sideChain) { this.coin = coin; this.sideChain = sideChain; }

    public boolean matches(CoinEntry coin) { return this == coin || this.coin == coin.coin; }
    public boolean matches(Item item) { return this.coin == item; }
    public boolean matches(ItemResource resource) { return this.matches(resource.getItem()); }
    public boolean matches(ItemInstance stack) { return this.matches(stack.typeHolder().value()); }

    public final JsonObject serialize(ValueDisplayData displayData)
    {
        JsonObject json = new JsonObject();
        json.addProperty("Coin", BuiltInRegistries.ITEM.getKey(this.coin).toString());
        this.writeAdditional(json);
        displayData.getSerializer().writeAdditionalToCoin(displayData, this, json);
        return json;
    }

    protected void writeAdditional(JsonObject json) {}

    protected static Item parseBase(JsonObject json) throws JsonSyntaxException, IdentifierException
    {
        Identifier itemID = Identifier.parse(GsonHelper.getAsString(json, "Coin"));
        Item item = BuiltInRegistries.ITEM.getValue(itemID);
        if(item == Items.AIR)
            throw new JsonSyntaxException(itemID + " is not a valid item!");
        return item;
    }

    public static CoinEntry parse(JsonObject json) throws JsonSyntaxException, IdentifierException { return new CoinEntry(parseBase(json)); }

}
