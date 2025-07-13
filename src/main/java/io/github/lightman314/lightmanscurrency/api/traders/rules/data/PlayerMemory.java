package io.github.lightman314.lightmanscurrency.api.traders.rules.data;

import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerMemory {

    private final Map<UUID, List<Long>> memory = new HashMap<>();

    public int getCount(TradeEvent event, long timeLimit) { return getCount(event.getContext().getPlayerReference().id,timeLimit); }

    public int getCount(UUID player, long timeLimit) {
        int count = 0;
        if(this.memory.containsKey(player))
        {
            List<Long> eventTimes = this.memory.get(player);
            if(timeLimit <= 0)
                return eventTimes.size();
            for (Long eventTime : eventTimes) {
                if (TimeUtil.compareTime(timeLimit, eventTime))
                    count++;
            }
        }
        return count;
    }

    public void addEntry(UUID player)
    {
        List<Long> list = this.memory.getOrDefault(player,new ArrayList<>());
        list.add(TimeUtil.getCurrentTime());
        this.memory.put(player,list);
    }

    public void clear() { this.memory.clear(); }
    public boolean clearExpiredData(long timeLimit)
    {
        if(timeLimit <= 0)
            return false;
        AtomicBoolean changed = new AtomicBoolean(false);
        List<UUID> emptyEntries = new ArrayList<>();
        this.memory.forEach((id, eventTimes) ->{
            for(int i = 0; i < eventTimes.size(); i++)
            {
                if(!TimeUtil.compareTime(timeLimit, eventTimes.get(i)))
                {
                    eventTimes.remove(i);
                    i--;
                    changed.set(true);
                }
            }
            if(eventTimes.isEmpty())
                emptyEntries.add(id);
        });
        emptyEntries.forEach(this.memory::remove);
        return changed.get();
    }

    public void save(CompoundTag compound)
    {
        final ListTag memoryList = new ListTag();
        this.memory.forEach((id,entries) -> {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("ID", id);
            tag.putLongArray("Times", entries);
            memoryList.add(tag);
        });
        compound.put("Memory", memoryList);
    }

    public void load(CompoundTag compound)
    {
        if(compound.contains("Memory", Tag.TAG_LIST))
        {
            this.memory.clear();
            ListTag memoryList = compound.getList("Memory", Tag.TAG_COMPOUND);
            for(int i = 0; i < memoryList.size(); i++)
            {
                CompoundTag tag = memoryList.getCompound(i);
                if(tag.contains("ID"))
                {
                    List<Long> eventTimes = new ArrayList<>();
                    for(long time : tag.getLongArray("Times"))
                        eventTimes.add(time);
                    this.memory.put(tag.getUUID("ID"),eventTimes);
                }
            }
        }
    }

}
