package io.github.lightman314.lightmanscurrency.common.villager_merchant.listings;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.ItemListingSerializer;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RandomTrade implements ItemListing
{

    public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "random_selection");
    public static final Serializer SERIALIZER = new Serializer();

    protected final ItemStack price1;
    protected final ItemStack price2;
    protected final List<ItemStack> sellItemOptions;
    protected final TagKey<Item> sellItemTag;
    protected final int maxTrades;
    protected final int xp;
    protected final float priceMult;

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
        this.price1 = price1;
        this.price2 = price2;
        this.sellItemOptions = sellItemOptions;
        this.sellItemTag = itemTag;
        this.maxTrades = maxTrades;
        this.xp = xpValue;
        this.priceMult = priceMult;
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
    public MerchantOffer getOffer(@NotNull Entity trader, @Nonnull RandomSource rand) {

        ItemStack sellItem = this.getRandomItem(rand);
        if(sellItem == null)
            return null;

        return new MerchantOffer(this.price1, this.price2, sellItem, this.maxTrades, this.xp, this.priceMult);
    }

    private ItemStack getRandomItem(@Nonnull RandomSource rand) {
        if(this.sellItemOptions != null)
        {
            int index = rand.nextInt(this.sellItemOptions.size());
            return this.sellItemOptions.get(index);
        }
        if(this.sellItemTag != null)
        {
            Optional<Item> result = ForgeRegistries.ITEMS.tags().getTag(this.sellItemTag).getRandomElement(rand);
            return result.map(ItemStack::new).orElse(null);
        }
        return null;
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

                if(t.sellItemOptions != null)
                {
                    JsonArray sellItems = new JsonArray();
                    for(ItemStack item : t.sellItemOptions)
                        sellItems.add(FileUtil.convertItemStack(item));
                    json.add("Sell", sellItems);
                }
                else if(t.sellItemTag != null)
                    json.addProperty("SellTag", t.sellItemTag.location().toString());
                json.addProperty("MaxTrades", t.maxTrades);
                json.addProperty("XP", t.xp);
                json.addProperty("PriceMult", t.priceMult);
                return json;
            }
            return null;
        }

        @Override
        public ItemListing deserialize(JsonObject json) throws JsonSyntaxException, ResourceLocationException {
            ItemStack price1 = FileUtil.parseItemStack(GsonHelper.getAsJsonObject(json, "Price"));
            ItemStack price2 = json.has("Price2") ? FileUtil.parseItemStack(GsonHelper.getAsJsonObject(json, "Price2")) : ItemStack.EMPTY;
            List<ItemStack> sellItems = null;
            if(json.has("Sell"))
            {
                sellItems = new ArrayList<>();
                JsonArray sellItemsArray = GsonHelper.getAsJsonArray(json, "Sell");
                for(int i = 0; i < sellItemsArray.size(); ++i)
                    sellItems.add(FileUtil.parseItemStack(sellItemsArray.get(i).getAsJsonObject()));
            }
            TagKey<Item> sellTag = null;
            if(json.has("SellTag"))
                sellTag = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), new ResourceLocation(GsonHelper.getAsString(json, "SellTag")));
            if(sellTag == null && sellItems == null)
                throw new JsonSyntaxException("Missing 'Sell' or 'SellTag' key");
            int maxTrades = GsonHelper.getAsInt(json,"MaxTrades");
            int xp = GsonHelper.getAsInt(json,"XP");
            float priceMult = GsonHelper.getAsFloat(json,"PriceMult");
            return new RandomTrade(price1, price2, sellItems, sellTag, maxTrades, xp, priceMult);
        }
    }

}
