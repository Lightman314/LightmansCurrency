package io.github.lightman314.lightmanscurrency.api.ownership.listing.builtin;

import com.mojang.authlib.GameProfile;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwnerProvider;
import io.github.lightman314.lightmanscurrency.api.proxy.LCProxy;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class PlayerOwnerProvider implements PotentialOwnerProvider {

    public static final PotentialOwnerProvider INSTANCE = new PlayerOwnerProvider();

    private PlayerOwnerProvider() {}

    @Override
    public void collectPotentialOwners(Player player,Consumer<PotentialOwner> builder) {
        for(GameProfile profile : LCProxy.get().getPlayerList(ISidedContext.wrap(player)))
        {
            PlayerReference pr = PlayerReference.of(profile);
            PotentialPlayerOwner ppo = new PotentialPlayerOwner(pr);
            if(pr.is(player))
                ppo.flagAsHighPriority();
            builder.accept(ppo);
        }
    }

}