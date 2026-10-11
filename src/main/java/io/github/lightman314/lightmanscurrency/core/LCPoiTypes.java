package io.github.lightman314.lightmanscurrency.core;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.ITallBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class LCPoiTypes {
    private LCPoiTypes() {}

    public static final DeferredRegister<PoiType> REGISTER = DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE,LCApi.MODID);

    public static final DeferredHolder<PoiType,PoiType> BANKER = register("banker",() -> getStates(LCBlocks.ATM.get(),s -> s.getValue(ITallBlock.ISBOTTOM)));
    public static final DeferredHolder<PoiType,PoiType> CASHIER = register("cashier",LCBlocks.CASH_REGISTER);

    public static Set<BlockState> getStates(Block block) { return Set.copyOf(block.getStateDefinition().getPossibleStates()); }
    public static Set<BlockState> getStates(Block block,Predicate<BlockState> filter) {
        Set<BlockState> set = new HashSet<>();
        for(BlockState state : block.getStateDefinition().getPossibleStates()) {
            if(filter.test(state))
                set.add(state);
        }
        return Set.copyOf(set);
    }

    private static DeferredHolder<PoiType,PoiType> register(String name,DeferredBlock<?> block) { return register(name,() -> getStates(block.get())); }
    private static DeferredHolder<PoiType,PoiType> register(String name,Supplier<Set<BlockState>> states) {
        return REGISTER.register(name,() -> new PoiType(states.get(),1,1));
    }

}
