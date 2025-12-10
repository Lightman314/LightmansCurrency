package io.github.lightman314.lightmanscurrency.api.money.bank.reference;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.ISidedObject;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.BankAPI;
import io.github.lightman314.lightmanscurrency.api.money.bank.IBankAccount;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.PlayerBankReference;
import io.github.lightman314.lightmanscurrency.api.money.bank.reference.builtin.TeamBankReference;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.MoneyHolder;
import io.github.lightman314.lightmanscurrency.common.player.LCAdminMode;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class BankReference extends MoneyHolder.Slave implements ISidedObject {

    private Supplier<Boolean> isClient = () -> false;
    public boolean isClient() { return this.isClient.get(); }

    public BankReference flagAsClient() { return this.flagAsClient(true); }
    public BankReference flagAsClient(boolean isClient) { this.isClient = () -> isClient; return this; }
    public BankReference flagAsClient(IClientTracker parent) { this.isClient = parent::isClient; return this; }

    protected final BankReferenceType type;
    protected BankReference(BankReferenceType type) { this.type = type; }

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

    
    public final CompoundTag save()
    {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        tag.putString("Type", this.type.id.toString());
        return tag;
    }

    protected abstract void saveAdditional(CompoundTag tag);

    public final void encode(FriendlyByteBuf buffer)
    {
        buffer.writeUtf(this.type.id.toString());
        this.encodeAdditional(buffer);
    }

    protected abstract void encodeAdditional(FriendlyByteBuf buffer);

    @Nullable
    public static BankReference load(CompoundTag tag)
    {
        if(tag.contains("Type"))
        {
            BankReferenceType type = BankAPI.getApi().GetReferenceType(VersionUtil.parseResource(tag.getString("Type")));
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

    public static BankReference decode(FriendlyByteBuf buffer)
    {
        BankReferenceType type = BankAPI.getApi().GetReferenceType(VersionUtil.parseResource(buffer.readUtf()));
        if(type != null)
            return type.decode(buffer);
        else
            LightmansCurrency.LogWarning("No Bank Reference Type '" + type + "' could be decoded.");
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
