package io.github.lightman314.lightmanscurrency.common.seasonal_events;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.data.types.EventRewardDataCache;
import io.github.lightman314.lightmanscurrency.common.seasonal_events.chocolate.ChocolateEventCoins;
import io.github.lightman314.lightmanscurrency.common.seasonal_events.data.EventData;
import io.github.lightman314.lightmanscurrency.common.seasonal_events.data.EventRange;
import io.github.lightman314.lightmanscurrency.common.items.ancient_coins.AncientCoinType;
import io.github.lightman314.lightmanscurrency.common.loot.LootManager;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class SeasonalEventManager {

    private static final List<EventData> loadedEvents = new ArrayList<>();
    private static int timer = 0;

    public static final String EVENTS_FILENAME = "config/lightmanscurrency/SeasonalEvents.json";

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerStart(ServerStartedEvent event) { reload(); }

    public static void reload() {

        //Unregister all currently loaded events
        for(EventData event : loadedEvents)
            LootManager.removeLootModifier(event);
        loadedEvents.clear();

        File file = new File(EVENTS_FILENAME);
        if(!file.exists())
        {
            //Create default file and load it
            loadedEvents.addAll(getDefaultEvents());
            //Register the events as a loot modifier
            for(EventData event : loadedEvents)
                LootManager.addLootModifier(event);
            saveCurrentEvents();
        }
        else
        {
            try {
                JsonObject json = GsonHelper.parse(Files.readString(file.toPath()));
                JsonArray events = GsonHelper.getAsJsonArray(json,"Events");
                List<String> loadedEventIDS = new ArrayList<>();
                for(int i = 0; i < events.size(); ++i)
                {
                    try {
                        JsonObject entry = GsonHelper.convertToJsonObject(events.get(i),"Events[" + i + "]");
                        EventData event = EventData.fromJson(entry);
                        //Confirm that the new event doesn't have a duplicate id
                        if(loadedEventIDS.contains(event.eventID))
                            throw new JsonSyntaxException("Cannot have two events with the ID of '" + event.eventID + "'!");
                        loadedEvents.add(event);
                        //Note which event id was just loaded to test future events
                        loadedEventIDS.add(event.eventID);
                        LootManager.addLootModifier(event);
                    } catch (Exception e) { LightmansCurrency.LogWarning("Error loading Seasonal Event #" + i,e); }
                }
            } catch (Exception e) {
                LightmansCurrency.LogWarning("Error loading Seasonal Events",e);
            }
        }
    }

    private static void saveCurrentEvents()
    {
        JsonObject json = new JsonObject();
        JsonArray events = new JsonArray();
        for(EventData event : loadedEvents)
            events.add(event.toJson());
        json.add("Events",events);
        try {
            File file = new File(EVENTS_FILENAME);
            FileUtil.writeStringToFile(file,FileUtil.GSON.toJson(json));
        } catch (IOException e) { LightmansCurrency.LogError("Error writing Seasonal Events to file!",e);}
    }

    private static List<EventData> getDefaultEvents()
    {
        List<EventData> list = new ArrayList<>();
        list.add(ChocolateEventCoins.lazyEvent(ChocolateEventCoins.CHRISTMAS,"builtin/christmas",LCText.SEASONAL_EVENT_CHRISTMAS.get()));
        list.add(ChocolateEventCoins.lazyEvent(ChocolateEventCoins.VALENTINES,"builtin/valentines",LCText.SEASONAL_EVENT_VALENTINES.get()));
        list.add(ChocolateEventCoins.lazyEvent(ChocolateEventCoins.HALLOWEEN,"builtin/halloween",LCText.SEASONAL_EVENT_HALLOWEEN.get()));
        //Special birthday event for playing on my birthday
        list.add(EventData.builder("builtin/birthday")
                .dateRange(EventRange.create(3,29,3,29))
                .startingReward(AncientCoinType.randomizingItem(5))
                .startingRewardMessage(LCText.SEASONAL_EVENT_BIRTHDAY.get())
                .build());
        return list;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event)
    {
        if(event.phase == TickEvent.Phase.END)
            return;
        //Check for player event rewards every 5 minutes
        if(++timer >= 6000)
        {
            timer = 0;
            EventRewardDataCache data = EventRewardDataCache.TYPE.get(false);
            if(data == null)
            {
                LightmansCurrency.LogWarning("Could not get event reward data.");
                return;
            }
            LightmansCurrency.LogDebug("Manually checking for Seasonal Event rewards and clearing inactive events from the reward cache");
            //Check player rewards
            for(ServerPlayer player : event.getServer().getPlayerList().getPlayers())
                checkPlayerRewards(player,data);
            //Check if an event has ended and their data should be cleared
            data.clearDisabledEvents(new ArrayList<>(loadedEvents));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if(event.getEntity() instanceof ServerPlayer player)
        {
            EventRewardDataCache data = EventRewardDataCache.TYPE.get(false);
            if(data != null)
                checkPlayerRewards(player,data);
        }
    }

    private static void checkPlayerRewards(ServerPlayer player,EventRewardDataCache data)
    {
        if(!LCConfig.COMMON.eventStartingRewards.get())
            return;
        for(EventData event : loadedEvents)
        {
            if(event.hasStartingReward() && event.range.isActive() && data.shouldGivePlayerReward(player,event.eventID))
            {
                //Give the player the reward
                event.giveStartingReward(player);
                data.playerReceivedReward(player,event.eventID);
            }
        }
    }

}