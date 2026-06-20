package io.github.lightman314.lightmanscurrency.api.world.menu.validation;

import io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin.SimpleValidator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public interface IValidatedMenu {

    MenuValidator getValidator();

    void addValidator(MenuValidator validator);

    default void addCheck(BooleanSupplier stillValid) { this.addValidator(new SimpleValidator(p -> stillValid.getAsBoolean()));}
    default void addCheck(BooleanSupplier stillValid,Runnable failAction) { this.addValidator(new SimpleValidator(p -> stillValid.getAsBoolean(),failAction));}
    default void addCheck(Predicate<Player> stillValid) { this.addValidator(new SimpleValidator(stillValid)); }
    default void addCheck(Predicate<Player> stillValid,Runnable failAction) { this.addValidator(new SimpleValidator(stillValid,failAction)); }

    static MenuValidator extract(RegistryFriendlyByteBuf buffer) { return MenuValidator.STREAM_CODEC.decode(buffer); }
    static void encode(RegistryFriendlyByteBuf buffer,MenuValidator validator) { MenuValidator.STREAM_CODEC.encode(buffer,validator); }

}
