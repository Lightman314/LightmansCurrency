package io.github.lightman314.lightmanscurrency.common.entity.merchant.villager.listings;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.entity.merchant.villager.ItemListingSerializer;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.Random;

public class ItemsForMapTrade implements VillagerTrades.ITrade
{

    public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "items_for_map");
    public static final Serializer SERIALIZER = new Serializer();

    protected final ItemStack price1;
    protected final ItemStack price2;
    protected final Structure<?> destination;
    protected final String displayName;
    protected final MapDecoration.Type mapDecorationType;
    protected final int maxTrades;
    protected final int xp;
    protected final float priceMult;

    public ItemsForMapTrade(ItemStack price, Structure<?> destination, String displayName, MapDecoration.Type mapDecorationType, int maxUses, int xpValue)
    {
        this(price, ItemStack.EMPTY, destination, displayName, mapDecorationType, maxUses, xpValue, SimpleTrade.PRICE_MULT);
    }

    public ItemsForMapTrade(ItemStack price1, ItemStack price2, Structure<?> destination, String displayName, MapDecoration.Type mapDecorationType, int maxUses, int xpValue, float priceMult)
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
    public MerchantOffer getOffer(Entity trader, @Nonnull Random rand) {

        if(!(trader.level instanceof ServerWorld))
            return null;
        else
        {
            ServerWorld serverworld = (ServerWorld)trader.level;
            BlockPos blockPos = serverworld.findNearestMapFeature(this.destination, trader.blockPosition(), 100, true);
            if(blockPos != null)
            {
                ItemStack itemstack = FilledMapItem.create(serverworld, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
                FilledMapItem.lockMap(serverworld, itemstack);
                MapData.addTargetDecoration(itemstack, blockPos, "+", this.mapDecorationType);
                itemstack.setHoverName(EasyText.translatable(this.displayName));
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
        public JsonObject serializeInternal(JsonObject json, VillagerTrades.ITrade trade) {
            if(trade instanceof ItemsForMapTrade)
            {
                ItemsForMapTrade t = (ItemsForMapTrade)trade;
                json.add("Price", FileUtil.convertItemStack(t.price1));
                if(!t.price2.isEmpty())
                    json.add("Price2", FileUtil.convertItemStack(t.price2));
                json.addProperty("Destination", t.destination.getRegistryName().toString());
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
        public VillagerTrades.ITrade deserialize(JsonObject json) throws Exception {
            ItemStack price1 = FileUtil.parseItemStack(json.get("Price").getAsJsonObject());
            ItemStack price2 = json.has("Price2") ? FileUtil.parseItemStack(json.get("Price2").getAsJsonObject()) : ItemStack.EMPTY;
            Structure<?> destination = ForgeRegistries.STRUCTURE_FEATURES.getValue(new ResourceLocation(json.get("Destination").getAsString()));
            String displayName = json.get("MapName").getAsString();
            MapDecoration.Type mapDecorationType = EnumUtil.enumFromString(json.get("Decoration").getAsString(), MapDecoration.Type.values(), MapDecoration.Type.FRAME);
            int maxTrades = json.get("MaxTrades").getAsInt();
            int xp = json.get("XP").getAsInt();
            float priceMult = json.get("PriceMult").getAsFloat();
            return new ItemsForMapTrade(price1, price2, destination, displayName, mapDecorationType, maxTrades, xp, priceMult);
        }
    }

}