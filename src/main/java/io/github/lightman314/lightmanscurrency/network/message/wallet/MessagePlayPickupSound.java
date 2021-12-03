package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.CurrencySoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class MessagePlayPickupSound implements IMessage<MessagePlayPickupSound> {
	
	//private static final SoundEvent pickupSound = new SoundEvent(new ResourceLocation("minecraft","entity.item.pickup"));
	//private static final SoundEvent pickupSound = new SoundEvent(new ResourceLocation("lightmanscurrency","coins_clinking"));
	
	public MessagePlayPickupSound()
	{
		
	}
	
	@Override
	public void encode(MessagePlayPickupSound message, PacketBuffer buffer) {
		//buffer.writeBlockPos(message.pos);
	}

	@Override
	public MessagePlayPickupSound decode(PacketBuffer buffer) {
		return new MessagePlayPickupSound();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(MessagePlayPickupSound message, Supplier<Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Minecraft instance = Minecraft.getInstance();
			if(instance != null)
			{
				ClientPlayerEntity player = instance.player;
				if(player != null)
				{
					player.world.playSound((PlayerEntity)player, player.getPosition(), CurrencySoundEvents.COINS_CLINKING, SoundCategory.PLAYERS, 0.4f, 1f);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
