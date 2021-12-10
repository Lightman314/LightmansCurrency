package io.github.lightman314.lightmanscurrency.network.message.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

public class MessageSyncAdminList {
	
	List<UUID> adminList;
	
	public MessageSyncAdminList(List<UUID> adminList)
	{
		this.adminList = adminList;
	}
	
	private CompoundTag getAsCompound()
	{
		CompoundTag compound = new CompoundTag();
		ListTag adminListData = new ListTag();
		for(int i = 0; i < adminList.size(); i++)
		{
			CompoundTag thisData = new CompoundTag();
			if(adminList.get(i) != null)
				thisData.putUUID("id", adminList.get(i));
			adminListData.add(thisData);
		}
		compound.put("data", adminListData);
		return compound;
	}
	
	private static List<UUID> readFromCompound(CompoundTag compound)
	{
		List<UUID> adminList = new ArrayList<>();
		ListTag adminListData = compound.getList("data", Tag.TAG_COMPOUND);
		for(int i = 0; i < adminListData.size(); i++)
		{
			CompoundTag thisData = adminListData.getCompound(i);
			adminList.add(thisData.getUUID("id"));
		}
		return adminList;
	}
	
	public static void encode(MessageSyncAdminList message, FriendlyByteBuf buffer) {
		buffer.writeNbt(message.getAsCompound());
	}

	public static MessageSyncAdminList decode(FriendlyByteBuf buffer) {
		return new MessageSyncAdminList(readFromCompound(buffer.readNbt()));
	}

	public static void handle(MessageSyncAdminList message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> LightmansCurrency.PROXY.loadAdminPlayers(message.adminList));
		supplier.get().setPacketHandled(true);
	}

}
