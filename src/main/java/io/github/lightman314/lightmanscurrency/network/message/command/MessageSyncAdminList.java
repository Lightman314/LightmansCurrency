package io.github.lightman314.lightmanscurrency.network.message.command;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessageSyncAdminList implements IMessage<MessageSyncAdminList> {
	
	List<UUID> adminList;
	
	public MessageSyncAdminList()
	{
		
	}
	
	public MessageSyncAdminList(List<UUID> adminList)
	{
		this.adminList = adminList;
	}
	
	private CompoundNBT getAsCompound()
	{
		CompoundNBT compound = new CompoundNBT();
		ListNBT adminListData = new ListNBT();
		for(int i = 0; i < adminList.size(); i++)
		{
			CompoundNBT thisData = new CompoundNBT();
			if(adminList.get(i) != null)
				thisData.putUniqueId("id", adminList.get(i));
			adminListData.add(thisData);
		}
		compound.put("data", adminListData);
		return compound;
	}
	
	private static List<UUID> readFromCompound(CompoundNBT compound)
	{
		List<UUID> adminList = new ArrayList<>();
		ListNBT adminListData = compound.getList("data", Constants.NBT.TAG_COMPOUND);
		for(int i = 0; i < adminListData.size(); i++)
		{
			CompoundNBT thisData = adminListData.getCompound(i);
			adminList.add(thisData.getUniqueId("id"));
		}
		return adminList;
	}
	
	@Override
	public void encode(MessageSyncAdminList message, PacketBuffer buffer) {
		buffer.writeCompoundTag(message.getAsCompound());
	}

	@Override
	public MessageSyncAdminList decode(PacketBuffer buffer) {
		return new MessageSyncAdminList(readFromCompound(buffer.readCompoundTag()));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(MessageSyncAdminList message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			//Run through proxy to prevent hacked clients from changing the admin list
			LightmansCurrency.PROXY.loadAdminPlayers(message.adminList);
		});
		supplier.get().setPacketHandled(true);
	}

}
