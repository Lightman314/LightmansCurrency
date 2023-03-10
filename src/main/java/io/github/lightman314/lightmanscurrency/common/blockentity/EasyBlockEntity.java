package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class EasyBlockEntity extends TileEntity implements IClientTracker {

    protected EasyBlockEntity(TileEntityType<?> type) { super(type); }

    @Override
    public boolean isClient() { return this.level == null || this.level.isClientSide; }

    @Override
    public void onLoad() {
        if(this.isClient())
            BlockEntityUtil.requestUpdatePacket(this);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.worldPosition, 99, this.getUpdateTag());
    }

    @Override
    public @Nonnull CompoundNBT getUpdateTag() { return this.save(new CompoundNBT()); }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.load(this.getBlockState(), pkt.getTag());
    }
}