package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BankUpgradeData {

    public static final BankUpgradeData DEFAULT = new BankUpgradeData(true,MoneyValue.empty(),Optional.empty(),Optional.empty(),ImmutableList.of());
    public static final Codec<BankUpgradeData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(Codec.BOOL.fieldOf("deposit").forGetter(d -> d.depositMode),
                    MoneyValue.CODEC.fieldOf("moneyLimit").forGetter(d -> d.moneyLimit),
                    PlayerReference.CODEC.optionalFieldOf("player").forGetter(d -> Optional.ofNullable(d.player)),
                    BankReference.CODEC.optionalFieldOf("account").forGetter(d -> Optional.ofNullable(d.targetAccount)),
                    ItemStack.CODEC.listOf().fieldOf("overflowItems").forGetter(d -> d.overflowItems))
                    .apply(builder,BankUpgradeData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,BankUpgradeData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,d -> d.depositMode,
            MoneyValue.STREAM_CODEC,d -> d.moneyLimit,
            ByteBufCodecs.optional(PlayerReference.STREAM_CODEC),d -> Optional.ofNullable(d.player),
            ByteBufCodecs.optional(BankReference.STREAM_CODEC),d -> Optional.ofNullable(d.targetAccount),
            ItemStack.LIST_STREAM_CODEC,d -> d.overflowItems,
            BankUpgradeData::new);

    public final boolean depositMode;
    public final MoneyValue moneyLimit;
    @Nullable
    public final PlayerReference player;
    @Nullable
    public final BankReference targetAccount;
    private final List<ItemStack> overflowItems;
    public List<ItemStack> getOverflowItems() { return ItemHandlerUtil.copyList(this.overflowItems); }

    public boolean canInteract() { return this.targetAccount != null && this.player != null && this.overflowItems.isEmpty(); }

    private BankUpgradeData(boolean depositMode, MoneyValue moneyLimit, Optional<PlayerReference> player, Optional<BankReference> targetAccount, List<ItemStack> overflowItems) { this(depositMode,moneyLimit,player.orElse(null),targetAccount.orElse(null),ImmutableList.copyOf(overflowItems)); }
    public BankUpgradeData(boolean depositMode, MoneyValue moneyLimit, @Nullable PlayerReference player, @Nullable BankReference targetAccount, List<ItemStack> overflowItems) { this.depositMode = depositMode; this.moneyLimit = Objects.requireNonNullElse(moneyLimit,MoneyValue.empty()); this.player = player; this.targetAccount = targetAccount; this.overflowItems = overflowItems; }

    
    public BankUpgradeData setDepositMode(boolean depositMode) { return new BankUpgradeData(depositMode,this.moneyLimit,this.player,this.targetAccount,this.overflowItems); }
    
    public BankUpgradeData setMoneyLimit(MoneyValue moneyLimit) { return new BankUpgradeData(this.depositMode,moneyLimit,this.player,this.targetAccount,this.overflowItems); }
    
    public BankUpgradeData setBankAccount(PlayerReference player,BankReference targetAccount) { return new BankUpgradeData(this.depositMode,this.moneyLimit,player,targetAccount, this.overflowItems); }
    
    public BankUpgradeData setOverflowItems(List<ItemStack> overflowItems) { return new BankUpgradeData(this.depositMode,this.moneyLimit, this.player, this.targetAccount, ItemHandlerUtil.copyList(overflowItems)); }

    @Override
    public int hashCode() { return Objects.hash(this.depositMode,this.moneyLimit,this.player,this.targetAccount); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BankUpgradeData other)
            return other.depositMode == this.depositMode && other.moneyLimit.equals(this.moneyLimit) && Objects.equals(this.player,other.player) && Objects.equals(this.targetAccount,other.targetAccount) && this.overflowItems.equals(other.overflowItems);
        return false;
    }

}
