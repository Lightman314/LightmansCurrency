package io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin;

import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReferenceType;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.common.data.types.BankDataCache;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PlayerBankReference extends BankReference {

    public static final BankReferenceType TYPE = new Type();

    private final PlayerReference player;
    public PlayerReference getPlayer() { return this.player; }

    protected PlayerBankReference(PlayerReference player) { super(TYPE); this.player = player; }

    public static BankReference of(UUID player) { return new PlayerBankReference(PlayerReference.of(player,"")); }
    @Nullable
    public static BankReference of(@Nullable PlayerReference player) { return player != null ? new PlayerBankReference(player) : null; }
    public static BankReference of(Player player) { return of(PlayerReference.of(player)).flagAsClient(player.level().isClientSide); }

    @Override
    public int sortPriority() { return 1000000; }

    @Nullable
    @Override
    public IconData getIcon() { return ItemIcon.ofItem(this.player.getSkull(this.isClient())); }

    @Nullable
    @Override
    public IBankAccount get() {
        BankDataCache data = BankDataCache.TYPE.get(this);
        return data == null ? null : data.getAccount(this.player.id);

    }

    @Override
    public boolean isSalaryTarget(PlayerReference player) { return this.player.is(player); }

    @Override
    public boolean allowedAccess(PlayerReference player) { return this.player.is(player); }
    @Override
    public boolean allowedAccess(Player player) { return LCAdminMode.isAdminPlayer(player) || this.player.is(player.getUUID()); }

    @Override
    public int salaryPermission(PlayerReference player) { return this.player.is(player) ? Integer.MAX_VALUE : 0; }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("Player",this.player.save());
    }

    @Override
    protected void encodeAdditional(FriendlyByteBuf buffer) {
        this.player.encode(buffer,this.isClient());
    }

    @Override
    public boolean canPersist(Player player) { return this.player.is(player); }

    private static final class Type extends BankReferenceType {
        Type() { super(VersionUtil.lcResource("personal")); }
        @Override
        public BankReference load(CompoundTag tag) {
            if(tag.contains("PlayerID"))
                return of(tag.getUUID("PlayerID"));
            else
                return of(PlayerReference.load(tag.getCompound("Player")));
        }
        @Override
        public BankReference decode(FriendlyByteBuf buffer) {
            return of(PlayerReference.decode(buffer));
        }
    }

}