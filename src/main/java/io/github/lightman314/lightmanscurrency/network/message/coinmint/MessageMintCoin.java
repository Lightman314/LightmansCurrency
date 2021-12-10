package io.github.lightman314.lightmanscurrency.network.message.coinmint;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.CoinMintBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class MessageMintCoin {

	private boolean fullStack;
	private BlockPos pos;
	
	public MessageMintCoin(boolean fullStack, BlockPos pos)
	{
		this.fullStack = fullStack;
		this.pos = pos;
	}
	
	public static void encode(MessageMintCoin message, FriendlyByteBuf buffer) {
		buffer.writeBoolean(message.fullStack);
		buffer.writeBlockPos(message.pos);
	}

	public static MessageMintCoin decode(FriendlyByteBuf buffer) {
		return new MessageMintCoin(buffer.readBoolean(), buffer.readBlockPos());
	}

	public static void handle(MessageMintCoin message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				BlockEntity blockEntity = player.level.getBlockEntity(message.pos);
				if(blockEntity instanceof CoinMintBlockEntity)
				{
					CoinMintBlockEntity mintEntity = (CoinMintBlockEntity)blockEntity;
					if(mintEntity.validMintOutput() > 0)
					{
						mintEntity.mintCoins(message.fullStack ? 64 : 1);
					}
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
