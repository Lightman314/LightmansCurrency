package io.github.lightman314.lightmanscurrency.common.capability;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyViewer;
import io.github.lightman314.lightmanscurrency.common.capability.event_unlocks.IEventUnlocks;
import io.github.lightman314.lightmanscurrency.common.capability.spawn_tracker.ISpawnTracker;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class CurrencyCapabilities {

	public static final Capability<IWalletHandler> WALLET = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<IMoneyHandler> MONEY_HANDLER = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<IMoneyViewer> MONEY_VIEWER = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<ISpawnTracker> SPAWN_TRACKER = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<IEventUnlocks> EVENT_TRACKER = CapabilityManager.get(new CapabilityToken<>() {});

	public static final ResourceLocation ID_WALLET = new ResourceLocation(LightmansCurrency.MODID,"wallet");
	public static final ResourceLocation ID_MONEY_VIEWER = new ResourceLocation(LightmansCurrency.MODID,"money_view");
	public static final ResourceLocation ID_MONEY_HANDLER = new ResourceLocation(LightmansCurrency.MODID,"money_handler");
	public static final ResourceLocation ID_SPAWN_TRACKER = new ResourceLocation(LightmansCurrency.MODID, "spawner_tracker");
	public static final ResourceLocation ID_EVENT_TRACKER = new ResourceLocation(LightmansCurrency.MODID, "event_tracker");

}
