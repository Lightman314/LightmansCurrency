package io.github.lightman314.lightmanscurrency.api.bank_account.reference;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.SortableMoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.builtin.EmptyMoneyResource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class BankReference implements SortableMoneyResourceHandler.Deferred, ISidedContext.Mutable<BankReference> {

    public static final Codec<BankReference> CODEC = LCRegistries.Bank.REFERENCE_TYPE.byNameCodec().dispatch(BankReference::getType,BankReferenceType::codec);
    public static final StreamCodec<RegistryFriendlyByteBuf,BankReference> STREAM_CODEC = ByteBufCodecs.registry(LCRegistries.Bank.REFERENCE_TYPE_KEY)
            .dispatch(BankReference::getType,BankReferenceType::streamCodec);

    private Supplier<Boolean> isClient = () -> false;
    public boolean isClient() { return this.isClient.get(); }

    @Override
    public BankReference setSidedContext(ISidedContext parent) { this.isClient = parent::isClient; return this; }

    public abstract BankReferenceType<?> getType();

    public final boolean isValid() { return this.get() != null; }
    @Nullable
    public abstract BankAccount get();

    public int sortPriority() { return 0; }

    public abstract boolean isSalaryTarget(PlayerReference player);
    public boolean isSalaryTarget(Player player) { return this.isSalaryTarget(PlayerReference.of(player)); }

    public abstract boolean allowedAccess(PlayerReference player);
    public boolean allowedAccess(Player player) { return LCApi.isInAdminMode(player) || this.allowedAccess(PlayerReference.of(player)); }

    /**
     * Permissions Levels:<br>
     * 0- Cannot view or edit any salaries
     * 1- Can view all salaries
     * 2- Can edit all salaries
     */
    public abstract int salaryPermission(PlayerReference player);
    /**
     * Permissions Levels:<br>
     * 0- Cannot view or edit any salaries
     * 1- Can only view salaries with their personal account as a target
     * 2- Can view all salaries
     * 3- Can view and edit all salaries
     */
    public final int salaryPermission(Player player) { return LCApi.isInAdminMode(player) ? Integer.MAX_VALUE : this.salaryPermission(PlayerReference.of(player)); }

    public boolean canPersist(Player player) { return true; }

    @Nullable
    public abstract IconData getIcon();

    @Override
    public final MoneyResourceHandler getMoneyResourceHandler() {
        BankAccount account = this.get();
        return account == null ? EmptyMoneyResource.INSTANCE : account;
    }
    @Override
    public final int insertSortPriority() {
        BankAccount account = this.get();
        return account == null ? 0 : account.insertSortPriority();
    }
    @Override
    public final int extractSortPriorty() {
        BankAccount account = this.get();
        return account == null ? 0 : account.extractSortPriorty();
    }
    @Override
    public final Component getMoneyCategoryTitle() {
        BankAccount account = this.get();
        return account == null ? Component.empty() : account.getMoneyCategoryTitle();
    }
    @Override
    public final void formatTooltip(Consumer<Component> builder) {
        BankAccount account = this.get();
        if(account != null)
            account.formatTooltip(builder);
    }

    @Override
    public final boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(obj instanceof BankReference br)
            return this.equals(br);
        return false;
    }

    protected abstract boolean equals(BankReference other);

    @Override
    public final int hashCode() { return Objects.hash(this.getType(),this.hash()); }

    protected abstract int hash();

}