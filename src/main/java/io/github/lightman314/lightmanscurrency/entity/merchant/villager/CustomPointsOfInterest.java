package io.github.lightman314.lightmanscurrency.entity.merchant.villager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CustomPointsOfInterest {

	private static final List<PoiType> INTEREST_TYPES = new ArrayList<>();
	
	public static final PoiType BANKER = register("banker", PoiType.getBlockStates(ModBlocks.MACHINE_ATM.block), 1, 1);
	public static final PoiType CASHIER = register("cashier", PoiType.getBlockStates(ModBlocks.CASH_REGISTER.block), 1, 1);
	
	private static PoiType register(String type, Set<BlockState> blockStates, int maxFreeTickets, int validRange)
	{
		PoiType interestType = new PoiType(type, blockStates, maxFreeTickets, validRange);
		interestType.setRegistryName(new ResourceLocation(LightmansCurrency.MODID, type));
		INTEREST_TYPES.add(interestType);
		return interestType;
	}
	
	@SubscribeEvent
	public static void registerInterestTypes(RegistryEvent.Register<PoiType> event)
	{
		INTEREST_TYPES.forEach(type -> event.getRegistry().register(type));
		INTEREST_TYPES.clear();
	}
	
}
