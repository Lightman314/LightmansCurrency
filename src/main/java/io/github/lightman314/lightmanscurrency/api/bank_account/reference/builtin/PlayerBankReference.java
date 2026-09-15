package io.github.lightman314.lightmanscurrency.api.bank_account.reference.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.bank_account.reference.BankReferenceType;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.features.api_impl.data.PlayerBankDataCache;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.UUID;

public final class PlayerBankReference extends BankReference {

    public static final MapCodec<PlayerBankReference> MAP_CODEC = UUIDUtil.CODEC.xmap(PlayerBankReference::new,PlayerBankReference::getPlayer).fieldOf("player");
    public static final StreamCodec<ByteBuf,PlayerBankReference> STREAM_CODEC = UUIDUtil.STREAM_CODEC.map(PlayerBankReference::new,PlayerBankReference::getPlayer);

    public static final BankReferenceType<PlayerBankReference> TYPE = new BankReferenceType<>(MAP_CODEC,STREAM_CODEC);

    private final UUID player;
    public UUID getPlayer() { return this.player; }
    private PlayerBankReference(UUID player) { this.player = player; }

    public static BankReference of(UUID player) { return new PlayerBankReference(player); }
    @Nullable
    public static BankReference of(@Nullable PlayerReference player) { return player != null ? new PlayerBankReference(player.id) : null; }
    public static BankReference of(Player player) { return of(player.getUUID()).setSidedContext(ISidedContext.wrap(player)); }

    @Override
    public BankReferenceType<?> getType() { return TYPE; }
    @Override
    public int sortPriority() { return 1000000; }
    @Nullable
    @Override
    public BankAccount get() {
        PlayerBankDataCache cache = PlayerBankDataCache.TYPE.get(this);
        return cache.getAccount(this.player);
    }

    @Override
    public boolean isSalaryTarget(PlayerReference player) { return this.player.equals(player.id); }
    @Override
    public boolean allowedAccess(PlayerReference player) { return this.player.equals(player.id); }
    @Override
    public int salaryPermission(PlayerReference player) { return this.player.equals(player.id) ? Integer.MAX_VALUE : 0; }

    @Nullable
    @Override
    public IconData getIcon() { return ItemIcon.of(ItemHelper.skullForPlayer(this.player)); }

    @Override
    protected boolean equals(BankReference other) { return other instanceof PlayerBankReference pr && pr.player.equals(this.player); }
    @Override
    protected int hash() { return this.player.hashCode(); }

}
