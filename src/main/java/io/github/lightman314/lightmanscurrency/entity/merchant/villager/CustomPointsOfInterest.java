package io.github.lightman314.lightmanscurrency.entity.merchant.villager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.PointOfInterestType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CustomPointsOfInterest {

	private static final List<PointOfInterestType> INTEREST_TYPES = new ArrayList<>();
	
	public static final PointOfInterestType BANKER = register("banker", PointOfInterestType.getAllStates(ModBlocks.MACHINE_ATM.block), 1, 1);
	public static final PointOfInterestType CASHIER = register("cashier", PointOfInterestType.getAllStates(ModBlocks.CASH_REGISTER.block), 1, 1);
	
	private static PointOfInterestType register(String type, Set<BlockState> blockStates, int maxFreeTickets, int validRange)
	{
		PointOfInterestType interestType = new PointOfInterestType(type, blockStates, maxFreeTickets, validRange);
		interestType.setRegistryName(new ResourceLocation(LightmansCurrency.MODID, type));
		INTEREST_TYPES.add(interestType);
		return interestType;
	}
	
	@SubscribeEvent
	public static void registerInterestTypes(RegistryEvent.Register<PointOfInterestType> event)
	{
		INTEREST_TYPES.forEach(type -> event.getRegistry().register(type));
		INTEREST_TYPES.clear();
	}
	
}
