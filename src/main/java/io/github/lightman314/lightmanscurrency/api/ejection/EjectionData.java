package io.github.lightman314.lightmanscurrency.api.ejection;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.partial.SPart1;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.ISidedObject;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.common.data.types.EjectionDataCache;
import io.github.lightman314.lightmanscurrency.common.notifications.types.ejection.OwnableBlockEjectedNotification;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;

public abstract class EjectionData implements ISidedObject {

    public static final Codec<EjectionData> CODEC = LCRegistries.EJECTION_DATA.byNameCodec()
            .dispatch(EjectionData::getType,EjectionDataType::mapCodec);
    public static final StreamCodec<RegistryFriendlyByteBuf,EjectionData> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.EJECTION_DATA_KEY)
            .dispatch(EjectionData::getType,EjectionDataType::streamCodec);

    private boolean isClient = false;
    @Override
    public boolean isClient() { return this.isClient; }
    
    @Override
    public EjectionData flagAsClient() { return this.flagAsClient(true); }
    
    @Override
    public EjectionData flagAsClient(boolean isClient) { this.isClient = isClient; return this; }
    
    @Override
    public EjectionData flagAsClient(IClientTracker tracker) { return this.flagAsClient(tracker.isClient()); }

    public EjectionData() { }
    protected EjectionData(long id) { this.id = id; }

    private long id = -1;
    public long id() { return this.id; }
    public void setID(long newID)
    {
        if(this.id >= 0)
            return;
        this.id = newID;
    }

    public abstract OwnerData getOwner();

    public final boolean canAccess(Player player) { return this.getOwner().isMember(player); }

    public abstract Component getName();

    public abstract EjectionDataType<?> getType();
    
    public abstract IItemHandlerModifiable getContents();

    public boolean isEmpty() { return ItemHandlerUtil.isEmpty(this.getContents()); }

    public void setChanged() {
        if(this.isServer())
            EjectionDataCache.TYPE.get(this).markEjectionDataDirty(this.id);
        this.onChanged();
    }

    protected void onChanged() {}

    public final CompoundTag save(DataContext<Tag> context) {
        return (CompoundTag)CODEC.encodeStart(context.ops(),this).getOrThrow();
    }

    public static EjectionData load(CompoundTag tag, DataContext<Tag> context) {
        return CODEC.decode(context.ops(),tag).getOrThrow().getFirst();
    }

    public boolean canSplit() { return false; }

    public IconData getSplitButtonIcon() { return IconUtil.ICON_X; }

    public List<Component> getSplitButtonTooltip() { return Lists.newArrayList(LCText.TOOLTIP_EJECTION_SPLIT_GENERIC.get()); }

    public void splitContents() { if(this.canSplit()) throw new NotImplementedException("Ejection Data flags itself as being able to be split, but does not override the split method!"); }

    public final void pushNotificationToOwner() {
        Owner owner = this.getOwner().getValidOwner();
        if(owner != null)
            owner.pushNotification(OwnableBlockEjectedNotification.create(this.getName()), 1, true);
    }

    public static <T extends EjectionData> App<RecordCodecBuilder.Mu<T>,Long> baseFields() {
        return Codec.LONG.fieldOf("id").forGetter(EjectionData::id);
    }

    public static <T extends EjectionData> SPart1<RegistryFriendlyByteBuf,T,Long> baseStreamFields() {
        return new SPart1<>(ByteBufCodecs.VAR_LONG,EjectionData::id);
    }

}
