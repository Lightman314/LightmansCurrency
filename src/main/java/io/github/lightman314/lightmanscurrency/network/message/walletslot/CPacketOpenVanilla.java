package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fmllegacy.network.NetworkEvent.Context;

public class CPacketOpenVanilla {
	
	public static void encode(CPacketOpenVanilla message, FriendlyByteBuf buffer) { }

	public static CPacketOpenVanilla decode(FriendlyByteBuf buffer) {
		return new CPacketOpenVanilla();
	}

	public static void handle(CPacketOpenVanilla message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				ItemStack stack = player.containerMenu.getCarried();
				player.containerMenu.setCarried(ItemStack.EMPTY);
				player.closeContainer();
				
				if(!stack.isEmpty())
				{
					player.containerMenu.setCarried(stack);
					LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new SPacketGrabbedItem(stack));
				}
				LightmansCurrencyPacketHandler.instance.send(LightmansCurrencyPacketHandler.getTarget(player), new SPacketOpenVanillaResponse());
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
