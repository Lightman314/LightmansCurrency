package io.github.lightman314.lightmanscurrency.api.money.value.builtin;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class CoinValuePair
{

    public final Item coin;
    public final int amount;

    public ItemStack asStack() { return new ItemStack(this.coin, this.amount); }

    public CoinValuePair addAmount(int amount) { return new CoinValuePair(this.coin, this.amount + amount); }
    public CoinValuePair removeAmount(int amount) { return new CoinValuePair(this.coin, this.amount - amount); }

    public CoinValuePair(Item coin, int amount)
    {
        this.coin = coin;
        this.amount = amount;
    }

    public CoinValuePair copy() { return new CoinValuePair(this.coin,this.amount); }

    public CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        tag.putString("Coin", BuiltInRegistries.ITEM.getKey(this.coin).toString());
        tag.putInt("Amount", this.amount);
        return tag;
    }

    public static CoinValuePair load(@Nullable ChainData chainData, @Nonnull CompoundTag tag) { return from(chainData, BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString("Coin"))), tag.getInt("Amount")); }

    public JsonObject toJson()
    {
        JsonObject json = new JsonObject();
        json.addProperty("Coin", BuiltInRegistries.ITEM.getKey(this.coin).toString());
        json.addProperty("Amount", this.amount);
        return json;
    }

    @Nullable
    public static CoinValuePair fromJson(@Nonnull ChainData chainData, @Nonnull JsonObject json) throws JsonSyntaxException, ResourceLocationException
    {
        Item coin = BuiltInRegistries.ITEM.get(ResourceLocation.parse(GsonHelper.getAsString(json, "Coin")));
        int quantity = GsonHelper.getAsInt(json, "Amount", 1);
        if(quantity <= 0)
            throw new JsonSyntaxException("Coin Amount (" + quantity + ") is <= 0!");
        else if(CoinAPI.API.ChainDataOfCoin(coin) == null)
            throw new JsonSyntaxException("Coin Item " + BuiltInRegistries.ITEM.getKey(coin) + " is not a valid coin!");
        return from(chainData, coin, quantity);
    }

    @Nullable
    private static CoinValuePair from(@Nullable ChainData chainData, @Nonnull Item coinItem, int amount)
    {
        if(coinItem == Items.AIR)
            return null;
        //Forcibly load the given coin value if the data is null
        if(chainData == null)
            return new CoinValuePair(coinItem, amount);
        CoinEntry entry = chainData.findEntry(coinItem);
        if(entry != null)
            return new CoinValuePair(entry.getCoin(), amount);
        return null;
    }

}
