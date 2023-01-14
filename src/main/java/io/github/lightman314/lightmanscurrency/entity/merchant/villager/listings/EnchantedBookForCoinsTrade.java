package io.github.lightman314.lightmanscurrency.entity.merchant.villager.listings;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.entity.merchant.villager.ItemListingSerializer;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class EnchantedBookForCoinsTrade implements ItemListing {

    public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "enchanted_book_for_coins");
    public static final Serializer SERIALIZER = new Serializer();

    protected final Item baseCoin;
    protected final int baseCoinCount;
    protected final int maxTrades;
    protected final int xp;
    protected final float priceMult;

    public EnchantedBookForCoinsTrade(int xp) { this(BASE_COIN, BASE_COIN_COUNT, SimpleTrade.MAX_TRADES, xp, SimpleTrade.PRICE_MULT); }
    public EnchantedBookForCoinsTrade(Item baseCoin, int baseCoinCount, int maxTrades, int xp, float priceMult) { this.xp = xp; this.baseCoin = baseCoin; this.baseCoinCount = baseCoinCount; this.maxTrades = maxTrades; this.priceMult = priceMult; }

    private static final Item BASE_COIN = ModItems.COIN_GOLD.get();
    private static final int BASE_COIN_COUNT = 5;

    @Override
    public MerchantOffer getOffer(@NotNull Entity trader, Random rand) {

        List<Enchantment> list = ForgeRegistries.ENCHANTMENTS.getValues().stream().filter(Enchantment::isTradeable).toList();
        Enchantment enchantment = list.get(rand.nextInt(list.size()));

        int level = 1;
        if (enchantment.getMaxLevel() > 0)
            level = rand.nextInt(enchantment.getMaxLevel()) + 1;
        else
            LightmansCurrency.LogError("Enchantment of type '" + ForgeRegistries.ENCHANTMENTS.getKey(enchantment) + "' has a max enchantment level of " + enchantment.getMaxLevel() + ". Unable to properly randomize the enchantment level for a villager trade. Will default to a level 1 enchantment.");
        ItemStack itemstack = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level));

        long coinValue = MoneyUtil.getValue(this.baseCoin);
        long baseValue = coinValue * this.baseCoinCount;

        int valueRandom = rand.nextInt(5 + level * 10);
        long value = baseValue + coinValue * (level + valueRandom);
        if (enchantment.isTreasureOnly())
            value *= 2;

        List<ItemStack> coins = MoneyUtil.getCoinsOfValue(value);
        ItemStack price1 = ItemStack.EMPTY, price2 = ItemStack.EMPTY;
        if (coins.size() > 0)
            price1 = coins.get(0);
        if (coins.size() > 1)
            price2 = coins.get(1);

        LightmansCurrency.LogInfo("EnchantedBookForCoinsTrade.getOffer() -> \n" +
                "baseValue=" + baseValue +
                "\ncoinValue=" + coinValue +
                "\nlevel=" + level +
                "\nvalueRandom=" + valueRandom +
                "\nvalue=" + value +
                "\nprice1=" + price1.getCount() + "x" + ForgeRegistries.ITEMS.getKey(price1.getItem()) +
                "\nprice2=" + price2.getCount() + "x" + ForgeRegistries.ITEMS.getKey(price2.getItem())
        );

        return new MerchantOffer(price1, price2, itemstack, this.maxTrades, this.xp, this.priceMult);

    }

    private static class Serializer implements ItemListingSerializer.IItemListingSerializer, ItemListingSerializer.IItemListingDeserializer {

        @Override
        public ResourceLocation getType() { return TYPE; }

        @Override
        public JsonObject serializeInternal(JsonObject json, ItemListing trade) {
            if(trade instanceof EnchantedBookForCoinsTrade t)
            {
                json.addProperty("Coin", ForgeRegistries.ITEMS.getKey(t.baseCoin).toString());
                json.addProperty("StartCoinCount", t.baseCoinCount);
                json.addProperty("MaxTrades", t.maxTrades);
                json.addProperty("XP", t.xp);
                json.addProperty("PriceMult", t.priceMult);
                return json;
            }
            return null;
        }

        @Override
        public ItemListing deserialize(JsonObject json) throws Exception {
            Item baseCoin = ForgeRegistries.ITEMS.getValue(new ResourceLocation(json.get("Coin").getAsString()));
            int baseCoinCount = json.get("StartCoinCount").getAsInt();
            int maxTrades = json.get("MaxTrades").getAsInt();
            int xp = json.get("XP").getAsInt();
            float priceMult = json.get("PriceMult").getAsFloat();
            return new EnchantedBookForCoinsTrade(baseCoin, baseCoinCount, maxTrades, xp, priceMult);
        }
    }

}