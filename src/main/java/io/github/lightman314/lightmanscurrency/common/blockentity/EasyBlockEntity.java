package io.github.lightman314.lightmanscurrency.common.blockentity;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.util.BlockEntityUtil;
import net.minecraft.block.BlockState;
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
        this.loadAdditional(pkt.getTag());
    }

    @Nonnull
    @Override
    public final CompoundNBT save(@Nonnull CompoundNBT compound) {
        compound = super.save(compound);
        try{
            this.saveAdditional(compound);
        } catch(Throwable t) { LightmansCurrency.LogError("Error loading Block Entity from tag!", t); }
        return compound;
    }

    protected abstract void saveAdditional(@Nonnull CompoundNBT compound);

    @Override
    public final void load(@Nonnull BlockState state, @Nonnull CompoundNBT compound) {
        super.load(state, compound);
        try{ this.loadAdditional(compound);
        } catch(Throwable t) { LightmansCurrency.LogError("Error loading Block Entity from tag!", t); }
    }

    protected abstract void loadAdditional(@Nonnull CompoundNBT compound);
}