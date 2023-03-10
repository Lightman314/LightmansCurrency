package io.github.lightman314.lightmanscurrency.common.capability;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class CurrencyCapabilities {

	@CapabilityInject(IWalletHandler.class)
	public static final Capability<IWalletHandler> WALLET;
	@CapabilityInject(ISpawnTracker.class)
	public static final Capability<ISpawnTracker> SPAWN_TRACKER;
	
	public static final ResourceLocation ID_WALLET = new ResourceLocation(LightmansCurrency.MODID,"wallet");
	public static final ResourceLocation ID_SPAWN_TRACKER = new ResourceLocation(LightmansCurrency.MODID, "spawner_tracker");

	static {
		WALLET = null;
		SPAWN_TRACKER= null;
	}

}
