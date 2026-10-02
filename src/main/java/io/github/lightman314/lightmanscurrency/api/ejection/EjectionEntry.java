package io.github.lightman314.lightmanscurrency.api.ejection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.holder.OwnerHolder;
import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnerHolder;
import io.github.lightman314.lightmanscurrency.features.api_impl.data.EjectionDataCache;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

public final class EjectionEntry implements ISidedContext.Mutable<EjectionEntry>, IOwnerHolder {

    public static final Codec<EjectionEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            OwnerHolder.CODEC.fieldOf("owner").forGetter(e -> e.owner),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("items").forGetter(e -> e.contents)
    ).apply(builder,EjectionEntry::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,EjectionEntry> STREAM_CODEC = StreamCodec.composite(
            OwnerHolder.STREAM_CODEC,e -> e.owner,
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()),e -> e.contents,
            EjectionEntry::new);

    private long id = -1;
    public long getID() { return this.id; }
    @ApiStatus.Internal
    public void setID(long id) {
        if(this.id >= 0)
            throw new IllegalStateException("The ID of this entry is already assigned!");
        this.id = id;
    }
    private ISidedContext context = ISidedContext.LOGICAL_CLIENT;
    private final OwnerHolder owner = new OwnerHolder(this);
    private final List<ItemStack> contents;
    private EjectionEntry(long id,OwnerHolder owner,List<ItemStack> contents) {
        this.id = id;
        this.owner.copyFrom(owner);
        this.contents = new ArrayList<>(contents);
    }
    public EjectionEntry(IOwnerHolder owner, List<ItemStack> contents) {
        this.owner.unsafeCopyFrom(owner);
        this.contents = new ArrayList<>(contents);
    }
    public EjectionEntry(Owner owner,List<ItemStack> contents) {
        this.owner.setOwner(owner);
        this.contents = contents;
    }

    public void setChanged() {
        //Remove empty contents
        this.contents.removeIf(ItemStack::isEmpty);
        //Tell the data cache that the data has been changed
        EjectionDataCache.TYPE.get(this).setChanged(this.id);
    }

    public List<ItemStack> getContents() { return this.contents; }
    /**
     * Used by {@link EjectionContainer} to revert content changes from a transaction snapshot
     */
    @ApiStatus.Internal
    public void updateContents(List<ItemStack> contents) {
        this.contents.clear();
        this.contents.addAll(ItemHelper.copyList(contents));
    }

    public boolean isEmpty() { return !this.contents.stream().allMatch(ItemStack::isEmpty); }

    @Override
    public Owner getValidOwner() { return this.owner.getValidOwner(); }

    @Override
    public boolean isClient() { return this.context.isClient(); }

    @Override
    public EjectionEntry setSidedContext(ISidedContext context) { this.context = context; return this; }

}