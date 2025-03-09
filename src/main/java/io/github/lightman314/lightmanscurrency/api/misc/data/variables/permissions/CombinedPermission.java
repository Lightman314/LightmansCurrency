package io.github.lightman314.lightmanscurrency.api.misc.data.variables.permissions;

import io.github.lightman314.lightmanscurrency.api.misc.data.variables.IVariableHost;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CombinedPermission implements IVariablePermission {

    private final IVariablePermission permission1;
    private final IVariablePermission permission2;
    private final boolean isAndTest;

    private CombinedPermission(IVariablePermission permission1,IVariablePermission permission2,boolean isAndTest) {
        this.permission1 = permission1;
        this.permission2 = permission2;
        this.isAndTest = isAndTest;
    }

    @Override
    public boolean canEdit(Player player, IVariableHost host) {
        return this.isAndTest ? this.permission1.canEdit(player,host) && this.permission2.canEdit(player,host) : this.permission1.canEdit(player,host) || this.permission2.canEdit(player,host);
    }

    public static IVariablePermission and(IVariablePermission perm1, IVariablePermission perm2) { return new CombinedPermission(perm1,perm2,true); }
    public static IVariablePermission or(IVariablePermission perm1, IVariablePermission perm2) { return new CombinedPermission(perm1,perm2,false); }

}