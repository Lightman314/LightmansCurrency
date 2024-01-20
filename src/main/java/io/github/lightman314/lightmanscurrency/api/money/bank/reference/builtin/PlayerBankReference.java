package io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReferenceType;
import io.github.lightman314.lightmanscurrency.common.bank.BankSaveData;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerBankReference extends BankReference {

    public static final BankReferenceType TYPE = new Type();

    public final UUID playerID;

    protected PlayerBankReference(@Nonnull UUID playerID) { super(TYPE); this.playerID = playerID; }


    public static BankReference of(@Nonnull UUID player) { return new PlayerBankReference(player); }
    @Nullable
    public static BankReference of(@Nullable PlayerReference player) { return player != null ? new PlayerBankReference(player.id) : null; }
    public static BankReference of(@Nonnull Player player) { return new PlayerBankReference(player.getUUID()).flagAsClient(player.level().isClientSide); }

    @Nullable
    @Override
    public IBankAccount get() { return BankSaveData.GetBankAccount(this.isClient(), this.playerID); }

    @Override
    public boolean allowedAccess(@Nonnull Player player) { return LCAdminMode.isAdminPlayer(player) || this.playerID.equals(player.getUUID()); }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) { tag.putUUID("PlayerID", this.playerID); }

    @Override
    protected void encodeAdditional(@Nonnull FriendlyByteBuf buffer) { buffer.writeUUID(this.playerID); }

    @Override
    public boolean canPersist(@Nonnull Player player) { return this.playerID.equals(player.getUUID()); }

    private static final class Type extends BankReferenceType {
        Type() { super(new ResourceLocation(LightmansCurrency.MODID, "personal")); }
        @Override
        public BankReference load(CompoundTag tag) { return of(tag.getUUID("PlayerID")); }
        @Override
        public BankReference decode(FriendlyByteBuf buffer) { return of(buffer.readUUID()); }
    }

}
