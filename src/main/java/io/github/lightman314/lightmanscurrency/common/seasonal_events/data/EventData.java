package io.github.lightman314.lightmanscurrency.common.seasonal_events.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI;
import io.github.lightman314.lightmanscurrency.common.advancements.date.DatePredicate;
import io.github.lightman314.lightmanscurrency.common.loot.ConfigItemTier;
import io.github.lightman314.lightmanscurrency.common.loot.modifier.SimpleLootModifier;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.EventCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.types.TextNotification;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EventData extends SimpleLootModifier {

    public final String eventID;
    public final EventRange range;
    private final double replacementRate;
    private final Map<ConfigItemTier,Item> replacementItems;
    private final List<ItemStack> startingRewards;
    public boolean hasStartingReward() { return !this.startingRewards.isEmpty(); }
    @Nullable
    private Component startingRewardMessage;

    private EventData(JsonObject json) throws JsonSyntaxException, ResourceLocationException
    {
        this.eventID = GsonHelper.getAsString(json,"ID");
        this.range = EventRange.fromJson(GsonHelper.getAsJsonObject(json,"dates"));
        this.replacementRate = MathUtil.clamp(GsonHelper.getAsDouble(json,"replacementRate",0d),0d,1d);
        Map<ConfigItemTier,Item> temp = new HashMap<>();
        for(ConfigItemTier tier : ConfigItemTier.values())
        {
            String key = replacementItemKey(tier);
            if(json.has(key))
                temp.put(tier,GsonHelper.getAsItem(json,key));
        }
        this.replacementItems = ImmutableMap.copyOf(temp);
        if(!this.replacementItems.isEmpty() && this.replacementRate <= 0d)
            throw new JsonSyntaxException("Cannot have a 0% replacement rate while replacement items are defined!");
        JsonArray rewardsList = GsonHelper.getAsJsonArray(json,"startingRewards",new JsonArray());
        List<ItemStack> temp2 = new ArrayList<>();
        for(int i = 0; i < rewardsList.size(); ++i)
        {
            try {
                temp2.add(FileUtil.parseItemStack(GsonHelper.convertToJsonObject(rewardsList.get(i),"startingRewards[" + i + "]")));
            }catch (JsonSyntaxException e) {
                LightmansCurrency.LogWarning("Error loading startingRewards[" + i + "]",e);
            }
        }
        this.startingRewards = ImmutableList.copyOf(temp2);
        if(this.replacementItems.isEmpty() && this.startingRewards.isEmpty())
            throw new JsonSyntaxException("Cannot have an Event with no loot replacement items AND no one-time reward defined!");
        if(!this.startingRewards.isEmpty() && json.has("startingRewardMessage"))
            this.startingRewardMessage = Component.Serializer.fromJson(json.get("startingRewardMessage"));
    }
    private EventData(Builder builder) throws IllegalArgumentException
    {
        this.eventID = builder.eventID;
        this.range = builder.buildRange();
        this.replacementRate = builder.replacementRate;
        this.replacementItems = ImmutableMap.copyOf(builder.replacementItems);
        if(!this.replacementItems.isEmpty() && this.replacementRate <= 0d)
            throw new IllegalArgumentException("Cannot have a replacement rate of 0% while replacement items are defined!");
        this.startingRewards = ImmutableList.copyOf(builder.startingRewards);
        if(this.replacementItems.isEmpty() && this.startingRewards.isEmpty())
            throw new IllegalArgumentException("Cannot have an Event with no loot replacement items AND no starting reward defined!");
        this.startingRewardMessage = builder.startingRewardMessage;
    }

    public JsonObject toJson()
    {
        JsonObject json = new JsonObject();
        json.addProperty("ID",this.eventID);
        json.add("dates",this.range.toJson());
        if(!this.replacementItems.isEmpty()) //Don't bother writing the replacement rate if no replacement items are defined
            json.addProperty("replacementRate",this.replacementRate);
        //Write the tiers in-order so that the default config will be more legible
        for(ConfigItemTier tier : ConfigItemTier.values())
        {
            if(this.replacementItems.containsKey(tier))
            {
                Item item = this.replacementItems.get(tier);
                ResourceLocation itemID = ForgeRegistries.ITEMS.getKey(item);
                json.addProperty(replacementItemKey(tier),itemID.toString());
            }
        }
        if(!this.startingRewards.isEmpty())
        {
            JsonArray rewardsList = new JsonArray();
            for(ItemStack item : this.startingRewards)
                rewardsList.add(FileUtil.convertItemStack(item));
            json.add("startingRewards",rewardsList);
            if(this.startingRewardMessage != null)
                json.add("startingRewardMessage",Component.Serializer.toJsonTree(this.startingRewardMessage));
        }
        return json;
    }

    private static String replacementItemKey(ConfigItemTier tier) { return "replacementItem" + tier; }

    public static EventData fromJson(JsonObject json) throws JsonSyntaxException, ResourceLocationException { return new EventData(json); }

    public void giveStartingReward(ServerPlayer player)
    {
        for(ItemStack item : this.startingRewards)
            ItemHandlerHelper.giveItemToPlayer(player,item.copy());
        if(this.startingRewardMessage != null)
        {
            player.sendSystemMessage(this.startingRewardMessage);
            NotificationAPI.API.PushPlayerNotification(player.getUUID(),new TextNotification(EasyText.makeMutable(this.startingRewardMessage), EventCategory.INSTANCE),false);
        }
    }

    @Override
    protected void replaceLoot(RandomSource random, List<ItemStack> loot) {
        for(ConfigItemTier tier : ConfigItemTier.values())
        {
            if(this.replacementItems.containsKey(tier))
                this.replaceRandomItems(random,loot,tier.getItem(),this.replacementItems.get(tier));
        }
    }

    @Override
    public boolean isEnabled() { return this.range.isActive() && LCConfig.COMMON.eventLootReplacements.get(); }
    @Override
    protected double getSuccessChance() { return this.replacementRate; }

    public static Builder builder(String eventID) { return new Builder(eventID); }

    public static class Builder
    {
        private Builder(String eventID) { this.eventID = eventID; }

        private final String eventID;
        private DatePredicate start;
        private DatePredicate end;
        private EventRange range;
        private EventRange buildRange() { return this.range != null ? this.range : EventRange.create(Objects.requireNonNull(this.start),Objects.requireNonNull(this.end)); }

        private double replacementRate = 0d;

        private final Map<ConfigItemTier,Item> replacementItems = new HashMap<>();

        private final List<ItemStack> startingRewards = new ArrayList<>();

        @Nullable
        private Component startingRewardMessage = null;

        public Builder startDate(int month, int day) { this.start = new DatePredicate(month,day); return this; }
        public Builder startDate(DatePredicate date) { this.start = date; return this; }
        public Builder endDate(int month, int day) { this.end = new DatePredicate(month,day); return this; }
        public Builder endDate(DatePredicate date) { this.end = date; return this; }
        public Builder dateRange(int startMonth, int startDay, int endMonth, int endDay) { return this.startDate(startMonth,startDay).endDate(endMonth,endDay); }
        public Builder dateRange(DatePredicate startDate, DatePredicate endDate) { return this.startDate(startDate).endDate(endDate); }
        public Builder dateRange(EventRange range) { this.range = range; return this; }

        public Builder replacementRate(double replacementRate) { this.replacementRate = MathUtil.clamp(replacementRate,0d,1d); return this; }

        public Builder replacementItem(ConfigItemTier tier, Item item) { this.replacementItems.put(tier,item); return this; }

        public Builder startingReward(ItemLike item) { return this.startingReward(item,1); }
        public Builder startingReward(Supplier<? extends ItemLike> item) { return this.startingReward(item.get()); }
        public Builder startingReward(ItemLike item, int count) { return this.startingReward(new ItemStack(item,count)); }
        public Builder startingReward(Supplier<? extends ItemLike> item, int count) { return this.startingReward(item.get(),count); }
        public Builder startingReward(ItemStack item) { this.startingRewards.add(item.copy()); return this; }
        public Builder startingReward(List<ItemStack> items) { this.startingRewards.addAll(InventoryUtil.copyList(items)); return this; }

        public Builder startingRewardMessage(@Nullable Component message) { this.startingRewardMessage = message; return this; }

        public EventData build() throws IllegalArgumentException { return new EventData(this); }

    }

}