package io.github.lightman314.lightmanscurrency.common.emergency_ejection;

import io.github.lightman314.lightmanscurrency.api.ejection.builtin.BasicEjectionData;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OldEjectionDataHelper {

    @Nullable
    public static BasicEjectionData parseOldData(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup)
    {
        OwnerData owner = new OwnerData(IClientTracker.forClient());
        if(tag.contains("Owner"))
            owner.load(tag.getCompound("Owner"),lookup);
        Component name = Component.Serializer.fromJson(tag.getString("Name"),lookup);
        Container contents = InventoryUtil.loadAllItems("Items",tag,tag.getList("Items",Tag.TAG_COMPOUND).size(),lookup);
        return new BasicEjectionData(owner,contents,name);
    }

}
