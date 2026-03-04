package io.github.lightman314.lightmanscurrency.api.misc.blockentity;

import io.github.lightman314.lightmanscurrency.api.misc.ticker.IServerTicker;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public abstract class EasySyncingBlockEntity extends EasyBlockEntity implements IServerTicker {

    public EasySyncingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    private LazyPacketData.Builder changedData;

    public final void setChanged(Consumer<LazyPacketData.Builder> dataWriter) {
        this.setChanged();
        if(this.changedData != null)
            this.changedData = this.builder();
        dataWriter.accept(this.changedData);
    }

    @Override
    public void handleMessage(Player player, LazyPacketData message) {
        super.handleMessage(player, message);
        if(message.contains("DataSync") && this.isClient())
            this.handleSyncPacket(message.getMap("DataSync"));
    }

    @Override
    public final void serverTick() {
        this.serverTickInternal();
        if(this.changedData != null)
        {
            LazyPacketData.Builder packet = this.changedData;
            this.changedData = null;
            this.sendPacket(this.builder().setMap("DataSync",packet));
        }
    }

    protected void serverTickInternal() {}

    protected abstract void handleSyncPacket(LazyPacketData data);


}
