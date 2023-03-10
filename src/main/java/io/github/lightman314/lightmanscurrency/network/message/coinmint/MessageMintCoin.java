package io.github.lightman314.lightmanscurrency.network.message.coinmint;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.blockentity.CoinMintBlockEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageMintCoin {

	final boolean fullStack;
	final BlockPos pos;
	
	public MessageMintCoin(boolean fullStack, BlockPos pos)
	{
		this.fullStack = fullStack;
		this.pos = pos;
	}
	
	public static void encode(MessageMintCoin message, PacketBuffer buffer) {
		buffer.writeBoolean(message.fullStack);
		buffer.writeBlockPos(message.pos);
	}

	public static MessageMintCoin decode(PacketBuffer buffer) {
		return new MessageMintCoin(buffer.readBoolean(), buffer.readBlockPos());
	}

	public static void handle(MessageMintCoin message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				TileEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof CoinMintBlockEntity)
				{
					CoinMintBlockEntity mintEntity = (CoinMintBlockEntity)blockEntity;
					mintEntity.mintCoins(message.fullStack ? 64 : 1);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
