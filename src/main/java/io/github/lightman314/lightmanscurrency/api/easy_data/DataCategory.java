package io.github.lightman314.lightmanscurrency.api.easy_data;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.IPermissions;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.BiFunction;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class DataCategory {

    public static final DataCategory NULL = builder().build();

    public final Component name;
    private final BiFunction<IEasyDataHost,PlayerReference,Boolean> canEdit;

    public DataCategory(Component name, BiFunction<IEasyDataHost,PlayerReference,Boolean> canEdit)
    {
        this.name = name;
        this.canEdit = canEdit;
    }
    private DataCategory(Builder builder)
    {
        this.name = Objects.requireNonNull(builder.name);
        this.canEdit = builder.canEdit;
    }

    public boolean canEdit(Player player,IEasyDataHost host) { return LCAdminMode.isAdminPlayer(player) || this.canEdit(PlayerReference.of(player),host); }
    public boolean canEdit(PlayerReference player,IEasyDataHost host) { return this.canEdit.apply(host,player); }

    public static Builder builder() { return new Builder(); }

    public static class Builder
    {
        private Component name = LCText.NOTIFICATION_SOURCE_NULL.get();
        private BiFunction<IEasyDataHost,PlayerReference,Boolean> canEdit = (h,p) -> false;

        private Builder() {}

        public Builder name(Component name) { this.name = name; return this; }

        public Builder adminAccess() { this.canEdit = (h,p) -> h.getOwner().isAdmin(p); return this; }
        public Builder memberAccess() { this.canEdit = (h,p) -> h.getOwner().isMember(p); return this; }

        public Builder permission(String permission) { this.canEdit = (h,p) -> h instanceof IPermissions h2 && h2.hasPermission(p,permission); return this; }
        public Builder permission(String permission, int minLevel) { this.canEdit = (h,p) -> h instanceof IPermissions h2 && h2.getPermissionLevel(p,permission) >= minLevel; return this; }

        public DataCategory build() { return new DataCategory(this); }

    }

}