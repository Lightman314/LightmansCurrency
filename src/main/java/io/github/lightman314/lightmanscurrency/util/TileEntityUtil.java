package io.github.lightman314.lightmanscurrency.util;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.stream.Stream;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.MessageRequestNBT;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.entity.player.ServerPlayerEntity;

public class TileEntityUtil
{
    /**
     * Sends an update packet to clients tracking a tile entity.
     *
     * @param tileEntity the tile entity to update
     */
    public static void sendUpdatePacket(TileEntity tileEntity)
    {
        SUpdateTileEntityPacket packet = tileEntity.getUpdatePacket();
        if(packet != null)
        {
            sendUpdatePacket(tileEntity.getWorld(), tileEntity.getPos(), packet);
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
    public static void sendUpdatePacket(TileEntity tileEntity, CompoundNBT compound)
    {
        SUpdateTileEntityPacket packet = new SUpdateTileEntityPacket(tileEntity.getPos(), 0, compound);
        sendUpdatePacket(tileEntity.getWorld(), tileEntity.getPos(), packet);
    }

    private static void sendUpdatePacket(World world, BlockPos pos, SUpdateTileEntityPacket packet)
    {
        if(world instanceof ServerWorld)
        {
        	//CurrencyMod.LOGGER.info("Sending Tile Entity Update Packet to the connected clients.");
            ServerWorld server = (ServerWorld) world;
            @SuppressWarnings("resource")
			Stream<ServerPlayerEntity> players = server.getChunkProvider().chunkManager.getTrackingPlayers(new ChunkPos(pos), false);
            players.forEach(player -> player.connection.sendPacket(packet));
        }
        else
        {
        	LightmansCurrency.LogWarning("Cannot send Tile Entity Update Packet from a client.");
        }
    }
    
    public static void requestUpdatePacket(World world, BlockPos pos)
    {
    	if(world.isRemote)
    		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageRequestNBT(pos));
    }
    
}
