package io.github.lightman314.lightmanscurrency.network.message.player;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.UsernameCache;
import net.minecraftforge.network.NetworkEvent.Context;

public class CPacketRequestPlayerList {

	public static void handle(CPacketRequestPlayerList message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() -> {
			
			CompoundTag data = new CompoundTag();
			ListTag playerList = new ListTag();
			
			UsernameCache.getMap().forEach((id,name) -> {
				PlayerReference pr = PlayerReference.of(id, name);
				if(pr != null)
					playerList.add(pr.save());
			});
			
			data.put("KnownPlayers", playerList);
			LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(supplier.get().getSender()), new SPacketSendPlayerList(data));
			
		});
		supplier.get().setPacketHandled(true);
	}
	
}
