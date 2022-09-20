package io.github.lightman314.lightmanscurrency.network.message.armor_display;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.blockentity.trader.ArmorDisplayTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent.Context;

public class MessageRequestArmorStandID {

	BlockPos pos;
	
	public MessageRequestArmorStandID(BlockPos pos) {
		this.pos = pos;
	}
	
	public static void encode(MessageRequestArmorStandID message, FriendlyByteBuf buffer) {
		buffer.writeBlockPos(message.pos);
	}
	
	public static MessageRequestArmorStandID decode(FriendlyByteBuf buffer) {
		return new MessageRequestArmorStandID(buffer.readBlockPos());
	}
	
	public static void handle(MessageRequestArmorStandID message, Supplier<Context> source) {
		source.get().enqueueWork(() ->{
			ServerPlayer player = source.get().getSender();
			BlockEntity be = player.level.getBlockEntity(message.pos);
			if(be instanceof ArmorDisplayTraderBlockEntity) {
				ArmorDisplayTraderBlockEntity ad = (ArmorDisplayTraderBlockEntity)be;
				ad.sendArmorStandSyncMessageToClient(LightmansCurrencyPacketHandler.getTarget(player));
			}
		});
		source.get().setPacketHandled(true);
	}
	
}
