package io.github.lightman314.lightmanscurrency.api.money.value.builtin;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

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
        tag.putString("Coin", ForgeRegistries.ITEMS.getKey(this.coin).toString());
        tag.putInt("Amount", this.amount);
        return tag;
    }

    public static CoinValuePair load(@Nullable ChainData chainData, @Nonnull CompoundTag tag) { return from(chainData, ForgeRegistries.ITEMS.getValue(VersionUtil.parseResource(tag.getString("Coin"))), tag.getInt("Amount")); }

    public void encode(FriendlyByteBuf buffer)
    {
        buffer.writeItem(new ItemStack(this.coin));
        buffer.writeInt(this.amount);
    }

    public static CoinValuePair decode(@Nonnull ChainData chainData, @Nonnull FriendlyByteBuf buffer) { return from(chainData, buffer.readItem().getItem(), buffer.readInt()); }

    public JsonObject toJson()
    {
        JsonObject json = new JsonObject();
        json.addProperty("Coin", ForgeRegistries.ITEMS.getKey(this.coin).toString());
        json.addProperty("Amount", this.amount);
        return json;
    }

    @Nullable
    public static CoinValuePair fromJson(@Nonnull ChainData chainData, @Nonnull JsonObject json) throws JsonSyntaxException, ResourceLocationException
    {
        Item coin = ForgeRegistries.ITEMS.getValue(VersionUtil.parseResource(GsonHelper.getAsString(json, "Coin")));
        int quantity = GsonHelper.getAsInt(json, "Amount", 1);
        if(quantity <= 0)
            throw new JsonSyntaxException("Coin Amount (" + quantity + ") is <= 0!");
        else if(CoinAPI.getApi().ChainDataOfCoin(coin) == null)
            throw new JsonSyntaxException("Coin Item " + ForgeRegistries.ITEMS.getKey(coin) + " is not a valid coin!");
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
