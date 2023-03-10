package io.github.lightman314.lightmanscurrency.common.entity.merchant.villager;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.entity.merchant.villager.listings.*;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemListingSerializer {


    private static final Map<Class<? extends VillagerTrades.ITrade>, IItemListingSerializer> serializers = new HashMap<>();
    private static final Map<ResourceLocation, IItemListingDeserializer> deserializers = new HashMap<>();

    public static <L extends VillagerTrades.ITrade,T extends IItemListingSerializer & IItemListingDeserializer> void registerItemListing(@Nonnull ResourceLocation type, @Nonnull Class<L> clazz, @Nonnull T serializer) { registerItemListing(type, clazz, serializer, serializer); }

    public static <T extends VillagerTrades.ITrade> void registerItemListing(@Nonnull ResourceLocation type, Class<T> clazz, @Nonnull IItemListingSerializer serializer, @Nonnull IItemListingDeserializer deserializer) {
        if(serializers.containsKey(clazz))
            LightmansCurrency.LogWarning("Attempted to register a duplicate VillagerTrades.ITrade Serializer of class '" + clazz.getName() + "'!");
        else if(deserializers.containsKey(type))
            LightmansCurrency.LogWarning("Attempted to register a duplicate VillagerTrades.ITrade Deserializer of type '" + type + "'!");
        else
        {
            serializers.put(clazz, serializer);
            deserializers.put(type, deserializer);
            LightmansCurrency.LogInfo("Registered Item Listing serializer '" + type + "'");
        }
    }


    public static JsonObject serialize(Map<Integer, List<VillagerTrades.ITrade>> trades) {
        JsonObject json = new JsonObject();
        for(int i = 1; i <= 5; ++i)
            json.add("TradesLevel" + i, serializeList(trades.getOrDefault(i,new ArrayList<>())));
        return json;
    }

    public static Map<Integer,List<VillagerTrades.ITrade>> deserialize(JsonObject json) {
        Map<Integer,List<VillagerTrades.ITrade>> result = new HashMap<>();
        for(int i = 1; i <= 5; ++i)
        {
            if(json.has("TradesLevel" + i) && json.get("TradesLevel" + i) instanceof JsonArray)
            {
                JsonArray jsonList = (JsonArray)json.get("TradesLevel" + i);
                result.put(i, deserializeList(jsonList));
            }
            else
                result.put(i, new ArrayList<>());
        }
        return result;
    }

    public static JsonArray serializeList(List<VillagerTrades.ITrade> trades) {
        JsonArray list = new JsonArray();
        for(VillagerTrades.ITrade trade : trades)
        {
            JsonObject tj = serializeTrade(trade);
            if(tj != null)
                list.add(tj);
        }
        return list;
    }

    public static List<VillagerTrades.ITrade> deserializeList(JsonArray jsonList) {
        List<VillagerTrades.ITrade> list = new ArrayList<>();
        for(int i = 0; i < jsonList.size(); ++i)
        {
            try{
                VillagerTrades.ITrade trade = deserializeTrade(jsonList.get(i).getAsJsonObject());
                list.add(trade);
            } catch(Throwable t) { LightmansCurrency.LogError("Error deserializing item listing at index " + i + "!", t); }
        }
        return list;
    }

    public static <T extends VillagerTrades.ITrade> JsonObject serializeTrade(T listing) {
        if(listing == null)
            return null;
        IItemListingSerializer serializer = serializers.get(listing.getClass());
        if(serializer == null)
            return null;
        return serializer.serialize(listing);
    }

    public static VillagerTrades.ITrade deserializeTrade(JsonObject json) throws Exception{
        if(!json.has("Type"))
            throw new RuntimeException("Could not deserialize entry as no 'Type' was defined!");
        ResourceLocation type = new ResourceLocation(json.get("Type").getAsString());
        IItemListingDeserializer deserializer = deserializers.get(type);
        if(deserializer == null)
            throw new RuntimeException("Could not deserialize entry as no deserializer was found of type '" + type + "'!");
        VillagerTrades.ITrade trade = deserializer.deserialize(json);
        if(trade == null)
            throw new RuntimeException("An unknown error occurred while deserializing entry!");
        return trade;
    }

    public interface IItemListingSerializer {
        ResourceLocation getType();
        default JsonObject serialize(VillagerTrades.ITrade trade) {
            JsonObject json = new JsonObject();
            json.addProperty("Type", this.getType().toString());
            return serializeInternal(json, trade);
        }
        JsonObject serializeInternal(JsonObject json, VillagerTrades.ITrade trade);
    }

    public interface IItemListingDeserializer { VillagerTrades.ITrade deserialize(JsonObject json) throws Exception; }


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