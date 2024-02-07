package io.github.lightman314.lightmanscurrency.api.money.coins.data.coin;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.display.ValueDisplayData;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
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
    private long coreValue = 0;

    public long getCoreValue() { return this.coreValue; }
    public void setCoreValue(long value)
    {
        if(this.coreValue > 0)
        {
            LightmansCurrency.LogError("Should not be overriding a coin entries defined core value once it's already been defined!");
            return;
        }
        this.coreValue = value;
    }

    public int getExchangeRate() { return 0; }
    public final Component getName() { return new ItemStack(this.coin).getHoverName(); }
    public final Item getCoin() { return this.coin; }
    public CoinEntry(@Nonnull Item coin) { this(coin, false); }
    protected CoinEntry(@Nonnull Item coin, boolean sideChain) { this.coin = coin; this.sideChain = sideChain; }

    public boolean matches(@Nonnull CoinEntry coin) { return this == coin || this.coin == coin.coin; }
    public boolean matches(@Nonnull Item item) { return this.coin == item; }
    public boolean matches(@Nonnull ItemStack stack) { return this.matches(stack.getItem()); }

    public boolean matches(@Nonnull CompoundTag tag)
    {
        if(tag.contains("coin"))
            return this.matches(ForgeRegistries.ITEMS.getValue(new ResourceLocation(tag.getString("coin"))));
        return false;
    }

    public final JsonObject serialize(@Nonnull ValueDisplayData displayData)
    {
        JsonObject json = new JsonObject();
        json.addProperty("Coin", ForgeRegistries.ITEMS.getKey(coin).toString());
        this.writeAdditional(json);
        displayData.getSerializer().writeAdditionalToCoin(displayData, this, json);
        return json;
    }

    protected void writeAdditional(@Nonnull JsonObject json) {}

    protected static Item parseBase(@Nonnull JsonObject json) throws JsonSyntaxException, ResourceLocationException
    {
        ResourceLocation itemID = new ResourceLocation(GsonHelper.getAsString(json, "Coin"));
        Item item = ForgeRegistries.ITEMS.getValue(itemID);
        if(item == null || item == Items.AIR)
            throw new JsonSyntaxException(itemID + " is not a valid item!");
        return item;
    }

    public static CoinEntry parse(JsonObject json) throws JsonSyntaxException, ResourceLocationException { return new CoinEntry(parseBase(json)); }

}
