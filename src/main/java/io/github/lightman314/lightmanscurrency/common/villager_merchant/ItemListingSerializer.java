package io.github.lightman314.lightmanscurrency.common.villager_merchant;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.*;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemListingSerializer {


    private static final Map<Class<? extends ItemListing>, IItemListingSerializer> serializers = new HashMap<>();
    private static final Map<ResourceLocation, IItemListingDeserializer> deserializers = new HashMap<>();

    public static <L extends ItemListing,T extends IItemListingSerializer & IItemListingDeserializer> void registerItemListing(@Nonnull ResourceLocation type, @Nonnull Class<L> clazz, @Nonnull T serializer) { registerItemListing(type, clazz, serializer, serializer); }

    public static <T extends ItemListing> void registerItemListing(@Nonnull ResourceLocation type, Class<T> clazz, @Nonnull IItemListingSerializer serializer, @Nonnull IItemListingDeserializer deserializer) {
        if(serializers.containsKey(clazz))
            LightmansCurrency.LogWarning("Attempted to register a duplicate ItemListing Serializer of class '" + clazz.getName() + "'!");
        else if(deserializers.containsKey(type))
            LightmansCurrency.LogWarning("Attempted to register a duplicate ItemListing Deserializer of type '" + type + "'!");
        else
        {
            serializers.put(clazz, serializer);
            deserializers.put(type, deserializer);
            LightmansCurrency.LogInfo("Registered Item Listing serializer '" + type + "'");
        }
    }


    public static JsonObject serialize(Map<Integer, List<ItemListing>> trades, int count, @Nonnull HolderLookup.Provider lookup) {
        JsonObject json = new JsonObject();
        for(int i = 1; i <= count; ++i)
            json.add("TradesLevel" + i, serializeList(trades.getOrDefault(i,new ArrayList<>()),lookup));
        return json;
    }

    public static Map<Integer,List<ItemListing>> deserialize(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) {
        Map<Integer,List<ItemListing>> result = new HashMap<>();
        for(int i = 1; i <= 5; ++i)
        {
            if(json.has("TradesLevel" + i) && json.get("TradesLevel" + i) instanceof JsonArray jsonList)
                result.put(i, deserializeList(jsonList,lookup));
            else
                result.put(i, new ArrayList<>());
        }
        return result;
    }

    public static JsonArray serializeList(@Nonnull List<ItemListing> trades, @Nonnull HolderLookup.Provider lookup) {
        JsonArray list = new JsonArray();
        for(ItemListing trade : trades)
        {
            JsonObject tj = serializeTrade(trade,lookup);
            if(tj != null)
                list.add(tj);
        }
        return list;
    }

    public static List<ItemListing> deserializeList(@Nonnull JsonArray jsonList, @Nonnull HolderLookup.Provider lookup) {
        List<ItemListing> list = new ArrayList<>();
        for(int i = 0; i < jsonList.size(); ++i)
        {
            try{
                ItemListing trade = deserializeTrade(jsonList.get(i).getAsJsonObject(),lookup);
                list.add(trade);
            } catch(Throwable t) { LightmansCurrency.LogError("Error deserializing item listing at index " + i + "!", t); }
        }
        return list;
    }

    public static <T extends ItemListing> JsonObject serializeTrade(T listing, @Nonnull HolderLookup.Provider lookup) {
        if(listing == null)
            return null;
        IItemListingSerializer serializer = serializers.get(listing.getClass());
        if(serializer == null)
            return null;
        return serializer.serialize(listing,lookup);
    }

    public static ItemListing deserializeTrade(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException{
        ResourceLocation type = VersionUtil.parseResource(GsonHelper.getAsString(json,"Type"));
        IItemListingDeserializer deserializer = deserializers.get(type);
        if(deserializer == null)
            throw new JsonSyntaxException("Could not deserialize entry as no deserializer was found of type '" + type + "'!");
        ItemListing trade = deserializer.deserialize(json,lookup);
        if(trade == null)
            throw new JsonSyntaxException("An unknown error occurred while deserializing entry!");
        return trade;
    }

    public interface IItemListingSerializer {
        ResourceLocation getType();
        @Nullable
        default JsonObject serialize(@Nonnull ItemListing trade, @Nonnull HolderLookup.Provider lookup) {
            JsonObject json = new JsonObject();
            json.addProperty("Type", this.getType().toString());
            return serializeInternal(json, trade,lookup);
        }
        @Nullable
        JsonObject serializeInternal(@Nonnull JsonObject json, @Nonnull ItemListing trade, @Nonnull HolderLookup.Provider lookup);
    }

    public interface IItemListingDeserializer { @Nonnull ItemListing deserialize(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException; }


    public static void registerDefaultSerializers() {
        //Register Simple Trade
        registerItemListing(SimpleTrade.TYPE, SimpleTrade.class, SimpleTrade.SERIALIZER);
        //Random Trade
        registerItemListing(RandomTrade.TYPE, RandomTrade.class, RandomTrade.SERIALIZER);
        //Enchantment Trades
        registerItemListing(EnchantedItemForCoinsTrade.TYPE, EnchantedItemForCoinsTrade.class, EnchantedItemForCoinsTrade.SERIALIZER);
        registerItemListing(EnchantedBookForCoinsTrade.TYPE, EnchantedBookForCoinsTrade.class, EnchantedBookForCoinsTrade.SERIALIZER);
        //Map Trades
        registerItemListing(ItemsForMapTrade.TYPE, ItemsForMapTrade.class, ItemsForMapTrade.SERIALIZER);
    }

}