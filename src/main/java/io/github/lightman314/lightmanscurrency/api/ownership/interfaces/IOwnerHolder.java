package io.github.lightman314.lightmanscurrency.api.ownership.interfaces;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public interface IOwnerHolder extends ISidedContext {

    Owner getValidOwner();
    default boolean hasValidOwner() { return this.getValidOwner().stillValid(); }
    default PlayerReference getPlayerForContext() { return this.getValidOwner().asPlayerReference(); }

    default boolean isAdmin(Player player) { return LCApi.isInAdminMode(player) || this.isAdmin(PlayerReference.of(player)); }

    default boolean isAdmin(PlayerReference player) { return this.getValidOwner().isAdmin(player); }

    default boolean isMember(Player player) { return LCApi.isInAdminMode(player) || this.isMember(PlayerReference.of(player));}

    default boolean isMember(PlayerReference player) { return this.getValidOwner().isMember(player); }

    default Component getName() { return this.getValidOwner().getName(); }

    default boolean hasMemberLevels() { return this.getValidOwner().hasMemberLevels(); }

    final class Simple implements IOwnerHolder {

        private final Owner owner;
        private final ISidedContext context;
        public Simple(Owner owner,ISidedContext context) {
            this.owner = owner.copyWithContext(this);
            this.context = context;
        }
        @Override
        public Owner getValidOwner() { return this.owner; }
        @Override
        public boolean isClient() { return this.context.isClient(); }
    }

}
