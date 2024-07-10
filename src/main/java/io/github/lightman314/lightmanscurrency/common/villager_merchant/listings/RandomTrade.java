package io.github.lightman314.lightmanscurrency.common.villager_merchant.listings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.ItemListingSerializer;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RandomTrade extends ItemsForXTradeTemplate
{

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "random_selection");
    public static final Serializer SERIALIZER = new Serializer();

    protected final List<ItemStack> sellItemOptions;
    protected final TagKey<Item> sellItemTag;

    public static RandomTrade build(ItemStack price, List<? extends ItemLike> sellItemOptions, int maxTrades, int xpValue, float priceMult) {
        return build(price, ItemStack.EMPTY, sellItemOptions, maxTrades, xpValue, priceMult);
    }

    public static RandomTrade build(ItemStack price1, ItemStack price2, List<? extends ItemLike> sellItemOptions, int maxTrades, int xpValue, float priceMult) {
        return new RandomTrade(price1, price2, convertItemList(sellItemOptions), null, maxTrades, xpValue, priceMult);
    }

    public static RandomTrade build(ItemStack price1, TagKey<Item> itemTag, int maxTrades, int xpValue, float priceMult) {
        return build(price1, ItemStack.EMPTY, itemTag, maxTrades, xpValue, priceMult);
    }

    public static RandomTrade build(ItemStack price1, ItemStack price2, TagKey<Item> itemTag, int maxTrades, int xpValue, float priceMult) {
        return new RandomTrade(price1, price2, null, itemTag, maxTrades, xpValue, priceMult);
    }

    public RandomTrade(ItemStack price1, ItemStack price2, List<ItemStack> sellItemOptions, TagKey<Item> itemTag, int maxTrades, int xpValue, float priceMult)
    {
        super(price1,price2,maxTrades,xpValue,priceMult);
        this.sellItemOptions = sellItemOptions;
        this.sellItemTag = itemTag;
    }
    private RandomTrade(DeserializedData data, @Nullable List<ItemStack> sellItemOptions, @Nullable TagKey<Item> itemTag)
    {
        super(data);
        this.sellItemOptions = sellItemOptions;
        this.sellItemTag = itemTag;
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
    protected ItemStack createResult(@Nonnull Entity trader, @Nonnull RandomSource rand) { return this.getRandomItem(rand); }

    private ItemStack getRandomItem(@Nonnull RandomSource rand) {
        if(this.sellItemOptions != null)
        {
            int index = rand.nextInt(this.sellItemOptions.size());
            return this.sellItemOptions.get(index);
        }
        if(this.sellItemTag != null)
        {
            Optional<Holder<Item>> result = BuiltInRegistries.ITEM.getRandomElementOf(this.sellItemTag,rand);
            return result.map(ItemStack::new).orElse(null);
        }
        return null;
    }

    public static class Serializer implements ItemListingSerializer.IItemListingSerializer, ItemListingSerializer.IItemListingDeserializer {

        private Serializer() {}

        @Override
        public ResourceLocation getType() { return TYPE; }

        @Override
        public JsonObject serializeInternal(@Nonnull JsonObject json, @Nonnull ItemListing trade, @Nonnull HolderLookup.Provider lookup) {
            if(trade instanceof RandomTrade t)
            {
                t.serializeData(json,lookup);
                if(t.sellItemOptions != null)
                {
                    JsonArray sellItems = new JsonArray();
                    for(ItemStack item : t.sellItemOptions)
                        sellItems.add(FileUtil.convertItemStack(item,lookup));
                    json.add("Sell", sellItems);
                }
                else if(t.sellItemTag != null)
                    json.addProperty("SellTag", t.sellItemTag.location().toString());
                return json;
            }
            return null;
        }

        @Nonnull
        @Override
        public ItemListing deserialize(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException {
            var data = deserializeData(json,lookup);
            List<ItemStack> sellItems = null;
            if(json.has("Sell"))
            {
                sellItems = new ArrayList<>();
                JsonArray sellItemsArray = GsonHelper.getAsJsonArray(json, "Sell");
                for(int i = 0; i < sellItemsArray.size(); ++i)
                    sellItems.add(FileUtil.parseItemStack(sellItemsArray.get(i).getAsJsonObject(),lookup));
            }
            TagKey<Item> sellTag = null;
            if(json.has("SellTag"))
                sellTag = TagKey.create(BuiltInRegistries.ITEM.key(), ResourceLocation.parse(GsonHelper.getAsString(json, "SellTag")));
            if(sellTag == null && sellItems == null)
                throw new JsonSyntaxException("Missing 'Sell' or 'SellTag' key");
            return new RandomTrade(data, sellItems, sellTag);
        }
    }

}
