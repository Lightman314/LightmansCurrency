package io.github.lightman314.lightmanscurrency.api.misc;

import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

public interface IPermissions {

	default boolean hasPermission(@Nonnull Player player, @Nonnull String permission) { return this.getPermissionLevel(player, permission) > 0; }
	default boolean hasPermission(@Nonnull PlayerReference player, @Nonnull String permission) { return this.getPermissionLevel(player, permission) > 0; }
	int getPermissionLevel(@Nonnull Player player, @Nonnull String permission);
	int getPermissionLevel(@Nonnull PlayerReference player, @Nonnull String permission);
	
}
