package io.github.lightman314.lightmanscurrency.common.data.types;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomData;
import io.github.lightman314.lightmanscurrency.api.misc.data.CustomDataType;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.seasonal_events.data.EventData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EventRewardDataCache extends CustomData {

    public static final CustomDataType<EventRewardDataCache> TYPE = new CustomDataType<>("lightmanscurrency_event_rewards",EventRewardDataCache::new,true);

    private final Map<String,List<UUID>> rewardData = new HashMap<>();

    @Override
    public CustomDataType<?> getType() { return TYPE; }

    public boolean shouldGivePlayerReward(ServerPlayer player, String eventID)
    {
        if(!LCConfig.COMMON.eventStartingRewards.get())
            return false;
        return !this.rewardData.getOrDefault(eventID,new ArrayList<>()).contains(player.getUUID());
    }

    public void playerReceivedReward(ServerPlayer player, String eventID)
    {
        List<UUID> players = this.rewardData.getOrDefault(eventID,new ArrayList<>());
        if(!players.contains(player.getUUID()))
        {
            players.add(player.getUUID());
            this.rewardData.put(eventID,players);
            this.setChanged();
        }
    }

    public void clearDisabledEvents(List<EventData> events)
    {
        for(EventData event : events)
        {
            if(this.rewardData.containsKey(event.eventID) && !event.range.isActive())
            {
                this.rewardData.remove(event.eventID);
                this.setChanged();
            }
        }
    }

    public boolean clearEventCache(String eventID)
    {
        if(this.rewardData.containsKey(eventID))
        {
            this.rewardData.remove(eventID);
            this.setChanged();
            return true;
        }
        return false;
    }

    @Override
    public void save(CompoundTag tag, HolderLookup.Provider lookup) {
        ListTag list = new ListTag();
        this.rewardData.forEach((event,players) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("Event",event);
            ListTag playerList = new ListTag();
            for(UUID id : players)
                playerList.add(NbtUtils.createUUID(id));
            entry.put("Players",playerList);
            list.add(entry);
        });
        tag.put("Data",list);
    }

    @Override
    protected void load(CompoundTag tag, HolderLookup.Provider lookup) {
        this.rewardData.clear();
        ListTag list = tag.getList("Data",Tag.TAG_COMPOUND);
        for(int i = 0; i < list.size(); ++i)
        {
            CompoundTag entry = list.getCompound(i);
            String event = entry.getString("Event");
            List<UUID> players = new ArrayList<>();
            ListTag playerList = entry.getList("Players",Tag.TAG_INT_ARRAY);
            for (Tag value : playerList) {
                players.add(NbtUtils.loadUUID(value));
            }
            this.rewardData.put(event,players);
        }
    }

    @Override
    protected void parseSyncPacket(LazyPacketData message, HolderLookup.Provider lookup) {}
    @Override
    public void onPlayerJoin(ServerPlayer player) { }

}
