package io.github.lightman314.lightmanscurrency.common.menus.validation;

import io.github.lightman314.lightmanscurrency.common.menus.validation.types.SimpleValidator;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class EasyMenu extends AbstractContainerMenu implements IClientTracker {

    public final Player player;

    @Override
    public boolean isClient() { return this.player.level().isClientSide; }

    private final List<MenuValidator> validators = new ArrayList<>();


    protected EasyMenu(@Nullable MenuType<?> type, int id, Inventory inventory) { super(type, id); this.player = inventory.player; }
    protected EasyMenu(@Nullable MenuType<?> type, int id, Inventory inventory, MenuValidator validator) {
        this(type,id, inventory);
        if(validator != null)
            this.addValidator(validator);
    }

    public final void addValidator(@Nonnull NonNullSupplier<Boolean> stillValid) { this.addValidator(SimpleValidator.of(stillValid)); }
    public final void addValidator(@Nonnull NonNullFunction<Player,Boolean> stillValid) { this.addValidator(SimpleValidator.of(stillValid)); }
    public final void addValidator(@Nonnull MenuValidator validator) {
        if(!this.validators.contains(validator))
            this.validators.add(validator);
    }

    public final void removeValidator(@Nonnull MenuValidator validator) { this.validators.remove(validator); }

    @Override
    public final boolean stillValid(@Nonnull Player player) { this.onValidationTick(player); return this.validators.stream().allMatch(v -> v.stillValid(player)); }

    protected void onValidationTick(@Nonnull Player player) {}

    public static Consumer<FriendlyByteBuf> nullEncoder() { return SimpleValidator.NULL::encode; }
    public static Consumer<FriendlyByteBuf> encoder(@Nonnull MenuValidator validator) { return validator::encode; }
    public static Consumer<FriendlyByteBuf> encoder(@Nonnull Consumer<FriendlyByteBuf> otherEncoder, @Nonnull MenuValidator validator) { return d -> { otherEncoder.accept(d); validator.encode(d); }; }
    public static Consumer<FriendlyByteBuf> encoder(@Nonnull BlockPos pos, @Nonnull MenuValidator validator) { return d -> { d.writeBlockPos(pos); validator.encode(d); }; }

}
