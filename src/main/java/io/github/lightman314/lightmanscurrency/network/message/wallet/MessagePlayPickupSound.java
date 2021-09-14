package io.github.lightman314.lightmanscurrency.network.message.wallet;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.network.message.IMessage;
import io.github.lightman314.lightmanscurrency.CurrencySoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.api.distmarker.Dist;

public class MessagePlayPickupSound implements IMessage<MessagePlayPickupSound> {
	
	//private static final SoundEvent pickupSound = new SoundEvent(new ResourceLocation("minecraft","entity.item.pickup"));
	//private static final SoundEvent pickupSound = new SoundEvent(new ResourceLocation("lightmanscurrency","coins_clinking"));
	
	public MessagePlayPickupSound()
	{
		
	}
	
	@Override
	public void encode(MessagePlayPickupSound message, FriendlyByteBuf buffer) {
		//buffer.writeBlockPos(message.pos);
	}

	@Override
	public MessagePlayPickupSound decode(FriendlyByteBuf buffer) {
		return new MessagePlayPickupSound();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(MessagePlayPickupSound message, Supplier<NetworkEvent.Context> supplier) {
		supplier.get().enqueueWork(() ->
		{
			Minecraft instance = Minecraft.getInstance();
			if(instance != null)
			{
				LocalPlayer player = instance.player;
				if(player != null)
				{
					player.level.playSound((Player)player, player.blockPosition(), CurrencySoundEvents.COINS_CLINKING, SoundSource.PLAYERS, 0.4f, 1f);
				}
			}
		});
		supplier.get().setPacketHandled(true);
	}

}
