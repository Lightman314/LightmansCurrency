package io.github.lightman314.lightmanscurrency.common.menus.validation;

import io.github.lightman314.lightmanscurrency.common.menus.validation.types.SimpleValidator;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class EasyMenu extends AbstractContainerMenu implements IClientTracker {

    public final Player player;
    protected final Inventory inventory;
    public final HolderLookup.Provider registryAccess() { return this.player.registryAccess(); }

    @Override
    public boolean isClient() { return this.player.level().isClientSide; }

    private final List<MenuValidator> validators = new ArrayList<>();


    protected EasyMenu(@Nullable MenuType<?> type, int id, Inventory inventory) { super(type, id); this.player = inventory.player; this.inventory = inventory; }
    protected EasyMenu(@Nullable MenuType<?> type, int id, Inventory inventory, MenuValidator validator) {
        this(type,id, inventory);
        if(validator != null)
            this.addValidator(validator);
    }

    public final void addValidator(BooleanSupplier stillValid) { this.addValidator(SimpleValidator.of(stillValid)); }
    public final void addValidator(BooleanSupplier stillValid,Runnable onFailure) { this.addValidator(SimpleValidator.of(stillValid,onFailure)); }
    public final void addValidator(Function<Player,Boolean> stillValid) { this.addValidator(SimpleValidator.of(stillValid)); }
    public final void addValidator(Function<Player,Boolean> stillValid,Runnable onFailure) { this.addValidator(SimpleValidator.of(stillValid,onFailure)); }
    public final void addValidator(MenuValidator validator) {
        if(!this.validators.contains(validator))
            this.validators.add(validator);
    }

    public final void removeValidator(MenuValidator validator) { this.validators.remove(validator); }

    @Override
    public final boolean stillValid(Player player) { this.onValidationTick(player); return this.validators.stream().allMatch(v -> v.stillValid(player)); }

    protected void onValidationTick(Player player) {}

    protected void clearContainer(Player player, IItemHandler container) {
        if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer)player).hasDisconnected()) {
            for (int j = 0; j < container.getSlots(); j++) {
                player.drop(container.extractItem(j,Integer.MAX_VALUE,false), false);
            }
        } else {
            for (int i = 0; i < container.getSlots(); i++) {
                Inventory inventory = player.getInventory();
                if (inventory.player instanceof ServerPlayer) {
                    inventory.placeItemBackInInventory(container.extractItem(i,Integer.MAX_VALUE,false));
                }
            }
        }
    }

    public static Consumer<RegistryFriendlyByteBuf> nullEncoder() { return SimpleValidator.NULL::encode; }
    public static Consumer<RegistryFriendlyByteBuf> encoder(MenuValidator validator) { return validator::encode; }
    public static Consumer<RegistryFriendlyByteBuf> encoder(Consumer<RegistryFriendlyByteBuf> otherEncoder, MenuValidator validator) { return d -> { otherEncoder.accept(d); validator.encode(d); }; }
    public static Consumer<RegistryFriendlyByteBuf> encoder(BlockPos pos, MenuValidator validator) { return d -> { d.writeBlockPos(pos); validator.encode(d); }; }

}
