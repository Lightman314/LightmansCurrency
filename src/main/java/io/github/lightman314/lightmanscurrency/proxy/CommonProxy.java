package io.github.lightman314.lightmanscurrency.proxy;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraft.nbt.CompoundTag;

public class CommonProxy {
	
	public void setupClient() {}
	
	public void registerLayers(final EntityRenderersEvent.RegisterLayerDefinitions event) {}	
	
	public void updateTraders(CompoundTag compound) {}
	
	public void openTerminalScreen(Player player) {}
	
}
