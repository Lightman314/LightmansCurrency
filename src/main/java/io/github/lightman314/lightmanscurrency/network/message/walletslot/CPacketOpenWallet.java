package io.github.lightman314.lightmanscurrency.network.message.walletslot;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.containers.providers.WalletInventoryContainerProvider;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.fml.network.NetworkHooks;

public class CPacketOpenWallet implements IMessage<CPacketOpenWallet> {
	
	
	@Override
	public void encode(CPacketOpenWallet message, PacketBuffer buffer) {
		
	}

	@Override
	public CPacketOpenWallet decode(PacketBuffer buffer) {
		return new CPacketOpenWallet();
	}

	@Override
	public void handle(CPacketOpenWallet message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			ServerPlayerEntity sender = supplier.get().getSender();
			if(sender != null)
			{
				ItemStack stack = sender.inventory.getItemStack();
				sender.inventory.setItemStack(ItemStack.EMPTY);
				NetworkHooks.openGui(sender, new WalletInventoryContainerProvider());
				
				if(!stack.isEmpty())
				{
					sender.inventory.setItemStack(stack);
					LightmansCurrencyPacketHandler.instance.send(PacketDistributor.PLAYER.with(() -> sender), new SPacketGrabbedItem(stack));
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
