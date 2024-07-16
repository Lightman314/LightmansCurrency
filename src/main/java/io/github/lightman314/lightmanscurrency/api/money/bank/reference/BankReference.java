package io.github.lightman314.lightmanscurrency.api.money.bank.reference;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.TeamBankReference;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.MoneyHolder;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BankReference extends MoneyHolder.Slave implements IClientTracker {

    private boolean isClient = false;
    public boolean isClient() { return this.isClient; }
    public BankReference flagAsClient() { return this.flagAsClient(true); }
    public BankReference flagAsClient(boolean isClient) { this.isClient = isClient; return this; }
    public BankReference flagAsClient(@Nonnull IClientTracker context) { this.isClient = context.isClient(); return this; }

    protected final BankReferenceType type;
    protected BankReference(@Nonnull BankReferenceType type) { this.type = type; }

    @Nullable
    public abstract IBankAccount get();

    public abstract boolean allowedAccess(@Nonnull Player player);
    public boolean canPersist(@Nonnull Player player) { return true; }

    @Nonnull
    public final CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        tag.putString("Type", this.type.id.toString());
        return tag;
    }

    protected abstract void saveAdditional(@Nonnull CompoundTag tag);

    public final void encode(@Nonnull FriendlyByteBuf buffer)
    {
        buffer.writeUtf(this.type.id.toString());
        this.encodeAdditional(buffer);
    }

    protected abstract void encodeAdditional(@Nonnull FriendlyByteBuf buffer);

    @Nullable
    public static BankReference load(CompoundTag tag)
    {
        if(tag.contains("Type"))
        {
            BankReferenceType type = BankAPI.API.GetReferenceType(new ResourceLocation(tag.getString("Type")));
            if(type != null)
                return type.load(tag);
            else
                LightmansCurrency.LogWarning("No Bank Reference Type '" + type + "' could be loaded.");
        }
        else
        {
            //Load from old AccountReference data
            if(tag.contains("PlayerID"))
                return PlayerBankReference.of(tag.getUUID("PlayerID"));
            if(tag.contains("TeamID"))
                return TeamBankReference.of(tag.getLong("TeamID"));
        }
        return null;
    }

    @Nullable
    public static BankReference decode(@Nonnull FriendlyByteBuf buffer)
    {
        BankReferenceType type = BankAPI.API.GetReferenceType(new ResourceLocation(buffer.readUtf()));
        if(type != null)
            return type.decode(buffer);
        else
            LightmansCurrency.LogWarning("No Bank Reference Type '" + type + "' could be decoded.");
        return null;
    }

    @Override
    @Nullable
    protected IMoneyHolder getParent() { return this.get(); }

}
