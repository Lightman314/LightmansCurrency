package io.github.lightman314.lightmanscurrency.common.emergency_ejection;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionDataType;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderState;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.menus.containers.NonEmptyContainer;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class TraderEjectionData extends EjectionData {

    public static final EjectionDataType TYPE = new Type();

    private IData data;

    public TraderEjectionData(long traderID, @Nonnull ItemStack item) { this.data = new PreSplitData(traderID,item); }
    private TraderEjectionData(@Nonnull IData data) { this.data = data; }

    @Nonnull
    @Override
    public OwnerData getOwner() { return this.data.getOwner(this); }

    @Nonnull
    @Override
    public Component getName() { return this.data.getName(this); }

    @Nonnull
    @Override
    public EjectionDataType getType() { return TYPE; }

    @Nonnull
    @Override
    public Container getContents() { return this.data.getContents(); }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) {
        tag.putBoolean("Split",this.data.isSplit());
        this.data.saveAdditional(tag,lookup);
    }

    @Override
    public boolean isEmpty() { return this.data.isEmpty(this) || super.isEmpty(); }

    @Override
    public boolean canSplit() { return this.data.isPreSplit(); }
    @Nonnull
    @Override
    public List<Component> getSplitButtonTooltip() {
        if(this.data instanceof PreSplitData psd)
            return Lists.newArrayList(LCText.TOOLTIP_EJECTION_SPLIT_TRADER.get(psd.item.getHoverName()));
        return super.getSplitButtonTooltip();
    }
    @Nonnull
    @Override
    public IconData getSplitButtonIcon() {
        if(this.data instanceof PreSplitData psd)
            return IconData.of(psd.item);
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
            Container contents = InventoryUtil.buildInventory(trader.getContents(item));
            //Copy ownership
            OwnerData owner = new OwnerData(IClientTracker.forClient());
            owner.copyFrom(trader.getOwner());
            //Load post-split data
            this.data = new SplitData(owner,new NonEmptyContainer(contents),trader.getName());
            //Delete the Trader Data as there is no longer an item linked to it,
            //and we can safely assume the ejection system is working properly at this point
            if(this.isServer())
                TraderAPI.API.DeleteTrader(trader);
        }
    }

    private static class Type extends EjectionDataType
    {
        @Nonnull
        @Override
        public EjectionData load(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) {
            boolean split = tag.getBoolean("Split");
            IData data;
            if(split)
                data = SplitData.load(tag,lookup);
            else
                data = PreSplitData.load(tag,lookup);
            return new TraderEjectionData(data);
        }
    }

    private interface IData
    {
        default boolean isPreSplit() { return this instanceof PreSplitData; }
        default boolean isSplit() { return this instanceof SplitData; }
        @Nonnull
        OwnerData getOwner(@Nonnull IClientTracker context);
        @Nonnull
        Component getName(@Nonnull IClientTracker context);
        @Nonnull
        Container getContents();
        boolean isEmpty(@Nonnull IClientTracker context);
        void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup);
    }

    private static class PreSplitData implements IData
    {
        private final long traderID;
        private final ItemStack item;
        private final Container contents;
        private PreSplitData(long traderID, @Nonnull ItemStack item)
        {
            this.traderID = traderID;
            this.item = item.copy();
            this.contents = InventoryUtil.buildInventory(this.item);
        }

        private TraderData getTrader(@Nonnull IClientTracker context) { return TraderAPI.API.GetTrader(context,this.traderID); }
        @Nonnull
        public OwnerData getOwner(@Nonnull IClientTracker context) {
            TraderData trader = this.getTrader(context);
            return trader == null ? new OwnerData(context) : trader.getOwner();
        }
        @Nonnull
        public Component getName(@Nonnull IClientTracker context) {
            TraderData trader = this.getTrader(context);
            return trader == null ? LCText.GUI_TRADER_DEFAULT_NAME.get() : trader.getName();
        }
        @Nonnull
        @Override
        public Container getContents() { return this.contents; }
        @Override
        public boolean isEmpty(@Nonnull IClientTracker context) {
            TraderData trader = this.getTrader(context);
            return trader == null || trader.getState() != TraderState.EJECTED;
        }

        @Override
        public void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) {
            tag.putLong("TraderID", this.traderID);
            tag.put("Item",InventoryUtil.saveItemNoLimits(this.item,lookup));
        }
        @Nonnull
        private static PreSplitData load(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) {
            long traderID = tag.getLong("TraderID");
            ItemStack item = InventoryUtil.loadItemNoLimits(tag.getCompound("Item"),lookup);
            return new PreSplitData(traderID,item);
        }
    }

    private static class SplitData implements IData
    {
        private final OwnerData tempOwner;
        private OwnerData owner;
        private final NonEmptyContainer contents;
        private final Component name;
        private SplitData(@Nonnull OwnerData owner, @Nonnull NonEmptyContainer container, @Nonnull Component name)
        {
            this.tempOwner = owner;
            this.contents = container;
            this.name = name;
        }
        @Nonnull
        @Override
        public OwnerData getOwner(@Nonnull IClientTracker context) {
            if(this.owner == null)
            {
                this.owner = new OwnerData(context);
                this.owner.copyFrom(this.tempOwner);
            }
            return this.owner;
        }
        @Nonnull
        @Override
        public Component getName(@Nonnull IClientTracker context) { return this.name; }
        @Nonnull
        @Override
        public Container getContents() { return this.contents; }
        @Override
        public boolean isEmpty(@Nonnull IClientTracker context) { return false; }

        @Override
        public void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) {
            OwnerData o = this.owner == null ? this.tempOwner : this.owner;
            tag.put("Owner",o.save(lookup));
            this.contents.save(tag,"Contents",lookup);
            tag.putString("Name",Component.Serializer.toJson(this.name,lookup));
        }
        @Nonnull
        public static SplitData load(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup)
        {
            OwnerData owner = new OwnerData(IClientTracker.forClient());
            owner.load(tag.getCompound("Owner"),lookup);
            NonEmptyContainer contents = NonEmptyContainer.load(tag,"Contents",lookup);
            Component name = Component.Serializer.fromJson(tag.getString("Name"),lookup);
            return new SplitData(owner,contents,name);
        }
    }

}
