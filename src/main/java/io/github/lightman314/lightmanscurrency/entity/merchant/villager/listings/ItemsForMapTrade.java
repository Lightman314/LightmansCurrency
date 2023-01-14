package io.github.lightman314.lightmanscurrency.entity.merchant.villager.listings;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.entity.merchant.villager.ItemListingSerializer;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ItemsForMapTrade implements ItemListing
{

    public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "items_for_map");
    public static final Serializer SERIALIZER = new Serializer();

    protected final ItemStack price1;
    protected final ItemStack price2;
    protected final TagKey<ConfiguredStructureFeature<?, ?>> destination;
    protected final String displayName;
    protected final MapDecoration.Type mapDecorationType;
    protected final int maxTrades;
    protected final int xp;
    protected final float priceMult;

    public ItemsForMapTrade(ItemStack price, TagKey<ConfiguredStructureFeature<?, ?>> destination, String displayName, MapDecoration.Type mapDecorationType, int maxUses, int xpValue)
    {
        this(price, ItemStack.EMPTY, destination, displayName, mapDecorationType, maxUses, xpValue, SimpleTrade.PRICE_MULT);
    }

    public ItemsForMapTrade(ItemStack price1, ItemStack price2, TagKey<ConfiguredStructureFeature<?, ?>> destination, String displayName, MapDecoration.Type mapDecorationType, int maxUses, int xpValue, float priceMult)
    {
        this.price1 = price1;
        this.price2 = price2;
        this.destination = destination;
        this.displayName = displayName;
        this.mapDecorationType = mapDecorationType;
        this.maxTrades = maxUses;
        this.xp = xpValue;
        this.priceMult = priceMult;
    }

    @Override
    public MerchantOffer getOffer(Entity trader, @NotNull Random rand) {

        if(!(trader.level instanceof ServerLevel serverworld))
            return null;
        else
        {
            BlockPos blockPos = serverworld.findNearestMapFeature(this.destination, trader.blockPosition(), 100, true);
            if(blockPos != null)
            {
                ItemStack itemstack = MapItem.create(serverworld, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
                MapItem.lockMap(serverworld, itemstack);
                MapItemSavedData.addTargetDecoration(itemstack, blockPos, "+", this.mapDecorationType);
                itemstack.setHoverName(new TranslatableComponent(this.displayName));
                return new MerchantOffer(this.price1, this.price2, itemstack, this.maxTrades, this.xp, this.priceMult);
            }
            else
                return null;
        }
    }

    private static class Serializer implements ItemListingSerializer.IItemListingSerializer, ItemListingSerializer.IItemListingDeserializer {

        @Override
        public ResourceLocation getType() { return TYPE; }

        @Override
        public JsonObject serializeInternal(JsonObject json, ItemListing trade) {
            if(trade instanceof ItemsForMapTrade t)
            {
                json.add("Price", FileUtil.convertItemStack(t.price1));
                if(!t.price2.isEmpty())
                    json.add("Price2", FileUtil.convertItemStack(t.price2));
                json.addProperty("Destination", t.destination.location().toString());
                json.addProperty("MapName", t.displayName);
                json.addProperty("Decoration", t.mapDecorationType.toString());
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
            TagKey<ConfiguredStructureFeature<?, ?>> destination = TagKey.create(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY, new ResourceLocation(json.get("Destination").getAsString()));
            String displayName = json.get("MapName").getAsString();
            MapDecoration.Type mapDecorationType = EnumUtil.enumFromString(json.get("Decoration").getAsString(), MapDecoration.Type.values(), MapDecoration.Type.FRAME);
            int maxTrades = json.get("MaxTrades").getAsInt();
            int xp = json.get("XP").getAsInt();
            float priceMult = json.get("PriceMult").getAsFloat();
            return new ItemsForMapTrade(price1, price2, destination, displayName, mapDecorationType, maxTrades, xp, priceMult);
        }
    }

}