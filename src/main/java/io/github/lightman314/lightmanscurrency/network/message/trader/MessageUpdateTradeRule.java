package io.github.lightman314.lightmanscurrency.network.message.trader;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.ITradeRuleHandler.ITradeRuleMessageHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageUpdateTradeRule {

	private BlockPos pos;
	private int index;
	private ResourceLocation type;
	private CompoundTag updateInfo;
	
	public MessageUpdateTradeRule(BlockPos pos, ResourceLocation type, CompoundTag updateInfo) {
		this(pos, -1, type, updateInfo);
	}
	
	public MessageUpdateTradeRule(BlockPos pos, int index, ResourceLocation type, CompoundTag updateInfo) {
		this.pos = pos;
		this.index = index;
		this.type = type;
		this.updateInfo = updateInfo;
	}
	
	public static void encode(MessageUpdateTradeRule message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
		buffer.writeInt(message.index);
		buffer.writeUtf(message.type.toString());
		buffer.writeNbt(message.updateInfo);
	}
	
	public static MessageUpdateTradeRule decode(FriendlyByteBuf buffer) {
		return new MessageUpdateTradeRule(buffer.readBlockPos(), buffer.readInt(), new ResourceLocation(buffer.readUtf()), buffer.readAnySizeNbt());
	}
	
	public static void handle(MessageUpdateTradeRule message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->{
			Player player = supplier.get().getSender();
			if(player != null)
			{
				BlockEntity be = player.level.getBlockEntity(message.pos);
				if(be instanceof ITradeRuleMessageHandler)
				{
					((ITradeRuleMessageHandler)be).receiveTradeRuleMessage(player, message.index, message.type, message.updateInfo);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}
	
}
