package io.github.lightman314.lightmanscurrency.api.settings.data;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.DynamicOps;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.ownership.IOwnable;
import io.github.lightman314.lightmanscurrency.api.settings.ISaveableSettingsHolder;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class LoadContext {

    public final Player player;

    private final OwnerData oldOwner;
    private final OwnerData newOwner;

    private final Map<String,Integer> oldAllyPermissions;
    private Map<String,Integer> newAllyPermissions = ImmutableMap.of();

    private final List<PlayerReference> oldAllies;
    private List<PlayerReference> newAllies = ImmutableList.of();

    private final Predicate<String> blockedPermissions;

    private final DataContext<Tag> dataContext;

    private LoadContext(Player player, ISaveableSettingsHolder host, OwnerData owner, Map<String,Integer> allyPermissions, List<PlayerReference> allies, Predicate<String> blockedPermissions) {
        this.player = player;
        this.oldOwner = new OwnerData(host);
        this.oldOwner.copyFrom(owner);
        this.newOwner = new OwnerData(host);
        this.oldAllies = ImmutableList.copyOf(allies);
        this.oldAllyPermissions = ImmutableMap.copyOf(allyPermissions);
        this.blockedPermissions = blockedPermissions;
        this.dataContext = DataContext.createNBT(this.player.registryAccess());
    }

    public void updateOwner(OwnerData newOwner) { this.newOwner.copyFrom(newOwner); }
    public void updateAllies(List<PlayerReference> newAllies) { this.newAllies = ImmutableList.copyOf(newAllies); }
    public void updateAllyPermissions(Map<String,Integer> newAllyPerms) { this.newAllyPermissions = ImmutableMap.copyOf(newAllyPerms); }

    public boolean isServerAdmin() { return LCAdminMode.isAdminPlayer(this.player); }

    public boolean isAdmin() { return this.oldOwner.isAdmin(this.player) || this.newOwner.isAdmin(this.player); }
    public boolean isMember() { return this.oldOwner.isMember(this.player) || this.newOwner.isMember(this.player); }

    public boolean hasPermission(String permission) { return this.getPermissionLevel(permission) > 0; }

    public int getPermissionLevel(String permission) {
        if(this.blockedPermissions.test(permission))
            return 0;
        if(this.oldOwner.isAdmin(this.player) || this.newOwner.isAdmin(this.player))
            return Integer.MAX_VALUE;
        int level = 0;
        boolean member = this.oldOwner.isMember(this.player) || this.newOwner.isMember(this.player);
        if(PlayerReference.isInList(this.oldAllies,this.player) || member)
            level = this.oldAllyPermissions.getOrDefault(permission,0);
        if(PlayerReference.isInList(this.newAllies,this.player) || member)
            level = Math.max(level,this.newAllyPermissions.getOrDefault(permission,0));
        return level;
    }

    public DataContext<Tag> dataContext() { return this.dataContext; }
    public DynamicOps<Tag> ops() { return this.dataContext.ops(); }

    public static Builder builder(Player player, ISaveableSettingsHolder host) { return new Builder(player,host); }

    public static class Builder
    {
        private final Player player;
        private final ISaveableSettingsHolder host;
        private Builder(Player player, ISaveableSettingsHolder host) {
            this.player = player;
            this.host = host;
            this.oldOwner = new OwnerData(host);
            if(this.host instanceof IOwnable owner)
                this.oldOwner.copyFrom(owner.getOwner());
        }

        private final OwnerData oldOwner;
        private Map<String,Integer> allyPermissions = ImmutableMap.of();
        private List<PlayerReference> allies = ImmutableList.of();
        private Predicate<String> blockedPermissions = Predicates.alwaysFalse();

        public Builder withOwner(IOwnable owner) { this.oldOwner.copyFrom(owner.getOwner()); return this; }
        public Builder withOwner(OwnerData owner) { this.oldOwner.copyFrom(owner); return this; }

        public Builder withAllyPermissions(Map<String,Integer> allyPermissions) { this.allyPermissions = ImmutableMap.copyOf(allyPermissions); return this; }
        public Builder withAllies(List<PlayerReference> allies) { this.allies = ImmutableList.copyOf(allies); return this; }

        public Builder withBlockedPermissions(List<String> blockedPermissions) { this.blockedPermissions = blockedPermissions::contains; return this; }
        public Builder withBlockedPermissions(Predicate<String> blockedPermissions) { this.blockedPermissions = blockedPermissions; return this; }

        public LoadContext build() { return new LoadContext(this.player,this.host, this.oldOwner,this.allyPermissions,this.allies,this.blockedPermissions); }

    }

}
