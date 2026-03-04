package io.github.lightman314.lightmanscurrency.api.money.bank.reference;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.misc.ISidedObject;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.TeamBankReference;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.money.capability.MoneyHolder;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public abstract class BankReference extends MoneyHolder.Slave implements ISidedObject {

    public static final Codec<BankReference> CODEC = Codec.withAlternative(
            //Intended Codec
            LCRegistries.BANK_REFERENCE.byNameCodec().dispatch(BankReference::getType,BankReferenceType::codec),
            //Fallback Codec for old data
            CodecHelper.oldValueLoader(BankReference::loadOldData,"Bank Reference"));
    public static final StreamCodec<RegistryFriendlyByteBuf,BankReference> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.BANK_REFERENCE_KEY)
            .dispatch(BankReference::getType,BankReferenceType::streamCodec);

    private Supplier<Boolean> isClient = () -> false;
    public boolean isClient() { return this.isClient.get(); }

    public BankReference flagAsClient() { return this.flagAsClient(true); }
    public BankReference flagAsClient(boolean isClient) { this.isClient = () -> isClient; return this; }
    public BankReference flagAsClient(IClientTracker parent) { this.isClient = parent::isClient; return this; }


    public abstract BankReferenceType<?> getType();

    public final boolean isValid() { return this.get() != null; }
    @Nullable
    public abstract IBankAccount get();

    public int sortPriority() { return 0; }

    public abstract boolean isSalaryTarget(PlayerReference player);
    public boolean isSalaryTarget(Player player) { return this.isSalaryTarget(PlayerReference.of(player)); }

    public abstract boolean allowedAccess(PlayerReference player);
    public abstract boolean allowedAccess(Player player);

    /**
     * Permissions Levels:<br>
     * 0- Cannot view or edit any salaries
     * 1- Can view all salaries
     * 3- Can edit all salaries
     */
    public abstract int salaryPermission(PlayerReference player);
    /**
     * Permissions Levels:<br>
     * 0- Cannot view or edit any salaries
     * 1- Can only view salaries with their personal account as a target
     * 2- Can view all salaries
     * 3- Can view and edit all salaries
     */
    public final int salaryPermission(Player player) { return LCAdminMode.isAdminPlayer(player) ? Integer.MAX_VALUE : this.salaryPermission(PlayerReference.of(player)); }

    public boolean canPersist(Player player) { return true; }

    public final CompoundTag save() { return (CompoundTag)CODEC.encodeStart(NbtOps.INSTANCE,this).getOrThrow(); }
    public static BankReference load(CompoundTag tag) { return CODEC.decode(NbtOps.INSTANCE,tag).getOrThrow().getFirst(); }

    private static BankReference loadOldData(CompoundTag tag)
    {
        if(tag.contains("Type"))
        {
            BankReferenceType<?> type = LCRegistries.BANK_REFERENCE.get(VersionUtil.parseResource(tag.getString("Type")));
            if(type != null)
                return type.loadOldData(tag);
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

    @Override
    @Nullable
    protected IMoneyHolder getParent() { return this.get(); }

    @Nullable
    public abstract IconData getIcon();

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BankReference br)
            return br.save().equals(this.save());
        return false;
    }

    @Override
    public int hashCode() { return this.save().hashCode(); }

}
