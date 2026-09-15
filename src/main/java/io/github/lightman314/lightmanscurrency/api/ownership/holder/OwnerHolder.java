package io.github.lightman314.lightmanscurrency.api.ownership.holder;

import java.util.Objects;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnerHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;


public final class OwnerHolder implements IOwnerHolder, ISidedContext.Mutable<OwnerHolder> {

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

    @Override
    public Owner getValidOwner() { return this.currentOwner.stillValid() ? this.currentOwner : this.backupOwner; }

    public void copyFrom(OwnerHolder owner) {
        if(owner == null || owner == this)
            return;
        this.backupOwner = owner.backupOwner.copyWithContext(this);
        this.currentOwner = owner.currentOwner.copyWithContext(this);
    }

    public void setOwner(Owner newOwner)
    {
        if(this.currentOwner.equals(newOwner))
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
            return owner.currentOwner.equals(this.currentOwner) && owner.backupOwner.equals(this.backupOwner);
        return false;
    }

    @Override
    public int hashCode() { return Objects.hash(this.currentOwner,this.backupOwner); }

    @Override
    public String toString() { return "OwnerHolder[" + this.backupOwner.toString() + ";" + this.currentOwner.toString() + "]"; }

}