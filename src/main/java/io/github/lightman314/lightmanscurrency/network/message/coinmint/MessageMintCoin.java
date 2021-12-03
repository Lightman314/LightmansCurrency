package io.github.lightman314.lightmanscurrency.network.message.coinmint;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.tileentity.CoinMintTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageMintCoin implements IMessage<MessageMintCoin> {

	private boolean fullStack;
	private BlockPos pos;
	
	public MessageMintCoin()
	{
		
	}
	
	public MessageMintCoin(boolean fullStack, BlockPos pos)
	{
		this.fullStack = fullStack;
		this.pos = pos;
	}
	
	
	@Override
	public void encode(MessageMintCoin message, PacketBuffer buffer) {
		buffer.writeBoolean(message.fullStack);
		buffer.writeBlockPos(message.pos);
	}

	@Override
	public MessageMintCoin decode(PacketBuffer buffer) {
		return new MessageMintCoin(buffer.readBoolean(), buffer.readBlockPos());
	}

	@Override
	public void handle(MessageMintCoin message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity entity = supplier.get().getSender();
			if(entity != null)
			{
				TileEntity tileEntity = entity.world.getTileEntity(message.pos);
				if(tileEntity instanceof CoinMintTileEntity)
				{
					CoinMintTileEntity mintEntity = (CoinMintTileEntity)tileEntity;
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
