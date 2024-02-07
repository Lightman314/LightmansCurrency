package io.github.lightman314.lightmanscurrency.common.villager_merchant.listings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.ItemListingSerializer;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RandomTrade implements ItemListing
{

    public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "random_selection");
    public static final Serializer SERIALIZER = new Serializer();

    protected final ItemStack price1;
    protected final ItemStack price2;
    protected final List<ItemStack> sellItemOptions;
    protected final int maxTrades;
    protected final int xp;
    protected final float priceMult;

    public static RandomTrade build(ItemStack price, List<? extends ItemLike> sellItemOptions, int maxTrades, int xpValue, float priceMult) {
        return build(price, ItemStack.EMPTY, sellItemOptions, maxTrades, xpValue, priceMult);
    }

    public static RandomTrade build(ItemStack price1, ItemStack price2, List<? extends ItemLike> sellItemOptions, int maxTrades, int xpValue, float priceMult) {
        return new RandomTrade(price1, price2, convertItemList(sellItemOptions), maxTrades, xpValue, priceMult);
    }


    public RandomTrade(ItemStack price, ItemLike[] sellItemOptions, int maxTrades, int xpValue, float priceMult) {
        this(price, ItemStack.EMPTY, sellItemOptions, maxTrades, xpValue, priceMult);
    }

    public RandomTrade(ItemStack price1, ItemStack price2, ItemLike[] sellItemOptions, int maxTrades, int xpValue, float priceMult) {
        this(price1, price2, convertItemArray(sellItemOptions), maxTrades, xpValue, priceMult);
    }

    public RandomTrade(ItemStack price1, ItemStack price2, List<ItemStack> sellItemOptions, int maxTrades, int xpValue, float priceMult)
    {
        this.price1 = price1;
        this.price2 = price2;
        this.sellItemOptions = sellItemOptions;
        this.maxTrades = maxTrades;
        this.xp = xpValue;
        this.priceMult = priceMult;
    }

    private static List<ItemStack> convertItemArray(ItemLike[] array) {
        List<ItemStack> options = new ArrayList<>();
        for(ItemLike item : array)
        {
            ItemStack stack = new ItemStack(item);
            if(!stack.isEmpty())
                options.add(stack);
        }
        return options;
    }

    private static List<ItemStack> convertItemList(Iterable<? extends ItemLike> array) {
        List<ItemStack> options = new ArrayList<>();
        for(ItemLike item : array)
        {
            ItemStack stack = new ItemStack(item);
            if(!stack.isEmpty())
                options.add(stack);
        }
        return options;
    }

    @Override
    public MerchantOffer getOffer(@NotNull Entity trader, RandomSource rand) {

        int index = rand.nextInt(this.sellItemOptions.size());
        ItemStack sellItem = sellItemOptions.get(index).copy();

        return new MerchantOffer(this.price1, this.price2, sellItem, this.maxTrades, this.xp, this.priceMult);
    }

    private static class Serializer implements ItemListingSerializer.IItemListingSerializer, ItemListingSerializer.IItemListingDeserializer {

        @Override
        public ResourceLocation getType() { return TYPE; }

        @Override
        public JsonObject serializeInternal(JsonObject json, ItemListing trade) {
            if(trade instanceof RandomTrade t)
            {
                json.add("Price", FileUtil.convertItemStack(t.price1));
                if(!t.price2.isEmpty())
                    json.add("Price2", FileUtil.convertItemStack(t.price2));
                JsonArray sellItems = new JsonArray();
                for(ItemStack item : t.sellItemOptions)
                    sellItems.add(FileUtil.convertItemStack(item));
                json.add("Sell", sellItems);
                json.addProperty("MaxTrades", t.maxTrades);
                json.addProperty("XP", t.xp);
                json.addProperty("PriceMult", t.priceMult);
                return json;
            }
            return null;
        }

        @Override
        public ItemListing deserialize(JsonObject json) throws Exception {
            ItemStack price1 = FileUtil.parseItemStack(json.get("Price").getAsJsonObject());
            ItemStack price2 = json.has("Price2") ? FileUtil.parseItemStack(json.get("Price2").getAsJsonObject()) : ItemStack.EMPTY;
            List<ItemStack> sellItems = new ArrayList<>();
            JsonArray sellItemsArray = json.getAsJsonArray("Sell");
            for(int i = 0; i < sellItemsArray.size(); ++i)
                sellItems.add(FileUtil.parseItemStack(sellItemsArray.get(i).getAsJsonObject()));
            int maxTrades = json.get("MaxTrades").getAsInt();
            int xp = json.get("XP").getAsInt();
            float priceMult = json.get("PriceMult").getAsFloat();
            return new RandomTrade(price1, price2, sellItems, maxTrades, xp, priceMult);
        }
    }

}
