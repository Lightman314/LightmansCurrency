package io.github.lightman314.lightmanscurrency.common.menus.validation;

import io.github.lightman314.lightmanscurrency.common.menus.validation.types.SimpleValidator;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class EasyMenu extends AbstractContainerMenu implements IClientTracker {

    public final Player player;
    protected final Inventory inventory;
    @Nonnull
    public final RegistryAccess registryAccess() { return this.player.registryAccess(); }

    @Override
    public boolean isClient() { return this.player.level().isClientSide; }

    private final List<MenuValidator> validators = new ArrayList<>();


    protected EasyMenu(@Nullable MenuType<?> type, int id, Inventory inventory) { super(type, id); this.player = inventory.player; this.inventory = inventory; }
    protected EasyMenu(@Nullable MenuType<?> type, int id, Inventory inventory, MenuValidator validator) {
        this(type,id, inventory);
        if(validator != null)
            this.addValidator(validator);
    }

    public final void addValidator(@Nonnull Supplier<Boolean> stillValid) { this.addValidator(SimpleValidator.of(stillValid)); }
    public final void addValidator(@Nonnull Function<Player,Boolean> stillValid) { this.addValidator(SimpleValidator.of(stillValid)); }
    public final void addValidator(@Nonnull MenuValidator validator) {
        if(!this.validators.contains(validator))
            this.validators.add(validator);
    }

    public final void removeValidator(@Nonnull MenuValidator validator) { this.validators.remove(validator); }

    @Override
    public final boolean stillValid(@Nonnull Player player) { this.onValidationTick(player); return this.validators.stream().allMatch(v -> v.stillValid(player)); }

    protected void onValidationTick(@Nonnull Player player) {}

    public static Consumer<RegistryFriendlyByteBuf> nullEncoder() { return SimpleValidator.NULL::encode; }
    public static Consumer<RegistryFriendlyByteBuf> encoder(@Nonnull MenuValidator validator) { return validator::encode; }
    public static Consumer<RegistryFriendlyByteBuf> encoder(@Nonnull Consumer<RegistryFriendlyByteBuf> otherEncoder, @Nonnull MenuValidator validator) { return d -> { otherEncoder.accept(d); validator.encode(d); }; }
    public static Consumer<RegistryFriendlyByteBuf> encoder(@Nonnull BlockPos pos, @Nonnull MenuValidator validator) { return d -> { d.writeBlockPos(pos); validator.encode(d); }; }

}
