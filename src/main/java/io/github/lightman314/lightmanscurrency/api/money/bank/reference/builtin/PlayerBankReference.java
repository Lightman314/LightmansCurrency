package io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReferenceType;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.data.types.BankDataCache;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class PlayerBankReference extends BankReference {

    public static final BankReferenceType TYPE = new Type();

    private final PlayerReference player;

    protected PlayerBankReference(@Nonnull PlayerReference player) { super(TYPE); this.player = player; }


    public static BankReference of(@Nonnull UUID player) { return new PlayerBankReference(PlayerReference.of(player,"")); }
    @Nullable
    public static BankReference of(@Nullable PlayerReference player) { return player != null ? new PlayerBankReference(player) : null; }
    public static BankReference of(@Nonnull Player player) { return of(PlayerReference.of(player)).flagAsClient(player.level().isClientSide); }

    @Override
    public int sortPriority() { return 1000000; }

    @Nullable
    @Override
    public IconData getIcon() { return IconData.of(this.player.getSkull(this.isClient())); }

    @Nullable
    @Override
    public IBankAccount get() {
        BankDataCache data = BankDataCache.TYPE.get(this);
        return data == null ? null : data.getAccount(this.player.id);
    }

    @Override
    public boolean allowedAccess(@Nonnull PlayerReference player) { return this.player.is(player); }
    @Override
    public boolean allowedAccess(@Nonnull Player player) { return LCAdminMode.isAdminPlayer(player) || this.player.is(player); }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) { tag.put("Player",this.player.save()); }

    @Override
    protected void encodeAdditional(@Nonnull FriendlyByteBuf buffer) { this.player.encode(buffer,this.isClient()); }

    @Override
    public boolean canPersist(@Nonnull Player player) { return this.player.is(player); }

    private static final class Type extends BankReferenceType {
        Type() { super(new ResourceLocation(LightmansCurrency.MODID, "personal")); }
        @Override
        public BankReference load(CompoundTag tag) {
            if(tag.contains("PlayerID"))
                return of(tag.getUUID("PlayerID"));
            return of(PlayerReference.load(tag.getCompound("Player")));
        }
        @Override
        public BankReference decode(FriendlyByteBuf buffer) { return of(PlayerReference.decode(buffer)); }
    }

}
