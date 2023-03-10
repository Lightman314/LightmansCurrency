package io.github.lightman314.lightmanscurrency.network.message.armor_display;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ArmorDisplayTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

public class MessageRequestArmorStandID {

	BlockPos pos;
	
	public MessageRequestArmorStandID(BlockPos pos) {
		this.pos = pos;
	}
	
	public static void encode(MessageRequestArmorStandID message, PacketBuffer buffer) {
		buffer.writeBlockPos(message.pos);
	}
	
	public static MessageRequestArmorStandID decode(PacketBuffer buffer) {
		return new MessageRequestArmorStandID(buffer.readBlockPos());
	}
	
	public static void handle(MessageRequestArmorStandID message, Supplier<NetworkEvent.Context> source) {
		source.get().enqueueWork(() ->{
			ServerPlayerEntity player = source.get().getSender();
			TileEntity be = player.level.getBlockEntity(message.pos);
			if(be instanceof ArmorDisplayTraderBlockEntity) {
				ArmorDisplayTraderBlockEntity ad = (ArmorDisplayTraderBlockEntity)be;
				ad.sendArmorStandSyncMessageToClient(LightmansCurrencyPacketHandler.getTarget(player));
			}
		});
		source.get().setPacketHandled(true);
	}
	
}
