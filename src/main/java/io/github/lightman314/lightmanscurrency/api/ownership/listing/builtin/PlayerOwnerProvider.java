package io.github.lightman314.lightmanscurrency.api.ownership.listing.builtin;

import com.mojang.authlib.GameProfile;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.IPotentialOwnerProvider;
import io.github.lightman314.lightmanscurrency.api.ownership.listing.PotentialOwner;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PlayerOwnerProvider implements IPotentialOwnerProvider {

    public static final IPotentialOwnerProvider INSTANCE = new PlayerOwnerProvider();

    private PlayerOwnerProvider() {}

    @Nonnull
    @Override
    public List<PotentialOwner> collectPotentialOwners(@Nonnull Player player) {
        List<PotentialOwner> results = new ArrayList<>();
        for(GameProfile profile : LightmansCurrency.PROXY.getPlayerList(player.level().isClientSide))
        {
            PlayerReference pr = PlayerReference.of(profile);
            PotentialPlayerOwner ppo = new PotentialPlayerOwner(pr);
            if(pr.is(player))
                ppo.flagAsHighPriority();
            results.add(ppo);
        }
        return results;
    }

}
