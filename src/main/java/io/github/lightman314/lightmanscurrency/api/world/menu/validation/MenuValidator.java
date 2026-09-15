package io.github.lightman314.lightmanscurrency.api.world.menu.validation;

import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin.SimpleValidator;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class MenuValidator {

    private static final StreamCodec<RegistryFriendlyByteBuf,MenuValidator> INTERNAL_STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.Misc.MENU_VALIDATOR_KEY)
            .dispatch(MenuValidator::getType,Function.identity());
    public static final StreamCodec<RegistryFriendlyByteBuf,MenuValidator> STREAM_CODEC = StreamCodec.of((buf,val) -> {
        buf.writeBoolean(val.networkAccess);
        INTERNAL_STREAM_CODEC.encode(buf,val);
    },buf -> {
        boolean networkAccess = buf.readBoolean();
        MenuValidator val = INTERNAL_STREAM_CODEC.decode(buf);
        val.networkAccess = networkAccess;
        return val;
    });

    private boolean networkAccess;
    public final boolean isNetworkAccess() { return this.networkAccess; }
    public final void flagAsNetworkAccess() { this.networkAccess = true; }

    public abstract StreamCodec<? super RegistryFriendlyByteBuf,? extends MenuValidator> getType();

    public abstract boolean stillValid(Player player);
    public static boolean stillValid(Player player, List<MenuValidator> validatorList) { return new ArrayList<>(validatorList).stream().allMatch(v -> v.stillValid(player)); }

    public final boolean isSimple() { return this instanceof SimpleValidator; }

}
