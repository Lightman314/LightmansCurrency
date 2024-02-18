package io.github.lightman314.lightmanscurrency.common.villager_merchant;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.*;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemListingSerializer {


    private static final Map<Class<? extends ItemListing>, IItemListingSerializer> serializers = new HashMap<>();
    private static final Map<ResourceLocation, IItemListingDeserializer> deserializers = new HashMap<>();

    public static <L extends ItemListing,T extends IItemListingSerializer & IItemListingDeserializer> void registerItemListing(@NotNull ResourceLocation type, @NotNull Class<L> clazz, @NotNull T serializer) { registerItemListing(type, clazz, serializer, serializer); }

    public static <T extends ItemListing> void registerItemListing(@NotNull ResourceLocation type, Class<T> clazz, @NotNull IItemListingSerializer serializer, @NotNull IItemListingDeserializer deserializer) {
        if(serializers.containsKey(clazz))
            LightmansCurrency.LogWarning("Attempted to registerNotification a duplicate ItemListing Serializer of class '" + clazz.getName() + "'!");
        else if(deserializers.containsKey(type))
            LightmansCurrency.LogWarning("Attempted to registerNotification a duplicate ItemListing Deserializer of type '" + type + "'!");
        else
        {
            serializers.put(clazz, serializer);
            deserializers.put(type, deserializer);
            LightmansCurrency.LogInfo("Registered Item Listing serializer '" + type + "'");
        }
    }


    public static JsonObject serialize(Map<Integer, List<ItemListing>> trades, int count) {
        JsonObject json = new JsonObject();
        for(int i = 1; i <= count; ++i)
            json.add("TradesLevel" + i, serializeList(trades.getOrDefault(i,new ArrayList<>())));
        return json;
    }

    public static Map<Integer,List<ItemListing>> deserialize(JsonObject json) {
        Map<Integer,List<ItemListing>> result = new HashMap<>();
        for(int i = 1; i <= 5; ++i)
        {
            if(json.has("TradesLevel" + i) && json.get("TradesLevel" + i) instanceof JsonArray jsonList)
                result.put(i, deserializeList(jsonList));
            else
                result.put(i, new ArrayList<>());
        }
        return result;
    }

    public static JsonArray serializeList(List<ItemListing> trades) {
        JsonArray list = new JsonArray();
        for(ItemListing trade : trades)
        {
            JsonObject tj = serializeTrade(trade);
            if(tj != null)
                list.add(tj);
        }
        return list;
    }

    public static List<ItemListing> deserializeList(JsonArray jsonList) {
        List<ItemListing> list = new ArrayList<>();
        for(int i = 0; i < jsonList.size(); ++i)
        {
            try{
                ItemListing trade = deserializeTrade(jsonList.get(i).getAsJsonObject());
                list.add(trade);
            } catch(Throwable t) { LightmansCurrency.LogError("Error deserializing item listing at index " + i + "!", t); }
        }
        return list;
    }

    public static <T extends ItemListing> JsonObject serializeTrade(T listing) {
        if(listing == null)
            return null;
        IItemListingSerializer serializer = serializers.get(listing.getClass());
        if(serializer == null)
            return null;
        return serializer.serialize(listing);
    }

    public static ItemListing deserializeTrade(JsonObject json) throws JsonSyntaxException, ResourceLocationException{
        ResourceLocation type = new ResourceLocation(GsonHelper.getAsString(json,"Type"));
        IItemListingDeserializer deserializer = deserializers.get(type);
        if(deserializer == null)
            throw new JsonSyntaxException("Could not deserialize entry as no deserializer was found of type '" + type + "'!");
        ItemListing trade = deserializer.deserialize(json);
        if(trade == null)
            throw new JsonSyntaxException("An unknown error occurred while deserializing entry!");
        return trade;
    }

    public interface IItemListingSerializer {
        ResourceLocation getType();
        default JsonObject serialize(ItemListing trade) {
            JsonObject json = new JsonObject();
            json.addProperty("Type", this.getType().toString());
            return serializeInternal(json, trade);
        }
        JsonObject serializeInternal(JsonObject json, ItemListing trade);
    }

    public interface IItemListingDeserializer { ItemListing deserialize(JsonObject json) throws JsonSyntaxException, ResourceLocationException; }


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