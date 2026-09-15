package io.github.lightman314.lightmanscurrency.network.message.system;

import io.github.lightman314.lightmanscurrency.api.proxy.LCProxy;
import io.github.lightman314.lightmanscurrency.network.packet.ServerToClientPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class SPacketSyncTime extends ServerToClientPacket {

    public static final Type<SPacketSyncTime> TYPE = sType("sync_time");
    public static final StreamCodec<ByteBuf,SPacketSyncTime> STREAM_CODEC = ByteBufCodecs.LONG.map(SPacketSyncTime::new,p -> p.timeMillis);

    private final long timeMillis;
    private SPacketSyncTime(long timeMillis) { super(TYPE); this.timeMillis = timeMillis; }

    public static void sendToPlayer(Player player) { new SPacketSyncTime(System.currentTimeMillis()).sendTo(player); }

    @Override
    protected void handle(IPayloadContext context, Player player) {
        //Calculate the difference in time between the server and the client
        long diff = (this.timeMillis - System.currentTimeMillis());
        long partialSecond = diff % 1000;
        //Round offset to the nearest second
        //Assume anything less than 1s is simply ping
        if(partialSecond >= 500)
            diff += 1000;
        diff -= partialSecond;
        LCProxy.get().setTimeDesync(diff);
    }

}