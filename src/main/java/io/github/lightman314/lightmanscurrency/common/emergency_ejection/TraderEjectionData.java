package io.github.lightman314.lightmanscurrency.common.emergency_ejection;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionDataType;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.NonEmptyHandler;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderState;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.util.OldDataHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TraderEjectionData extends EjectionData {

    public static final EjectionDataType<?> TYPE = new Type();

    private static final Codec<IData> DATA_CODEC = Codec.INT.dispatch(IData::getID,IData::getCodec);
    private static final MapCodec<TraderEjectionData> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            DATA_CODEC.fieldOf("data").forGetter(d -> d.data),
            baseFields()
    ).apply(builder,TraderEjectionData::new));

    private static final StreamCodec<RegistryFriendlyByteBuf,IData> DATA_STREAM_CODEC = StreamHelper.mapBufReg(ByteBufCodecs.INT)
            .dispatch(IData::getID,IData::getStreamCodec);
    private static final StreamCodec<RegistryFriendlyByteBuf,TraderEjectionData> STREAM_CODEC = StreamHelper.combine(baseStreamFields(),
            DATA_STREAM_CODEC,d -> d.data,
            TraderEjectionData::new);

    private IData data;

    public TraderEjectionData(long traderID, ItemStack item) { this.data = new PreSplitData(traderID,item); }
    private TraderEjectionData(IData data,long id) { super(id); this.data = data; }

    @Override
    public OwnerData getOwner() { return this.data.getOwner(this); }

    @Override
    public Component getName() { return this.data.getName(this); }

    @Override
    public EjectionDataType<?> getType() { return TYPE; }

    @Override
    public IItemHandlerModifiable getContents() { return this.data.getContents(); }

    @Override
    public boolean isEmpty() { return this.data.isEmpty(this) || super.isEmpty(); }

    @Override
    public boolean canSplit() { return this.data.isPreSplit(); }

    public long getTraderID()
    {
        if(this.data instanceof PreSplitData d)
            return d.traderID;
        return -1;
    }

    public void delete() {
        this.data = EmptyData.INSTANCE;
        this.setChanged();
    }
    
    @Override
    public List<Component> getSplitButtonTooltip() {
        if(this.data instanceof PreSplitData psd)
            return Lists.newArrayList(LCText.TOOLTIP_EJECTION_SPLIT_TRADER.get(psd.item.getHoverName()));
        return super.getSplitButtonTooltip();
    }
    
    @Override
    public IconData getSplitButtonIcon() {
        if(this.data instanceof PreSplitData psd)
            return ItemIcon.ofItem(psd.item);
        return super.getSplitButtonIcon();
    }
    @Override
    public void splitContents() {
        if(this.data instanceof PreSplitData psd && !psd.contents.isEmpty())
        {
            LightmansCurrency.LogDebug("Splitting Trader Data!");
            //Block Splitting if the trader has already been deleted and/or already recovered
            TraderData trader = psd.getTrader(this);
            if(trader == null || trader.getState() != TraderState.EJECTED)
                return;
            //Get "Block Item"
            ItemStack item = psd.item.copy();
            //Remove Trader ID so that it no longer links back to the trader
            item.remove(ModDataComponents.TRADER_ITEM_DATA);
            List<ItemStack> contents = trader.getContents(item);
            //Copy ownership
            OwnerData owner = new OwnerData(IClientTracker.forClient());
            owner.copyFrom(trader.getOwner());
            //Load post-split data
            this.data = new SplitData(owner,contents,trader.getName());
            //Delete the Trader Data as there is no longer an item linked to it,
            //and we can safely assume the ejection system is working properly at this point
            if(this.isServer())
                TraderAPI.getApi().DeleteTrader(trader);
        }
    }

    private static class Type extends EjectionDataType<TraderEjectionData>
    {
        
        @Override
        public EjectionData loadOldData(CompoundTag tag, HolderLookup.Provider lookup, long id) {
            if(tag.getBoolean("Empty"))
                return new TraderEjectionData(EmptyData.INSTANCE,id);
            boolean split = tag.getBoolean("Split");
            IData data;
            if(split)
                data = SplitData.loadOldData(tag,lookup);
            else
                data = PreSplitData.loadOldData(tag,lookup);
            return new TraderEjectionData(data,id);
        }

        @Override
        public MapCodec<TraderEjectionData> mapCodec() { return MAP_CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf,TraderEjectionData> streamCodec() { return STREAM_CODEC; }
    }

    private interface IData
    {

        private static MapCodec<? extends IData> getCodec(int id) {
            return switch (id) {
                case 1 -> PreSplitData.MAP_CODEC;
                case 2 -> SplitData.MAP_CODEC;
                default -> EmptyData.MAP_CODEC;
            };
        }
        private static StreamCodec<RegistryFriendlyByteBuf,? extends IData> getStreamCodec(int id)
        {
            return switch (id) {
                case 1 -> PreSplitData.STREAM_CODEC;
                case 2 -> SplitData.STREAM_CODEC;
                default -> EmptyData.STREAM_CODEC;
            };
        }

        default boolean isPreSplit() { return this instanceof PreSplitData; }
        default boolean isSplit() { return this instanceof SplitData; }
        int getID();
        
        OwnerData getOwner(IClientTracker context);
        
        Component getName(IClientTracker context);
        
        IItemHandlerModifiable getContents();
        boolean isEmpty(IClientTracker context);

    }

    private static class PreSplitData implements IData
    {
        private static final MapCodec<PreSplitData> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.LONG.fieldOf("id").forGetter(d -> d.traderID),
                ItemStack.CODEC.fieldOf("item").forGetter(d -> d.item)
        ).apply(builder,PreSplitData::new));
        private static final StreamCodec<RegistryFriendlyByteBuf,PreSplitData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_LONG,d -> d.traderID,
                ItemStack.STREAM_CODEC,d -> d.item,
                PreSplitData::new);

        private final long traderID;
        private final ItemStack item;
        private final LCItemStackHandler contents;
        private PreSplitData(long traderID, ItemStack item)
        {
            this.traderID = traderID;
            this.item = item.copy();
            this.contents = new LCItemStackHandler(this.item);
        }

        private TraderData getTrader(IClientTracker context) { return TraderAPI.getApi().GetTrader(context,this.traderID); }

        @Override
        public int getID() { return 1; }

        public OwnerData getOwner(IClientTracker context) {
            TraderData trader = this.getTrader(context);
            return trader == null ? new OwnerData(context) : trader.getOwner();
        }
        
        public Component getName(IClientTracker context) {
            TraderData trader = this.getTrader(context);
            return trader == null ? LCText.GUI_TRADER_DEFAULT_NAME.get() : trader.getName();
        }
        
        @Override
        public IItemHandlerModifiable getContents() { return this.contents; }
        @Override
        public boolean isEmpty(IClientTracker context) {
            TraderData trader = this.getTrader(context);
            return trader == null || trader.getState() != TraderState.EJECTED;
        }

        @SuppressWarnings("deprecation")
        private static PreSplitData loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
            long traderID = tag.getLong("TraderID");
            ItemStack item = OldDataHelper.loadItem(tag.getCompound("Item"),lookup);
            return new PreSplitData(traderID,item);
        }
    }

    private static class SplitData implements IData
    {

        private static final MapCodec<SplitData> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                OwnerData.CODEC.fieldOf("owner").forGetter(d -> d.tempOwner),
                NonEmptyHandler.CODEC.fieldOf("contents").forGetter(d -> d.contents),
                ComponentSerialization.CODEC.fieldOf("name").forGetter(d -> d.name)
        ).apply(builder,SplitData::new));
        private static final StreamCodec<RegistryFriendlyByteBuf,SplitData> STREAM_CODEC = StreamCodec.composite(
                OwnerData.STREAM_CODEC,d -> d.tempOwner,
                NonEmptyHandler.STREAM_CODEC,d -> d.contents,
                ComponentSerialization.STREAM_CODEC,d -> d.name,
                SplitData::new);

        private final OwnerData tempOwner;
        @Nullable
        private OwnerData owner;
        private final NonEmptyHandler contents;
        private List<ItemStack> getItems() { return this.contents.getStacks(); }
        private final Component name;
        private SplitData(OwnerData owner,List<ItemStack> contents,Component name) {
            this(owner,new NonEmptyHandler(contents),name);
        }
        private SplitData(OwnerData owner, NonEmptyHandler contents, Component name)
        {
            this.tempOwner = owner;
            this.contents = contents;
            this.name = name;
        }

        @Override
        public int getID() { return 2; }

        @Override
        public OwnerData getOwner(IClientTracker context) {
            if(this.owner == null)
            {
                this.owner = new OwnerData(context);
                this.owner.copyFrom(this.tempOwner);
            }
            return this.owner;
        }
        
        @Override
        public Component getName(IClientTracker context) { return this.name; }
        
        @Override
        public IItemHandlerModifiable getContents() { return this.contents; }
        @Override
        public boolean isEmpty(IClientTracker context) { return false; }

        @SuppressWarnings("deprecation")
        public static SplitData loadOldData(CompoundTag tag, HolderLookup.Provider lookup)
        {
            OwnerData owner = new OwnerData(IClientTracker.forClient());
            owner.load(tag.getCompound("Owner"),DataContext.createNBT(lookup));
            NonEmptyHandler contents = new NonEmptyHandler(OldDataHelper.loadNonEmptyList(tag.getList("Contents",Tag.TAG_COMPOUND),lookup));
            Component name = Component.Serializer.fromJson(tag.getString("Name"),lookup);
            return new SplitData(owner,contents,name);
        }
    }

    private static class EmptyData implements IData
    {

        private static final EmptyData INSTANCE = new EmptyData();

        private static final MapCodec<EmptyData> MAP_CODEC = MapCodec.unit(INSTANCE);
        private static final StreamCodec<RegistryFriendlyByteBuf,EmptyData> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public int getID() { return 0; }
        @Override
        public OwnerData getOwner(IClientTracker context) { return new OwnerData(context); }
        @Override
        public Component getName(IClientTracker context) { return EasyText.literal("Null"); }
        @Override
        public IItemHandlerModifiable getContents() { return new NonEmptyHandler(new ArrayList<>()); }
        @Override
        public boolean isEmpty(IClientTracker context) { return true; }
    }

}
