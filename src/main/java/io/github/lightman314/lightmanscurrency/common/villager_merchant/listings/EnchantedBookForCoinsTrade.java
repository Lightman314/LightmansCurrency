package io.github.lightman314.lightmanscurrency.common.villager_merchant.listings;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.builtin.CoinValue;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.ItemListingSerializer;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.ListingUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public class EnchantedBookForCoinsTrade implements ItemListing {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "enchanted_book_for_coins");
    public static final Serializer SERIALIZER = new Serializer();

    protected final Item baseCoin;
    protected final int baseCoinCount;
    protected final TagKey<Enchantment> allowedEnchantments;
    protected final int maxTrades;
    protected final int xp;
    protected final float priceMult;

    public EnchantedBookForCoinsTrade(int xp) { this(xp, EnchantmentTags.TRADEABLE); }
    public EnchantedBookForCoinsTrade(int xp, @Nonnull TagKey<Enchantment> tag) { this(BASE_COIN, BASE_COIN_COUNT, tag,SimpleTrade.MAX_TRADES, xp, SimpleTrade.PRICE_MULT); }
    public EnchantedBookForCoinsTrade(Item baseCoin, int baseCoinCount, int maxTrades, int xp, float priceMult) { this(baseCoin,baseCoinCount, EnchantmentTags.TRADEABLE, maxTrades,xp,priceMult); }
    public EnchantedBookForCoinsTrade(Item baseCoin, int baseCoinCount, @Nonnull TagKey<Enchantment> tag, int maxTrades, int xp, float priceMult) { this.xp = xp; this.baseCoin = baseCoin; this.baseCoinCount = baseCoinCount; this.allowedEnchantments = tag; this.maxTrades = maxTrades; this.priceMult = priceMult; }

    private static final Item BASE_COIN = ModItems.COIN_GOLD.get();
    private static final int BASE_COIN_COUNT = 5;

    @Override
    public MerchantOffer getOffer(@Nonnull Entity trader, @Nonnull RandomSource rand) {

        Optional<Holder<Enchantment>> optional = trader.level()
                .registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getRandomElementOf(EnchantmentTags.TRADEABLE, rand);

        if(optional.isPresent())
        {
            Holder<Enchantment> holder = optional.get();
            Enchantment enchantment = holder.value();

            int level = 1;
            if (enchantment.getMaxLevel() > 0)
                level = rand.nextInt(enchantment.getMaxLevel()) + 1;
            else
                LightmansCurrency.LogError("Enchantment of type '" + holder.getRegisteredName() + "' has a max enchantment level of " + enchantment.getMaxLevel() + ". Unable to properly randomize the enchantment level for a villager trade. Will default to a level 1 enchantment.");
            ItemStack itemstack = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, level));

            ChainData chain = CoinAPI.API.ChainDataOfCoin(this.baseCoin);
            if(chain == null)
            {
                LightmansCurrency.LogWarning("Book for coin trade failed as '" + new ItemStack(this.baseCoin).getHoverName().getString() + "' is not a registered coin!");
                return null;
            }
            long baseValue = chain.getCoreValue(this.baseCoin) * this.baseCoinCount;

            int valueRandom = rand.nextInt(5 + level * 10);
            long value = baseValue + baseValue * (level + valueRandom);
            if (holder.is(EnchantmentTags.DOUBLE_TRADE_PRICE))
                value *= 2;

            MoneyValue v = CoinValue.fromNumber(chain.chain, value);
            if(!(v instanceof CoinValue coinValue))
                return null;
            List<ItemStack> coins = coinValue.getAsSeperatedItemList();
            ItemCost price1 = null;
            Optional<ItemCost> price2 = Optional.empty();
            if (!coins.isEmpty())
                price1 = ListingUtil.costFor(coins.get(0));
            if (coins.size() > 1)
                price2 = Optional.of(ListingUtil.costFor(coins.get(1)));

            if(price1 != null)
                return new MerchantOffer(price1,price2,itemstack,this.maxTrades,this.xp,this.priceMult);
        }

        return null;

    }

    public static class Serializer implements ItemListingSerializer.IItemListingSerializer, ItemListingSerializer.IItemListingDeserializer {

        private Serializer() {}

        @Override
        public ResourceLocation getType() { return TYPE; }

        @Nonnull
        @Override
        public JsonObject serializeInternal(@Nonnull JsonObject json, @Nonnull ItemListing trade, @Nonnull HolderLookup.Provider lookup) {
            if(trade instanceof EnchantedBookForCoinsTrade t)
            {
                json.addProperty("Coin", BuiltInRegistries.ITEM.getKey(t.baseCoin).toString());
                json.addProperty("StartCoinCount", t.baseCoinCount);
                json.addProperty("EnchantmentTag", t.allowedEnchantments.location().toString());
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
            Item baseCoin = BuiltInRegistries.ITEM.get(ResourceLocation.parse(GsonHelper.getAsString(json,"Coin")));
            int baseCoinCount = GsonHelper.getAsInt(json,"StartCoinCount");
            TagKey<Enchantment> enchantmentTag = TagKey.create(Registries.ENCHANTMENT,ResourceLocation.parse(GsonHelper.getAsString(json,"EnchantmentTag")));
            int maxTrades = GsonHelper.getAsInt(json,"MaxTrades");
            int xp = GsonHelper.getAsInt(json,"XP");
            float priceMult = GsonHelper.getAsFloat(json,"PriceMult");
            return new EnchantedBookForCoinsTrade(baseCoin, baseCoinCount, enchantmentTag, maxTrades, xp, priceMult);
        }
    }

}
