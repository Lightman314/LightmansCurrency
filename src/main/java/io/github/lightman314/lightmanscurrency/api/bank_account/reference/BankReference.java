package io.github.lightman314.lightmanscurrency.api.bank_account.reference;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.bank_account.BankAccount;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.resource.SortableMoneyResourceHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
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
    public final int salaryPermission(Player player) { return LCApi.isInAdminMode(player) ? Integer.MAX_VALUE : this.salaryPermission(PlayerReference.of(player)); }

    public boolean canPersist(Player player) { return true; }

    public final CompoundTag save() { return (CompoundTag)CODEC.encodeStart(NbtOps.INSTANCE,this).getOrThrow(); }
    public static BankReference load(CompoundTag tag) { return CODEC.decode(NbtOps.INSTANCE,tag).getOrThrow().getFirst(); }

    @Override
    public MoneyResourceHandler getMoneyResourceHandler() {
        return null;
    }

    //@Nullable
    //public abstract IconData getIcon();

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof BankReference br)
            return br.save().equals(this.save());
        return false;
    }

    @Override
    public int hashCode() { return this.save().hashCode(); }

}