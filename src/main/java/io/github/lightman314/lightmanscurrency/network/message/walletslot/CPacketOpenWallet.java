package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.menus.providers.WalletInventoryMenuProvider;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;

public class CPacketOpenWallet {
	
	public static void encode(CPacketOpenWallet message, FriendlyByteBuf buffer) { }

	public static CPacketOpenWallet decode(FriendlyByteBuf buffer) {
		return new CPacketOpenWallet();
	}

	public static void handle(CPacketOpenWallet message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayer player = supplier.get().getSender();
			if(player != null)
			{
				ItemStack stack = player.containerMenu.getCarried();
				player.containerMenu.setCarried(ItemStack.EMPTY);
				NetworkHooks.openGui(player, new WalletInventoryMenuProvider());
				
				if(!stack.isEmpty())
				{
					player.containerMenu.setCarried(stack);
					LightmansCurrencyPacketHandler.instance.send(PacketDistributor.PLAYER.with(() -> player), new SPacketGrabbedItem(stack));
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
