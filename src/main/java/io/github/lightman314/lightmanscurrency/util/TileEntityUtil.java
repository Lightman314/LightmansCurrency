package io.github.lightman314.lightmanscurrency.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.stream.Stream;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.MessageRequestNBT;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

public class TileEntityUtil
{
    /**
     * Sends an update packet to clients tracking a tile entity.
     *
     * @param blockEntity the tile entity to update
     */
    public static void sendUpdatePacket(BlockEntity blockEntity)
    {
        ClientboundBlockEntityDataPacket packet = blockEntity.getUpdatePacket();
        if(packet != null)
        {
            sendUpdatePacket(blockEntity.getLevel(), blockEntity.getBlockPos(), packet);
        }
        else
        {
        	LightmansCurrency.LogError(blockEntity.getClass().getName() + ".getUpdatePacket() returned null!");
        }
    }

    /**
     * Sends an update packet to clients tracking a tile entity with a specific CompoundNBT
     *
     * @param tileEntity the tile entity to update
     */
    public static void sendUpdatePacket(BlockEntity tileEntity, CompoundTag compound)
    {
    	ClientboundBlockEntityDataPacket packet = new ClientboundBlockEntityDataPacket(tileEntity.getBlockPos(), 0, compound);
        sendUpdatePacket(tileEntity.getLevel(), tileEntity.getBlockPos(), packet);
    }

    private static void sendUpdatePacket(Level level, BlockPos pos, ClientboundBlockEntityDataPacket packet)
    {
        if(level instanceof ServerLevel)
        {
        	//CurrencyMod.LOGGER.info("Sending Tile Entity Update Packet to the connected clients.");
        	ServerLevel server = (ServerLevel) level;
            @SuppressWarnings("resource")
			Stream<ServerPlayer> players = server.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos), false);
            players.forEach(player -> player.connection.send(packet));
        }
        else
        {
        	LightmansCurrency.LogWarning("Cannot send Tile Entity Update Packet from a client.");
        }
    }
    
    public static void requestUpdatePacket(BlockEntity blockEntity)
    {
    	requestUpdatePacket(blockEntity.getLevel(), blockEntity.getBlockPos());
    }
    
    public static void requestUpdatePacket(Level level, BlockPos pos)
    {
    	if(level.isClientSide)
    		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageRequestNBT(pos));
    }
    
}
