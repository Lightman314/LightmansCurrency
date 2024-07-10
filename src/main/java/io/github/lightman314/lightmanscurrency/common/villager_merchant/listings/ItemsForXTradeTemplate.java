package io.github.lightman314.lightmanscurrency.common.villager_merchant.listings;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.ListingUtil;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ItemsForXTradeTemplate implements VillagerTrades.ItemListing {

    public static final int MAX_TRADES = 12;
    public static final float PRICE_MULT = 0.05f;

    private final ItemStack price;
    private final ItemStack price2;
    private final int maxTrades;
    private final int xp;
    private final float priceMult;

    protected ItemsForXTradeTemplate(@Nonnull ItemStack price, @Nonnull ItemStack price2, int maxTrades, int xp, float priceMult) {
        this.price = price;
        this.price2 = price2;
        this.maxTrades = maxTrades;
        this.xp = xp;
        this.priceMult = priceMult;
    }
    protected ItemsForXTradeTemplate(@Nonnull DeserializedData data) { this(data.price1,data.price2,data.maxTrades,data.xp,data.priceMult); }

    protected abstract ItemStack createResult(@Nonnull Entity trader, @Nonnull RandomSource rand);

    @Nullable
    @Override
    public final MerchantOffer getOffer(@Nonnull Entity trader, @Nonnull RandomSource rand) {
        ItemStack result = this.createResult(trader,rand);
        if(result == null)
            return null;
        return new MerchantOffer(ListingUtil.costFor(this.price),ListingUtil.optionalCost(this.price2),result,this.maxTrades,this.xp,this.priceMult);
    }

    protected final void serializeData(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup)
    {
        json.add("Price", FileUtil.convertItemStack(this.price,lookup));
        if(!this.price2.isEmpty())
            json.add("Price2", FileUtil.convertItemStack(this.price2,lookup));
        json.addProperty("MaxTrades", this.maxTrades);
        json.addProperty("XP", this.xp);
        json.addProperty("PriceMult", this.priceMult);
    }

    @Nonnull
    protected static DeserializedData deserializeData(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException
    {
        ItemStack price = FileUtil.parseItemStack(GsonHelper.getAsJsonObject(json,"Price"),lookup);
        ItemStack price2 = json.has("Price2") ? FileUtil.parseItemStack(GsonHelper.getAsJsonObject(json,"Price2"),lookup) : ItemStack.EMPTY;
        int maxTrades = GsonHelper.getAsInt(json,"MaxTrades",MAX_TRADES);
        int xp = GsonHelper.getAsInt(json,"XP",1);
        float priceMult = GsonHelper.getAsFloat(json, "PriceMult",PRICE_MULT);
        return new DeserializedData(price,price2,maxTrades,xp,priceMult);
    }

    protected record DeserializedData(@Nonnull ItemStack price1, @Nonnull ItemStack price2, int maxTrades, int xp, float priceMult)  { }

}
