package io.github.lightman314.lightmanscurrency.common.capability.event_unlocks;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.capability.CurrencyCapabilities;
import io.github.lightman314.lightmanscurrency.network.message.event.SPacketSyncEventUnlocks;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class CapabilityEventUnlocks {

    @Nullable
    public static IEventUnlocks getCapability(@Nonnull Player player)
    {
        LazyOptional<IEventUnlocks> optional = player.getCapability(CurrencyCapabilities.EVENT_TRACKER);
        if(optional.isPresent())
            return optional.orElseThrow(() -> new RuntimeException("Unexpected error occurred!"));
        return null;
    }

    public static boolean isUnlocked(@Nonnull Player player, @Nonnull String eventChain)
    {
        IEventUnlocks unlocks = getCapability(player);
        return unlocks != null && unlocks.isUnlocked(eventChain);
    }

    public static void unlock(@Nonnull Player player, @Nonnull String eventChain)
    {
        IEventUnlocks unlocks = getCapability(player);
        if(unlocks != null)
            unlocks.unlock(eventChain);
    }

    public static void lock(@Nonnull Player player, @Nonnull String eventChain)
    {
        IEventUnlocks unlocks = getCapability(player);
        if(unlocks != null)
            unlocks.lock(eventChain);
    }

    public static ICapabilityProvider createProvider(final Player entity) { return new Provider(entity); }

    public static class EventUnlocks implements IEventUnlocks
    {

        final Player player;
        private EventUnlocks(@Nonnull Player entity) { this.player = entity; }

        private final List<String> unlocked = new ArrayList<>();

        @Nonnull
        @Override
        public List<String> getUnlockedList() { return ImmutableList.copyOf(this.unlocked); }

        @Override
        public boolean isUnlocked(@Nonnull String eventChain) { return this.unlocked.contains(eventChain); }

        @Override
        public void unlock(@Nonnull String eventChain) {
            if(!this.unlocked.contains(eventChain))
            {
                this.unlocked.add(eventChain);
                this.onChange();
            }
        }

        @Override
        public void lock(@Nonnull String eventChain) {
            if(this.unlocked.contains(eventChain))
            {
                this.unlocked.remove(eventChain);
                this.onChange();
            }
         }

         private void onChange()
         {
             if(!this.player.level().isClientSide)
             {
                 //If changed on the server, send sync packet to the player
                 new SPacketSyncEventUnlocks(this.unlocked).sendTo(this.player);
             }
         }

        @Override
        public CompoundTag save() {
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
        public void load(CompoundTag tag) {
            this.unlocked.clear();
            String unlocked = tag.getString("Unlocked");
            this.unlocked.addAll(Arrays.stream(unlocked.split(";")).filter(Predicate.not(String::isBlank)).toList());
        }

        @Override
        public void sync(@Nonnull List<String> list) {
            this.unlocked.clear();
            this.unlocked.addAll(list);
        }

    }

    private static class Provider implements ICapabilitySerializable<Tag> {
        final LazyOptional<IEventUnlocks> optional;
        final IEventUnlocks handler;
        Provider(final Player player)
        {
            this.handler = new EventUnlocks(player);
            this.optional = LazyOptional.of(() -> this.handler);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nullable Capability<T> capability, Direction facing) {
            return CurrencyCapabilities.EVENT_TRACKER.orEmpty(capability, this.optional);
        }

        @Override
        public Tag serializeNBT() { return this.handler.save(); }

        @Override
        public void deserializeNBT(Tag tag) {
            if(tag instanceof CompoundTag compound)
                this.handler.load(compound);
        }

    }

}
