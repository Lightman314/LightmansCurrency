package io.github.lightman314.lightmanscurrency.common.villager_merchant.listings;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.ItemListingSerializer;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class SimpleTrade implements VillagerTrades.ItemListing
{

    public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "simple");
    public static final Serializer SERIALIZER = new Serializer();

    protected final ItemStack price;
    protected final ItemStack price2;
    protected final ItemStack forSale;
    protected final int maxTrades;
    protected final int xp;
    protected final float priceMult;

    protected static final int MAX_TRADES = 12;
    protected static final float PRICE_MULT = 0.05f;

    public SimpleTrade(ItemLike priceItem, int priceCount, ItemLike forsaleItem)
    {
        this(1, priceItem, priceCount, forsaleItem);
    }

    public SimpleTrade(ItemLike priceItem, int priceCount, ItemLike forsaleItem, int forsaleCount)
    {
        this(1, priceItem, priceCount, forsaleItem, forsaleCount);
    }

    public SimpleTrade(int xpValue, ItemLike priceItem, int priceCount, ItemLike forsaleItem)
    {
        this(xpValue, priceItem, priceCount, forsaleItem, 1);
    }

    public SimpleTrade(int xpValue, ItemLike priceItem, int priceCount, ItemLike forsaleItem, int forsaleCount)
    {
        this(new ItemStack(priceItem, priceCount), ItemStack.EMPTY, new ItemStack(forsaleItem, forsaleCount), MAX_TRADES, xpValue, PRICE_MULT);
    }

    public SimpleTrade(ItemLike priceItem1, int priceCount1, ItemLike priceItem2, int priceCount2, ItemLike forsaleItem)
    {
        this(1, priceItem1, priceCount1, priceItem2, priceCount2, forsaleItem);
    }

    public SimpleTrade(ItemLike priceItem1, int priceCount1, ItemLike priceItem2, int priceCount2, ItemLike forsaleItem, int forsaleCount)
    {
        this(1, priceItem1, priceCount1, priceItem2, priceCount2, forsaleItem, forsaleCount);
    }

    public SimpleTrade(int xpValue, ItemLike priceItem1, int priceCount1, ItemLike priceItem2, int priceCount2, ItemLike forsaleItem)
    {
        this(xpValue, priceItem1, priceCount1, priceItem2, priceCount2, forsaleItem, 1);
    }

    public SimpleTrade(int xpValue, ItemLike priceItem1, int priceCount1, ItemLike priceItem2, int priceCount2, ItemLike forsaleItem, int forsaleCount)
    {
        this(new ItemStack(priceItem1, priceCount1), new ItemStack(priceItem2, priceCount2), new ItemStack(forsaleItem, forsaleCount), MAX_TRADES, xpValue, PRICE_MULT);
    }

    public SimpleTrade(int xpValue, ItemLike priceItem1, int priceCount1, ItemStack forSaleItem)
    {
        this(new ItemStack(priceItem1, priceCount1), ItemStack.EMPTY, forSaleItem, MAX_TRADES, xpValue, PRICE_MULT);
    }

    public SimpleTrade(int xpValue, ItemLike priceItem1, int priceCount1, ItemLike priceItem2, int priceCount2, ItemStack forSaleItem)
    {
        this(new ItemStack(priceItem1, priceCount1), new ItemStack(priceItem2, priceCount2), forSaleItem, MAX_TRADES, xpValue, PRICE_MULT);
    }

    public SimpleTrade(ItemStack price, ItemStack forSale, int xp) { this(price, ItemStack.EMPTY, forSale, MAX_TRADES, xp, PRICE_MULT); }

    public SimpleTrade(ItemStack price, ItemStack price2, ItemStack forSale, int maxTrades, int xp, float priceMult) {
        this.price = price;
        this.price2 = price2;
        this.forSale = forSale;
        this.maxTrades = maxTrades;
        this.xp = xp;
        this.priceMult = priceMult;
    }

    public static ItemStack createSuspiciousStew(MobEffect effect, int duration) {
        ItemStack stew = new ItemStack(Items.SUSPICIOUS_STEW, 1);
        SuspiciousStewItem.saveMobEffect(stew, effect, duration);
        return stew;
    }

    @Nullable
    public MerchantOffer getOffer(@NotNull Entity villager, @NotNull RandomSource random) {
        return new MerchantOffer(this.price, this.price2, this.forSale, this.maxTrades, this.xp, this.priceMult);
    }

    private static class Serializer implements ItemListingSerializer.IItemListingSerializer, ItemListingSerializer.IItemListingDeserializer {
        @Override
        public ResourceLocation getType() { return TYPE; }
        @Override
        public JsonObject serializeInternal(JsonObject json, VillagerTrades.ItemListing trade) {
            if(trade instanceof SimpleTrade t)
            {
                json.add("Price", FileUtil.convertItemStack(t.price));
                if(!t.price2.isEmpty())
                    json.add("Price2", FileUtil.convertItemStack(t.price2));
                json.add("Sell", FileUtil.convertItemStack(t.forSale));
                json.addProperty("MaxTrades", t.maxTrades);
                json.addProperty("XP", t.xp);
                json.addProperty("PriceMult", t.priceMult);
                return json;
            }
            return null;
        }
        @Override
        public VillagerTrades.ItemListing deserialize(JsonObject json) throws Exception {
            ItemStack price = FileUtil.parseItemStack(json.get("Price").getAsJsonObject());
            ItemStack price2 = json.has("Price2") ? FileUtil.parseItemStack(json.get("Price2").getAsJsonObject()) : ItemStack.EMPTY;
            ItemStack forSale = FileUtil.parseItemStack(json.get("Sell").getAsJsonObject());
            int maxTrades = json.get("MaxTrades").getAsInt();
            int xp = json.get("XP").getAsInt();
            float priceMult = json.get("PriceMult").getAsFloat();
            return new SimpleTrade(price, price2, forSale, maxTrades, xp, priceMult);
        }
    }

}
