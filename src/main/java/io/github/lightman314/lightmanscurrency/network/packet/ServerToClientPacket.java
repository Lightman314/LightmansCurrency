package io.github.lightman314.lightmanscurrency.network.packet;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class ServerToClientPacket extends CustomPacket {


    public final void sendTo(@Nonnull Player player) { if(player instanceof ServerPlayer sp) this.sendTo(sp); }
    public final void sendTo(@Nonnull ServerPlayer player) { this.sendTo(ImmutableList.of(player)); }
    public final void sendTo(@Nonnull List<ServerPlayer> players) { this.sendToTargets(players.stream().map(LightmansCurrencyPacketHandler::getTarget).toList()); }

    public final void sendToAll() { this.sendToTarget(PacketDistributor.ALL.noArg()); }

    public final void sendToTarget(@Nonnull PacketDistributor.PacketTarget target) { this.sendToTargets(ImmutableList.of(target));}

    public final void sendToTargets(@Nonnull List<PacketDistributor.PacketTarget> targets)
    {
        for(PacketDistributor.PacketTarget target : targets)
            LightmansCurrencyPacketHandler.instance.send(this, target);
    }

    public static class Simple extends ServerToClientPacket {
        @Override
        public final void encode(@Nonnull FriendlyByteBuf buffer) {}
    }

}
