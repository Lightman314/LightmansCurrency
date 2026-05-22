package io.github.lightman314.lightmanscurrency.common.attachments;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.common.core.ModAttachmentTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class EventUnlocks extends EasyAttachment<EventUnlocks>
{

    public static final Codec<EventUnlocks> CODEC = Codec.withAlternative(
            CodecHelper.setCodec(Codec.STRING).xmap(EventUnlocks::new,EventUnlocks::getUnlockedList),
            Codec.STRING.xmap(EventUnlocks::parseOld,e -> "").fieldOf("Unlocked").codec());
    public static final StreamCodec<ByteBuf,EventUnlocks> STREAM_CODEC = StreamHelper.setCodec(ByteBufCodecs.STRING_UTF8).map(EventUnlocks::new, EventUnlocks::getUnlockedList);
    public static final UnaryOperator<EventUnlocks> COPIER = data -> {
        EventUnlocks e = new EventUnlocks();
        e.unlocked.addAll(data.unlocked);
        return e;
    };

    @Override
    protected AttachmentType<EventUnlocks> getType() { return ModAttachmentTypes.EVENT_UNLOCKS.get(); }

    private final Set<String> unlocked = new HashSet<>();
    public Set<String> getUnlockedList() { return ImmutableSet.copyOf(this.unlocked); }

    public EventUnlocks() {}
    private EventUnlocks(Collection<String> unlocked) { this.unlocked.addAll(unlocked); }

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

    private static EventUnlocks parseOld(String string)
    {
        EventUnlocks e = new EventUnlocks();
        e.unlocked.addAll(Arrays.stream(string.split(";")).filter(Predicate.not(String::isBlank)).toList());
        return e;
    }

    public void sync(List<String> list) {
        this.unlocked.clear();
        this.unlocked.addAll(list);
        this.setChanged();
    }

}
