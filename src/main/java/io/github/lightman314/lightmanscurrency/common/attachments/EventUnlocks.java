package io.github.lightman314.lightmanscurrency.common.attachments;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.core.ModAttachmentTypes;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.network.message.event.SPacketSyncEventUnlocks;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class EventUnlocks implements INBTSerializable<CompoundTag>, IClientTracker
{

    public static EventUnlocks create(IAttachmentHolder holder) { return new EventUnlocks(holder); }

    @Override
    public boolean isClient() { return this.parent == null || this.parent.level().isClientSide; }

    private final Entity parent;
    private final IAttachmentHolder holder;
    private EventUnlocks(IAttachmentHolder holder) {
        this.holder = holder;
        if(holder instanceof Entity e)
            this.parent = e;
        else
            this.parent = null;
    }

    private final List<String> unlocked = new ArrayList<>();

    public List<String> getUnlockedList() { return ImmutableList.copyOf(this.unlocked); }

    public static boolean isUnlocked(Player player, String eventChain) { return player.getData(ModAttachmentTypes.EVENT_UNLOCKS).isUnlocked(eventChain); }
    public static void unlock(Player player, String eventChain) { player.getData(ModAttachmentTypes.EVENT_UNLOCKS).unlock(eventChain); }
    public static void lock(Player player, String eventChain) { player.getData(ModAttachmentTypes.EVENT_UNLOCKS).lock(eventChain); }

    public boolean isUnlocked(String eventChain) { return this.unlocked.contains(eventChain); }

    public void unlock(String eventChain) {
        if(!this.unlocked.contains(eventChain))
        {
            this.unlocked.add(eventChain);
            this.setChanged();
        }
    }

    public void lock(String eventChain) {
        if(this.unlocked.contains(eventChain))
        {
            this.unlocked.remove(eventChain);
            this.setChanged();
        }
    }

    private void setChanged() {
        this.holder.setData(ModAttachmentTypes.EVENT_UNLOCKS,this);
        if(this.isServer() && this.parent instanceof Player player)
            new SPacketSyncEventUnlocks(this.unlocked).sendTo(player);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        StringBuilder builder = new StringBuilder();
        for(String unlock : this.unlocked)
        {
            if(!builder.isEmpty())
                builder.append(';');
            builder.append(unlock);
        }
        tag.putString("Unlocked", builder.toString());
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider lookup, CompoundTag tag) {
        this.unlocked.clear();
        String unlocked = tag.getString("Unlocked");
        this.unlocked.addAll(Arrays.stream(unlocked.split(";")).filter(Predicate.not(String::isBlank)).toList());
    }


    public void sync(List<String> list) {
        this.unlocked.clear();
        this.unlocked.addAll(list);
        this.setChanged();
    }

}
