package io.github.lightman314.lightmanscurrency.common.villager_merchant.listings;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.ItemListingSerializer;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;

public class SimpleTrade extends ItemsForXTradeTemplate
{

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "simple");
    public static final Serializer SERIALIZER = new Serializer();

    protected final ItemStack forSale;

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
        super(price,price2,maxTrades,xp,priceMult);
        this.forSale = forSale;
    }
    private SimpleTrade(@Nonnull DeserializedData data, @Nonnull ItemStack forSale)
    {
        super(data);
        this.forSale = forSale;
    }

    public static ItemStack createSuspiciousStew(Holder<MobEffect> effect, int duration) {
        ItemStack stew = new ItemStack(Items.SUSPICIOUS_STEW, 1);
        stew.set(DataComponents.SUSPICIOUS_STEW_EFFECTS,new SuspiciousStewEffects(ImmutableList.of(new SuspiciousStewEffects.Entry(effect,duration))));
        return stew;
    }

    @Override
    protected ItemStack createResult(@Nonnull Entity trader, @Nonnull RandomSource rand) { return this.forSale; }

    public static class Serializer implements ItemListingSerializer.IItemListingSerializer, ItemListingSerializer.IItemListingDeserializer {

        private Serializer() {}
        @Override
        public ResourceLocation getType() { return TYPE; }
        @Override
        public JsonObject serializeInternal(JsonObject json, VillagerTrades.ItemListing trade) {
            if(trade instanceof SimpleTrade t)
            {
                t.serializeData(json);
                json.add("Sell", FileUtil.convertItemStack(t.forSale));

                return json;
            }
            return null;
        }
        @Override
        public VillagerTrades.ItemListing deserialize(JsonObject json) throws JsonSyntaxException, ResourceLocationException {

            DeserializedData data = deserializeData(json);
            ItemStack forSale = FileUtil.parseItemStack(GsonHelper.getAsJsonObject(json,"Sell"));

            return new SimpleTrade(data, forSale);
        }
    }

}
