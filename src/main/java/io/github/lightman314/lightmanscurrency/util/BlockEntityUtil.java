package io.github.lightman314.lightmanscurrency.util;

import java.util.List;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.MessageRequestNBT;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

public class BlockEntityUtil
{
    /**
     * Sends an update packet to clients tracking a tile entity.
     *
     * @param tileEntity the tile entity to update
     */
    public static void sendUpdatePacket(BlockEntity tileEntity)
    {
    	ClientboundBlockEntityDataPacket packet = ClientboundBlockEntityDataPacket.create(tileEntity);
        if(packet != null)
        {
            sendUpdatePacket(tileEntity.getLevel(), tileEntity.getBlockPos(), packet);
        }
        else
        {
        	LightmansCurrency.LogError(tileEntity.getClass().getName() + ".getUpdatePacket() returned null!");
        }
    }

    /**
     * Sends an update packet to clients tracking a tile entity with a specific CompoundNBT
     *
     * @param tileEntity the tile entity to update
     */
    public static void sendUpdatePacket(BlockEntity tileEntity, CompoundTag compound)
    {
    	ClientboundBlockEntityDataPacket packet = ClientboundBlockEntityDataPacket.create(tileEntity, be -> compound);
        sendUpdatePacket(tileEntity.getLevel(), tileEntity.getBlockPos(), packet);
    }

    private static void sendUpdatePacket(Level world, BlockPos pos, ClientboundBlockEntityDataPacket packet)
    {
        if(world instanceof ServerLevel server)
        {
        	//CurrencyMod.LOGGER.info("Sending Tile Entity Update Packet to the connected clients.");
            List<ServerPlayer> players = server.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false);
            players.forEach(player -> player.connection.send(packet));
        }
        else
        {
        	LightmansCurrency.LogWarning("Cannot send Tile Entity Update Packet from a client.");
        }
    }
    
    public static void requestUpdatePacket(BlockEntity be) {
    	requestUpdatePacket(be.getLevel(), be.getBlockPos());
    }
    
    public static void requestUpdatePacket(Level level, BlockPos pos)
    {
    	if(level.isClientSide)
    		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageRequestNBT(pos));
    }
    
}
