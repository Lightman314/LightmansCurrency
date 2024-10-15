package io.github.lightman314.lightmanscurrency.api.ejection.builtin;

import io.github.lightman314.lightmanscurrency.api.ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.api.ejection.EjectionDataType;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.common.menus.containers.NonEmptyContainer;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class BasicEjectionData extends EjectionData {

    public static final EjectionDataType TYPE = new Type();

    private final Component name;
    private final Container contents;
    private final OwnerData owner;
    public BasicEjectionData(@Nonnull OwnerData owner, @Nonnull List<ItemStack> contents, @Nonnull Component name) { this(owner,InventoryUtil.buildInventory(contents),name); }
    public BasicEjectionData(@Nonnull OwnerData owner, @Nonnull Container contents, @Nonnull Component name) {
        this.name = name;
        this.contents = new NonEmptyContainer(contents);
        this.owner = new OwnerData(this);
        this.owner.copyFrom(owner);
    }

    @Nonnull
    @Override
    public OwnerData getOwner() { return this.owner; }

    @Nonnull
    @Override
    public Component getName() { return this.name; }

    @Nonnull
    @Override
    public EjectionDataType getType() { return TYPE; }

    @Nonnull
    @Override
    public Container getContents() { return this.contents; }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) {
        tag.put("Owner", this.owner.save(lookup));
        tag.putString("Name",Component.Serializer.toJson(this.name,lookup));
        tag.putInt("Size", this.contents.getContainerSize());
        InventoryUtil.saveAllItems("Contents",tag,this.contents,lookup);
    }

    private static class Type extends EjectionDataType
    {
        @Nonnull
        @Override
        public EjectionData load(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider lookup) {
            OwnerData owner = new OwnerData(IClientTracker.forClient());
            owner.load(tag,lookup);
            Component name = Component.Serializer.fromJson(tag.getString("Name"),lookup);
            Container contents = InventoryUtil.loadAllItems("Contents",tag,tag.getInt("Size"),lookup);
            return new BasicEjectionData(owner,contents,name);
        }
    }

}
