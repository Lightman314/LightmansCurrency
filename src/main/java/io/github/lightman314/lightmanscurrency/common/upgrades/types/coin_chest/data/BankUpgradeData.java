package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.codecs.LCCodecs;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.BankReference;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BankUpgradeData {

    public static final BankUpgradeData DEFAULT = new BankUpgradeData(true,MoneyValue.empty(),Optional.empty(),Optional.empty(),ImmutableList.of());
    public static final Codec<BankUpgradeData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(Codec.BOOL.fieldOf("deposit").forGetter(d -> d.depositMode),
                    LCCodecs.MONEY_VALUE.fieldOf("moneyLimit").forGetter(d -> d.moneyLimit),
                    LCCodecs.PLAYER_REFERENCE.optionalFieldOf("player").forGetter(d -> Optional.ofNullable(d.player)),
                    LCCodecs.BANK_REFERENCE.optionalFieldOf("account").forGetter(d -> Optional.ofNullable(d.targetAccount)),
                    ItemStack.CODEC.listOf().fieldOf("overflowItems").forGetter(d -> d.overflowItems))
                    .apply(builder,BankUpgradeData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf,BankUpgradeData> STREAM_CODEC = StreamCodec.of((b, d) -> {
        b.writeBoolean(d.depositMode);
        d.moneyLimit.encode(b);
        if(d.targetAccount == null)
            b.writeBoolean(false);
        else
        {
            b.writeBoolean(true);
            b.writeNbt(d.player.save());
            d.targetAccount.encode(b);
        }
        b.writeInt(d.overflowItems.size());
        for(ItemStack i : d.overflowItems)
            ItemStack.OPTIONAL_STREAM_CODEC.encode(b,i);
    }, b -> {
        boolean dm = b.readBoolean();
        MoneyValue val = MoneyValue.decode(b);
        BankReference tba = null;
        PlayerReference p = null;
        if(b.readBoolean())
        {
            p = PlayerReference.load(b.readNbt());
            tba = BankReference.decode(b);
        }
        List<ItemStack> list = new ArrayList<>();
        int count = b.readInt();
        for(int i = 0; i < count; ++i)
        {
            ItemStack item = ItemStack.OPTIONAL_STREAM_CODEC.decode(b);
            if(!item.isEmpty())
                list.add(item);
        }
        return new BankUpgradeData(dm,val,p,tba,ImmutableList.copyOf(list));
    });

    public final boolean depositMode;
    @Nonnull
    public final MoneyValue moneyLimit;
    @Nullable
    public final PlayerReference player;
    @Nullable
    public final BankReference targetAccount;
    @Nonnull
    private final List<ItemStack> overflowItems;
    public List<ItemStack> getOverflowItems() { return InventoryUtil.copyList(this.overflowItems); }

    public boolean canInteract() { return this.targetAccount != null && this.player != null && this.overflowItems.isEmpty(); }

    private BankUpgradeData(boolean depositMode, @Nonnull MoneyValue moneyLimit, @Nonnull Optional<PlayerReference> player, @Nonnull Optional<BankReference> targetAccount, @Nonnull List<ItemStack> overflowItems) { this(depositMode,moneyLimit,player.orElse(null),targetAccount.orElse(null),overflowItems); }
    public BankUpgradeData(boolean depositMode, @Nonnull MoneyValue moneyLimit, @Nullable PlayerReference player, @Nullable BankReference targetAccount, @Nonnull List<ItemStack> overflowItems) { this.depositMode = depositMode; this.moneyLimit = Objects.requireNonNullElse(moneyLimit,MoneyValue.empty()); this.player = player; this.targetAccount = targetAccount; this.overflowItems = overflowItems; }

    @Nonnull
    public BankUpgradeData setDepositMode(boolean depositMode) { return new BankUpgradeData(depositMode,this.moneyLimit,this.player,this.targetAccount,this.overflowItems); }
    @Nonnull
    public BankUpgradeData setMoneyLimit(@Nonnull MoneyValue moneyLimit) { return new BankUpgradeData(this.depositMode,moneyLimit,this.player,this.targetAccount,this.overflowItems); }
    @Nonnull
    public BankUpgradeData setBankAccount(@Nonnull PlayerReference player,@Nonnull BankReference targetAccount) { return new BankUpgradeData(this.depositMode,this.moneyLimit,player,targetAccount, this.overflowItems); }
    @Nonnull
    public BankUpgradeData setOverflowItems(@Nonnull List<ItemStack> overflowItems) { return new BankUpgradeData(this.depositMode,this.moneyLimit, this.player, this.targetAccount, InventoryUtil.copyList(overflowItems)); }

    @Override
    public int hashCode() { return Objects.hash(this.depositMode,this.moneyLimit,this.player,this.targetAccount); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BankUpgradeData other)
            return other.depositMode == this.depositMode && other.moneyLimit.equals(this.moneyLimit) && Objects.equals(this.player,other.player) && Objects.equals(this.targetAccount,other.targetAccount) && this.overflowItems.equals(other.overflowItems);
        return false;
    }

}
