package io.github.lightman314.lightmanscurrency.common.attachments;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.core.ModAttachmentTypes;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class EventUnlocks implements IClientTracker
{

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

    private void setChanged() { this.holder.setData(ModAttachmentTypes.EVENT_UNLOCKS,this); }

    private ListTag write() { return TagUtil.writeStringList(this.unlocked); }
    private void read(ListTag tag) {
        this.unlocked.clear();
        this.unlocked.addAll(TagUtil.loadStringList(tag));
    }
    private void read(CompoundTag tag)
    {
        this.unlocked.clear();
        String unlocked = tag.getString("Unlocked");
        this.unlocked.addAll(Arrays.stream(unlocked.split(";")).filter(Predicate.not(String::isBlank)).toList());
    }

    public void sync(List<String> list) {
        this.unlocked.clear();
        this.unlocked.addAll(list);
        this.setChanged();
    }

    public static AttachmentType.Builder<EventUnlocks> buildType() {
        return AttachmentType.builder(EventUnlocks::new)
                .serialize(new Serializer())
                .sync(new Syncer())
                .copyHandler(new Copier())
                .copyOnDeath();
    }

    private static class Serializer implements IAttachmentSerializer<Tag,EventUnlocks>
    {
        @Override
        public EventUnlocks read(IAttachmentHolder holder, Tag tag, HolderLookup.Provider provider) {
            EventUnlocks result = new EventUnlocks(holder);
            if(tag instanceof ListTag list)
                result.read(list);
            else if(tag instanceof CompoundTag t)
                result.read(t);
            return result;
        }
        @Override
        public @Nullable Tag write(EventUnlocks attachment, HolderLookup.Provider provider) { return attachment.write(); }
    }

    private static class Syncer implements AttachmentSyncHandler<EventUnlocks>
    {
        private final StreamCodec<ByteBuf,List<String>> codec = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list());
        @Override
        public void write(RegistryFriendlyByteBuf buf, EventUnlocks attachment, boolean initialSync) {
            this.codec.encode(buf,attachment.unlocked);
        }

        @Override
        public @Nullable EventUnlocks read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable EventUnlocks previousValue) {
            EventUnlocks eu = Objects.requireNonNullElseGet(previousValue,() -> new EventUnlocks(holder));
            eu.unlocked.clear();
            eu.unlocked.addAll(this.codec.decode(buf));
            return eu;
        }
    }

    private static class Copier implements IAttachmentCopyHandler<EventUnlocks>
    {
        @Override
        @Nullable
        public EventUnlocks copy(EventUnlocks attachment, IAttachmentHolder holder, HolderLookup.Provider provider) {
            EventUnlocks newUnlocks = new EventUnlocks(holder);
            newUnlocks.unlocked.addAll(attachment.unlocked);
            return newUnlocks;
        }
    }

}
