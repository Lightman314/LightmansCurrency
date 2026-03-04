package io.github.lightman314.lightmanscurrency.api.ejection.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionDataType;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.NonEmptyHandler;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.List;

public class BasicEjectionData extends EjectionData {

    public static final EjectionDataType<BasicEjectionData> TYPE = new Type();
    private static final MapCodec<BasicEjectionData> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            OwnerData.CODEC.fieldOf("owner").forGetter(n -> n.owner),
            NonEmptyHandler.CODEC.fieldOf("contents").forGetter(n -> n.contents),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(n -> n.name),
            baseFields()
    ).apply(builder,BasicEjectionData::new));
    private static final StreamCodec<RegistryFriendlyByteBuf,BasicEjectionData> STREAM_CODEC = StreamHelper.combine(
            baseStreamFields(),
            OwnerData.STREAM_CODEC,n -> n.owner,
            NonEmptyHandler.STREAM_CODEC,n -> n.contents,
            ComponentSerialization.STREAM_CODEC,n -> n.name,
            BasicEjectionData::new);

    private final Component name;
    private final NonEmptyHandler contents;
    private final OwnerData owner;
    public BasicEjectionData(OwnerData owner, IItemHandler contents, Component name) { this(owner, ItemHandlerUtil.toList(contents),name); }
    public BasicEjectionData(OwnerData owner,List<ItemStack> contents,Component name) {
        this.name = name;
        this.contents = new NonEmptyHandler(contents);
        this.owner = new OwnerData(this);
        this.owner.copyFrom(owner);
    }
    private BasicEjectionData(OwnerData owner,NonEmptyHandler contents,Component name,long id) {
        super(id);
        this.name = name;
        this.contents = contents;
        this.owner = new OwnerData(this);
        this.owner.copyFrom(owner);
    }

    @Override
    public OwnerData getOwner() { return this.owner; }
    @Override
    public Component getName() { return this.name; }
    @Override
    public EjectionDataType<BasicEjectionData> getType() { return TYPE; }
    
    @Override
    public IItemHandlerModifiable getContents() { return this.contents; }

    private static class Type extends EjectionDataType<BasicEjectionData>
    {
        @Override
        public MapCodec<BasicEjectionData> mapCodec() { return MAP_CODEC; }
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, BasicEjectionData> streamCodec() { return STREAM_CODEC; }

        @Override
        @SuppressWarnings("deprecation")
        public EjectionData loadOldData(CompoundTag tag, HolderLookup.Provider lookup,long id) {
            OwnerData owner = new OwnerData(IClientTracker.forClient());
            owner.load(tag,DataContext.createNBT(lookup));
            Component name = Component.Serializer.fromJson(tag.getString("Name"),lookup);
            Container container = InventoryUtil.loadAllItems("Contents",tag,tag.getInt("Size"),lookup);
            NonEmptyHandler contents = new NonEmptyHandler(InventoryUtil.buildList(container));
            return new BasicEjectionData(owner,contents,name);
        }
    }

}
