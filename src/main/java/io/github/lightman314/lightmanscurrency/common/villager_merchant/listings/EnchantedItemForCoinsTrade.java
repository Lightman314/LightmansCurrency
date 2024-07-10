package io.github.lightman314.lightmanscurrency.common.villager_merchant.listings;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.ItemListingSerializer;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.ListingUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import java.util.List;

public class EnchantedItemForCoinsTrade implements ItemListing
{

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "enchanted_item_for_coins");
    public static final Serializer SERIALIZER = new Serializer();

    protected final Item baseCoin;
    protected final int baseCoinCount;
    protected final Item sellItem;
    protected final int maxTrades;
    protected final int xp;
    protected final float priceMult;
    protected final double basePriceModifier;

    public EnchantedItemForCoinsTrade(ItemLike baseCoin, int baseCoinCount, ItemLike sellItem, int maxUses, int xpValue, float priceMultiplier, double basePriceModifier)
    {
        this.baseCoin = baseCoin.asItem();
        this.baseCoinCount = baseCoinCount;
        this.basePriceModifier = basePriceModifier;
        this.sellItem = sellItem.asItem();
        this.maxTrades = maxUses;
        this.xp = xpValue;
        this.priceMult = priceMultiplier;

    }

    @Override
    public MerchantOffer getOffer(@Nonnull Entity trader, @Nonnull RandomSource rand) {
        int i = 5 + rand.nextInt(15);
        ItemStack itemstack = EnchantmentHelper.enchantItem(rand, new ItemStack(sellItem), i, trader.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(EnchantmentTags.ON_TRADED_EQUIPMENT).stream());

        ChainData chain = CoinAPI.API.ChainDataOfCoin(this.baseCoin);
        if(chain == null)
        {
            LightmansCurrency.LogWarning("Item for coin trade failed as '" + new ItemStack(this.baseCoin).getHoverName().getString() + "' is not a registered coin!");
            return null;
        }

        long coinValue = chain.getCoreValue(this.baseCoin);
        long baseValue = coinValue * this.baseCoinCount;
        long priceValue = baseValue + (long)(coinValue * i * this.basePriceModifier);

        ItemStack price1 = ItemStack.EMPTY, price2 = ItemStack.EMPTY;
        MoneyValue v = CoinValue.fromNumber(chain.chain, priceValue);
        if(!(v instanceof CoinValue cv))
            return null;
        List<ItemStack> priceStacks = cv.getAsSeperatedItemList();
        if(!priceStacks.isEmpty())
            price1 = priceStacks.get(0);
        if(priceStacks.size() > 1)
            price2 = priceStacks.get(1);

        return new MerchantOffer(ListingUtil.costFor(price1), ListingUtil.optionalCost(price2), itemstack, this.maxTrades, this.xp, this.priceMult);
    }

    public static class Serializer implements ItemListingSerializer.IItemListingSerializer, ItemListingSerializer.IItemListingDeserializer {

        private Serializer() {}
        @Override
        public ResourceLocation getType() { return TYPE; }
        @Override
        public JsonObject serializeInternal(@Nonnull JsonObject json, @Nonnull ItemListing trade, @Nonnull HolderLookup.Provider lookup) {
            if(trade instanceof EnchantedItemForCoinsTrade t)
            {
                json.addProperty("Coin", BuiltInRegistries.ITEM.getKey(t.baseCoin).toString());
                json.addProperty("BaseCoinCount", t.baseCoinCount);
                json.addProperty("EnchantmentValueModifier", t.basePriceModifier);
                json.addProperty("Sell", BuiltInRegistries.ITEM.getKey(t.sellItem).toString());
                json.addProperty("MaxTrades", t.maxTrades);
                json.addProperty("XP", t.xp);
                json.addProperty("PriceMult", t.priceMult);
                return json;
            }
            return null;
        }

        @Nonnull
        @Override
        public ItemListing deserialize(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException {
            Item coin = BuiltInRegistries.ITEM.get(ResourceLocation.parse(GsonHelper.getAsString(json,"Coin")));
            int baseCoinCount = GsonHelper.getAsInt(json,"BaseCoinCount");
            double basePriceModifier = GsonHelper.getAsDouble(json,"EnchantmentValueModifier");
            Item sellItem = BuiltInRegistries.ITEM.get(ResourceLocation.parse(GsonHelper.getAsString(json,"Sell")));
            int maxTrades = GsonHelper.getAsInt(json,"MaxTrades");
            int xp = GsonHelper.getAsInt(json,"XP");
            float priceMult = GsonHelper.getAsFloat(json, "PriceMult");
            return new EnchantedItemForCoinsTrade(coin, baseCoinCount, sellItem, maxTrades, xp, priceMult, basePriceModifier);
        }
    }


}
