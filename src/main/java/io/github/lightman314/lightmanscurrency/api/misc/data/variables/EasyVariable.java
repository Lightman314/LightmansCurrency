package io.github.lightman314.lightmanscurrency.api.misc.data.variables;

import io.github.lightman314.lightmanscurrency.api.misc.data.variables.permissions.IVariablePermission;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class EasyVariable<T> {

    private boolean setup = true;
    public boolean isInSetup() { return this.setup; }
    protected final IVariableHost host;
    public final VariableCategory category;
    public final String key;
    private final IVariablePermission permission;
    public EasyVariable(Builder builder) {
        this.host = builder.host;
        this.category = builder.category;
        this.key = builder.key;
        this.permission = builder.permission;
        this.host.registerVariable(this);
        this.setup = false;
    }

    public abstract T get();
    protected abstract void setInternal(T newValue);

    public final void set(T newValue) { this.setInternal(newValue); }
    public final void set(@Nullable Player player, T newValue) {
        if(this.permission.canEdit(player,host))
            this.setInternal(newValue);
    }
    protected final void setChanged() { this.host.markVariableChanged(this); }

    public abstract void save(CompoundTag tag, HolderLookup.Provider lookup);
    public abstract void load(CompoundTag tag, HolderLookup.Provider lookup);

    public static Builder builder(IVariableHost host) { return new Builder(host); }

    public static class Builder
    {

        private Builder(IVariableHost host) { this.host = host; }

        private final IVariableHost host;
        private VariableCategory category = VariableCategory.EMPTY;
        private String key = "null";
        private IVariablePermission permission = IVariablePermission.noTest();

        public Builder category(Component name, String key) { return  this.category(new VariableCategory(name,key)); }
        public Builder category(VariableCategory category) { this.category = category; return this; }

        public Builder key(String key) { this.key = key; return this; }

        public Builder memberAccess() { return this.permissions(IVariablePermission.membersOnly()); }
        public Builder adminAccess() { return this.permissions(IVariablePermission.adminsOnly()); }

        public Builder hasPermission(String permission) { return this.permissions(IVariablePermission.hasPermission(permission)); }
        public Builder minPermission(String permission,int minLevel) { return this.permissions(IVariablePermission.minPermission(permission,minLevel)); }
        public Builder exactPermission(String permission,int exactLevel) { return this.permissions(IVariablePermission.exactPermission(permission,exactLevel)); }

        public Builder permissions(IVariablePermission permission) { this.permission = permission; return this; }

    }

}
