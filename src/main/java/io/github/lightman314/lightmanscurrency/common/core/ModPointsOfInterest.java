package io.github.lightman314.lightmanscurrency.common.core;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.ITallBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModPointsOfInterest {

    public static final DeferredRegister<PoiType> REGISTER = DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE,LightmansCurrency.MODID);

	public static final Supplier<PoiType> BANKER = registerVillager("banker",ModBlocks.ATM);
	public static final ResourceKey<PoiType> BANKER_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE,LightmansCurrency.id("banker"));
	public static final Supplier<PoiType> CASHIER = registerVillager("cashier",ModBlocks.CASH_REGISTER);
	public static final ResourceKey<PoiType> CASHIER_KEY = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE,LightmansCurrency.id("cashier"));


	private static Set<BlockState> getBlockStates(Block block) {
		if(block instanceof ITallBlock tallBlock)
			return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates().stream().filter(tallBlock::getIsBottom).collect(Collectors.toSet()));
		return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
	}

    public static DeferredHolder<PoiType,PoiType> registerVillager(String name, Supplier<? extends Block> block) {
        return register(name,() -> new PoiType(getBlockStates(block.get()),1,1));
    }
    public static DeferredHolder<PoiType,PoiType> register(String name,Supplier<PoiType> factory) {
        return REGISTER.register(name,factory);
    }
	
}
