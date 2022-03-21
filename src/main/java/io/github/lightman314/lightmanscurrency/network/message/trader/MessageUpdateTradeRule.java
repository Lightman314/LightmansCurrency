package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler.ITradeRuleMessageHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageUpdateTradeRule {

	private BlockPos pos;
	private int index;
	private ResourceLocation type;
	private CompoundNBT updateInfo;
	
	public MessageUpdateTradeRule(BlockPos pos, ResourceLocation type, CompoundNBT updateInfo) {
		this(pos, -1, type, updateInfo);
	}
	
	public MessageUpdateTradeRule(BlockPos pos, int index, ResourceLocation type, CompoundNBT updateInfo) {
		this.pos = pos;
		this.index = index;
		this.type = type;
		this.updateInfo = updateInfo;
	}
	
	public static void encode(MessageUpdateTradeRule message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.index);
		buffer.writeString(message.type.toString());
		buffer.writeCompoundTag(message.updateInfo);
	}
	
	public static MessageUpdateTradeRule decode(PacketBuffer buffer) {
		return new MessageUpdateTradeRule(buffer.readBlockPos(), buffer.readInt(), new ResourceLocation(buffer.readString(1000)), buffer.readCompoundTag());
	}
	
	public static void handle(MessageUpdateTradeRule message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->{
			PlayerEntity player = supplier.get().getSender();
			if(player != null)
			{
				TileEntity be = player.world.getTileEntity(message.pos);
				if(be instanceof ITradeRuleMessageHandler)
				{
					((ITradeRuleMessageHandler)be).receiveTradeRuleMessage(player, message.index, message.type, message.updateInfo);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}
	
}
