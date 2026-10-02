package io.github.lightman314.lightmanscurrency.api.trader.rules.data;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.helpers.time.TimeHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISyncingNode;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.core.UUIDUtil;

import java.util.*;


public final class InteractionMemory implements ISidedContext.Mutable<InteractionMemory> {

    public static final Codec<InteractionMemory> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC,Codec.LONG.listOf())
            .xmap(InteractionMemory::new, InteractionMemory::getMemory);

    private ISidedContext context = ISidedContext.LOGICAL_CLIENT;
    @Override
    public InteractionMemory setSidedContext(ISidedContext context) { this.context = context; return this; }
    @Override
    public boolean isClient() { return this.context.isClient(); }

    private final Map<UUID,List<Long>> memory = new HashMap<>();
    private Map<UUID,List<Long>> getMemory() { return this.memory; }
    private ISyncingNode.SyncingListener listener = (w, p) -> {};

    public InteractionMemory withListener(ISyncingNode.SyncingListener listener) { this.listener = listener; return this; }

    public boolean isEmpty() { return this.memory.isEmpty(); }

    public InteractionMemory() {}
    private InteractionMemory(Map<UUID,List<Long>> memory) {
        //Add each entry manually so that they're modifiable array lists
        memory.forEach((id,list) -> this.memory.put(id,new ArrayList<>(list)));
    }

    public FancyPacketMap createFullSyncPacket(ISyncingContext context) {
        FancyPacketMap.Mutable memoryMap = FancyPacketMap.map();
        //Loop through the customer set instead of the memory set as realistically speaking
        // most players won't be tracking more than 2 players memory at any given time
        // (their own, plus the memory of whatever trader interface they're interacting with)
         for(UUID entry : context.getCustomerSet()) {
             if(this.memory.containsKey(entry))
                 memoryMap.setList(entry.toString(),LCFancyPacketTypes.LONG,this.memory.get(entry));
         }
         return memoryMap;
    }

    public void handlePacket(FancyPacketMap message) {
        for(String key : message.keySet()) {
            try {
                UUID entry = UUID.fromString(key);
                List<Long> data = message.getList(key,LCFancyPacketTypes.LONG);
                if(data.isEmpty())
                    this.memory.remove(entry);
                else
                    this.memory.put(entry,new ArrayList<>(data));
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private void setMemoryChanged(UUID entry) {
        boolean empty = !this.memory.containsKey(entry);
        this.listener.accept(builder ->
                builder.setList(entry.toString(),LCFancyPacketTypes.LONG,this.memory.getOrDefault(entry,List.of())),
                c -> empty || c.isValidTarget(entry));
    }

    public int getCount(TradeEvent event,long timeLimit) {
        if(!event.isEditingView())
            return getCount(event.getCustomer().getID(),timeLimit);
        return 0;
    }

    public int getCount(UUID player,long timeLimit) {
        int count = 0;
        this.clearExpiredData(timeLimit);

        if(this.memory.containsKey(player)) {
            //Since we already cleared the expired data, we can simply use the list size here
            if(this.isServer() || timeLimit <= 0)
                return this.memory.get(player).size();
            //Filter results on the client as we can't safely clear the expired data
            return (int)this.memory.get(player).stream().filter(e -> TimeHelper.timerNotExpired(e,timeLimit)).count();
        }
        return count;
    }

    public long getTimeRemaining(TradeEvent event,long timeLimit) {
        if(!event.isEditingView())
            return this.getTimeRemaining(event.getCustomer().getID(),timeLimit);
        return 0;
    }

    public long getTimeRemaining(UUID player,long timeLimit) {
        if(timeLimit <= 0)
            return Long.MAX_VALUE;
        this.clearExpiredData(timeLimit);
        if(this.memory.containsKey(player)) {
            long minTime = Long.MAX_VALUE;
            List<Long> eventTimes = this.memory.get(player);
            for(long eventTime : eventTimes) {
                if(this.isServer() || TimeHelper.timerNotExpired(eventTime,timeLimit))
                    minTime = Math.min(minTime,eventTime);
            }
            if(minTime < Long.MAX_VALUE)
                return Math.max(timeLimit + minTime - TimeHelper.getCurrentTime(),0);
        }
        return 0;
    }

    public void addEntry(TradeEvent event) {
        if(!event.isEditingView())
            this.addEntry(event.getCustomer().getID());
    }
    public void addEntry(UUID player) {
        List<Long> list = this.memory.computeIfAbsent(player,p -> new ArrayList<>());
        list.add(TimeHelper.getCurrentTime());
        this.setMemoryChanged(player);
    }

    public void clear() {
        Set<UUID> oldKeySet = new HashSet<>(this.memory.keySet());
        this.memory.clear();
        for(UUID key : oldKeySet)
            this.setMemoryChanged(key);
    }

    public void clearExpiredData(long timeLimit) {
        if(timeLimit <= 0 || this.isClient())
            return;
        for(UUID id : new ArrayList<>(this.memory.keySet())) {
            List<Long> eventTimes = this.memory.get(id);
            boolean changed = false;
            for(int i = 0; i < eventTimes.size(); ++i) {
                long stamp = eventTimes.get(i);
                if(TimeHelper.timerExpired(stamp,timeLimit)) {
                    changed = true;
                    eventTimes.remove(i--);
                }
            }
            if(changed) {
                if(eventTimes.isEmpty())
                    this.memory.remove(id);
                this.setMemoryChanged(id);
            }
        }
    }

}