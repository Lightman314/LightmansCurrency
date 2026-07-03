package io.github.lightman314.lightmanscurrency.api.ownership;

import java.util.Objects;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;


public final class OwnerHolder implements ISidedContext.Mutable<OwnerHolder> {

    public static final Codec<OwnerHolder> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    Owner.CODEC.fieldOf("backup").forGetter(o -> o.backupOwner),
                    Owner.CODEC.fieldOf("owner").forGetter(o -> o.currentOwner)
            ).apply(builder,OwnerHolder::new));

    public static final StreamCodec<RegistryFriendlyByteBuf,OwnerHolder> STREAM_CODEC = StreamCodec.composite(
            Owner.STREAM_CODEC,o -> o.backupOwner,
            Owner.STREAM_CODEC,o -> o.currentOwner,
            OwnerHolder::new);

    private Owner backupOwner = Owner.getNull(this);
    private Owner currentOwner = Owner.getNull(this);

    @Override
    public boolean isClient() { return this.parent.isClient(); }

    private ISidedContext parent;
    private final Consumer<OwnerHolder> onChanged;

    @Override
    public OwnerHolder setSidedContext(ISidedContext context) { this.parent = context; return this; }

    private OwnerHolder(Owner backupOwner,Owner currentOwner) {
        this();
        this.backupOwner = backupOwner.setSidedContext(this);
        this.currentOwner = currentOwner.setSidedContext(this);
    }
    public OwnerHolder() { this(ISidedContext.LOGICAL_CLIENT,() -> {});}
    public OwnerHolder(ISidedContext parent) { this(parent,o -> {}); }
    public OwnerHolder(ISidedContext parent, Runnable onChanged) { this(parent,o -> onChanged.run()); }
    public OwnerHolder(ISidedContext parent, Consumer<OwnerHolder> onChanged) { this.parent = parent; this.onChanged = onChanged; }

    public Owner getValidOwner() { return this.currentOwner.stillValid() ? this.currentOwner : this.backupOwner; }
    public boolean hasOwner() { return this.currentOwner.stillValid() || this.backupOwner.stillValid(); }

    public void copyFrom(OwnerHolder owner) {
        if(owner == null || owner == this)
            return;
        this.backupOwner = owner.backupOwner.copyWithContext(this);
        this.currentOwner = owner.currentOwner.copyWithContext(this);
    }

    public PlayerReference getPlayerForContext() { return this.getValidOwner().asPlayerReference(); }

    public boolean isAdmin(Player player) { return LCApi.isInAdminMode(player) || this.isAdmin(PlayerReference.of(player)); }

    public boolean isAdmin(PlayerReference player) { return this.getValidOwner().isAdmin(player); }

    public boolean isMember(Player player) { return LCApi.isInAdminMode(player) || this.isMember(PlayerReference.of(player));}

    public boolean isMember(PlayerReference player) { return this.getValidOwner().isMember(player); }

    public Component getName() { return this.getValidOwner().getName(); }

    public void setOwner(Owner newOwner)
    {
        if(this.currentOwner.matches(newOwner))
            return;
        this.currentOwner = newOwner.copyWithContext(this);
        if(this.currentOwner.alwaysValid())
            this.backupOwner = this.currentOwner.copyWithContext(this);
        this.setChanged();
    }

    public void setChanged() { this.onChanged.accept(this); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof OwnerHolder owner)
            return owner.currentOwner.matches(this.currentOwner) && owner.backupOwner.matches(this.backupOwner);
        return false;
    }

    @Override
    public int hashCode() { return Objects.hash(this.currentOwner,this.backupOwner); }

    @Override
    public String toString() { return "OwnerHolder[" + this.backupOwner.toString() + ";" + this.currentOwner.toString() + "]"; }

}