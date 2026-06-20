package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin.BlockEntityValidator;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin.BlockValidator;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin.SimpleValidator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LCMenuValidators {
    private LCMenuValidators() {}

    public static final DeferredRegister<StreamCodec<? super RegistryFriendlyByteBuf,? extends MenuValidator>> REGISTER = DeferredRegister.create(LCRegistries.Misc.MENU_VALIDATOR,LCApi.MODID);

    static {
        register("simple",SimpleValidator.STREAM_CODEC);
        register("block", BlockValidator.STREAM_CODEC);
        register("block_entity", BlockEntityValidator.STREAM_CODEC);
    }

    private static void register(String name,StreamCodec<? super RegistryFriendlyByteBuf,? extends MenuValidator> codec) {
        REGISTER.register(name,() -> codec);
    }
}
