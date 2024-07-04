package io.github.lightman314.lightmanscurrency.network.packet;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class ServerToClientPacket extends CustomPacket {

    protected ServerToClientPacket(@Nonnull Type<?> type) { super(type); }

    public final void sendTo(@Nonnull Player player) { if(player instanceof ServerPlayer sp) this.sendTo(sp); }
    public final void sendTo(@Nonnull ServerPlayer player) { PacketDistributor.sendToPlayer(player, this); }
    public final void sendTo(@Nonnull List<ServerPlayer> players) {
        for(ServerPlayer player : players)
            PacketDistributor.sendToPlayer(player, this);
    }
    public final void sendToPlayersNear(@Nonnull ServerLevel level, @Nullable ServerPlayer excluded, double x, double y, double z, double radius) {
        PacketDistributor.sendToPlayersNear(level,excluded,x,y,z,radius,this);
    }
    public final void sendToPlayersInDimension(@Nonnull ServerLevel level) { PacketDistributor.sendToPlayersInDimension(level, this); }
    public final void sendToPlayersTrackingChunk(@Nonnull ServerLevel level, @Nonnull ChunkPos chunk) { PacketDistributor.sendToPlayersTrackingChunk(level, chunk, this); }
    public final void sendToPlayersTrackingEntity(@Nonnull Entity entity) { PacketDistributor.sendToPlayersTrackingEntity(entity, this); }
    public final void sendToPlayersTrackingEntityAndSelf(@Nonnull Entity entity) { PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, this); }

    public final void sendToAll() { PacketDistributor.sendToAllPlayers(this); }

}
