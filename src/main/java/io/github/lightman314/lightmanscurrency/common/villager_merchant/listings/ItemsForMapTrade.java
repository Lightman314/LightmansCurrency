package io.github.lightman314.lightmanscurrency.common.villager_merchant.listings;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.ItemListingSerializer;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import javax.annotation.Nonnull;

public class ItemsForMapTrade extends ItemsForXTradeTemplate
{

    public static final ResourceLocation TYPE = VersionUtil.lcResource( "items_for_map");
    public static final Serializer SERIALIZER = new Serializer();


    protected final TagKey<Structure> destination;
    protected final String displayName;
    protected final Holder<MapDecorationType> mapDecorationType;

    public ItemsForMapTrade(ItemStack price, TagKey<Structure> destination, String displayName, Holder<MapDecorationType> mapDecorationType, int maxUses, int xpValue)
    {
        this(price, ItemStack.EMPTY, destination, displayName, mapDecorationType, maxUses, xpValue, SimpleTrade.PRICE_MULT);
    }

    public ItemsForMapTrade(ItemStack price1, ItemStack price2, TagKey<Structure> destination, String displayName, Holder<MapDecorationType> mapDecorationType, int maxUses, int xpValue, float priceMult)
    {
        super(price1,price2,maxUses,xpValue,priceMult);
        this.destination = destination;
        this.displayName = displayName;
        this.mapDecorationType = mapDecorationType;
    }
    private ItemsForMapTrade(@Nonnull DeserializedData data, TagKey<Structure> destination, String displayName, Holder<MapDecorationType> mapDecorationType)
    {
        super(data);
        this.destination = destination;
        this.displayName = displayName;
        this.mapDecorationType = mapDecorationType;
    }

    @Override
    protected ItemStack createResult(@Nonnull Entity trader, @Nonnull RandomSource rand) {
        if(trader == null || (!(trader.level() instanceof ServerLevel level)))
            return null;
        else
        {
            BlockPos blockPos = level.findNearestMapStructure(this.destination, trader.blockPosition(), 100, true);
            if(blockPos != null)
            {
                ItemStack itemstack = MapItem.create(level, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
                MapItem.lockMap(level, itemstack);
                MapItemSavedData.addTargetDecoration(itemstack, blockPos, "+", this.mapDecorationType);
                itemstack.set(DataComponents.CUSTOM_NAME,Component.translatable(this.displayName));
                return itemstack;
            }
            else
                return null;
        }
    }

    public static class Serializer implements ItemListingSerializer.IItemListingSerializer, ItemListingSerializer.IItemListingDeserializer {

        private Serializer() {}

        @Override
        public ResourceLocation getType() { return TYPE; }

        @Nonnull
        @Override
        public JsonObject serializeInternal(@Nonnull JsonObject json, @Nonnull ItemListing trade, @Nonnull HolderLookup.Provider lookup) {
            if(trade instanceof ItemsForMapTrade t)
            {
                t.serializeData(json,lookup);
                json.addProperty("Destination", t.destination.location().toString());
                json.addProperty("MapName", t.displayName);
                json.addProperty("Decoration", BuiltInRegistries.MAP_DECORATION_TYPE.getKey(t.mapDecorationType.value()).toString());
                return json;
            }
            return null;
        }

        @Nonnull
        @Override
        public ItemListing deserialize(@Nonnull JsonObject json, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException {
            DeserializedData data = deserializeData(json,lookup);
            TagKey<Structure> destination = TagKey.create(Registries.STRUCTURE, VersionUtil.parseResource(GsonHelper.getAsString(json,"Destination")));
            String displayName = GsonHelper.getAsString(json,"MapName");
            Holder<MapDecorationType> mapDecorationType = BuiltInRegistries.MAP_DECORATION_TYPE.getHolder(VersionUtil.parseResource(GsonHelper.getAsString(json,"Decoration"))).orElseThrow(() -> new JsonSyntaxException(GsonHelper.getAsString(json,"Decoration") + " is not a valid decoration type!"));
            return new ItemsForMapTrade(data, destination, displayName, mapDecorationType);
        }
    }

}
