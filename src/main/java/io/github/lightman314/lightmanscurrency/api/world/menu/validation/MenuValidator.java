package io.github.lightman314.lightmanscurrency.api.world.menu.validation;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin.SimpleValidator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public interface MenuValidator {

    StreamCodec<RegistryFriendlyByteBuf,MenuValidator> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.Misc.MENU_VALIDATOR_KEY)
            .dispatch(MenuValidator::getType,Function.identity());

    StreamCodec<? super RegistryFriendlyByteBuf,? extends MenuValidator> getType();

    boolean stillValid(Player player);

    default boolean isSimple() { return this instanceof SimpleValidator; }

}
