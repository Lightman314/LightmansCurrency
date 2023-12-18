package io.github.lightman314.lightmanscurrency.common.capability.event_unlocks;

import net.minecraft.nbt.CompoundTag;

import javax.annotation.Nonnull;
import java.util.List;

public interface IEventUnlocks {

    @Nonnull
    List<String> getUnlockedList();
    boolean isUnlocked(@Nonnull String eventChain);
    void unlock(@Nonnull String eventChain);
    void lock(@Nonnull String eventChain);

    /**
     * Save the nbt data to file
     */
    CompoundTag save();

    /**
     * Load the nbt data from file
     */
    void load(CompoundTag tag);

    void sync(@Nonnull List<String> list);

}
