package io.github.lightman314.lightmanscurrency.network.message.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraft.network.PacketBuffer;

public class MessageSyncAdminList {
	
	List<UUID> adminList;
	
	public MessageSyncAdminList(List<UUID> adminList)
	{
		this.adminList = adminList;
	}
	
	public static void encode(MessageSyncAdminList message, PacketBuffer buffer) {
		buffer.writeInt(message.adminList.size());
		for(UUID id : message.adminList)
			buffer.writeUUID(id);
	}

	public static MessageSyncAdminList decode(PacketBuffer buffer) {
		List<UUID> list = new ArrayList<>();
		int count = buffer.readInt();
		for(int i = 0; i < count; ++i)
			list.add(buffer.readUUID());
		return new MessageSyncAdminList(list);
	}

	public static void handle(MessageSyncAdminList message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.loadAdminPlayers(message.adminList));
		supplier.get().setPacketHandled(true);
	}

}
