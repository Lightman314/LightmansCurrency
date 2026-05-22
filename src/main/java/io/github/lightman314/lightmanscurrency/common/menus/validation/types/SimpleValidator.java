package io.github.lightman314.lightmanscurrency.common.menus.validation.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidatorType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public final class SimpleValidator extends MenuValidator {

    public static final MenuValidatorType TYPE = new Type();

    public static final MenuValidator NULL = new SimpleValidator(p -> true,null);

    private final Function<Player,Boolean> test;
    @Nullable
    private final Runnable onFailure;

    private SimpleValidator(BooleanSupplier test,@Nullable Runnable onFailure) { this(p -> test.getAsBoolean(),onFailure); }
    private SimpleValidator(Function<Player,Boolean> test,@Nullable Runnable onFailure) {
        super(TYPE);
        this.test = test;
        this.onFailure = onFailure;
    }

    public static MenuValidator of(BooleanSupplier test) { return of(test,null); }
    public static MenuValidator of(BooleanSupplier test,Runnable onFailure) { return new SimpleValidator(test,onFailure); }
    public static MenuValidator of(Function<Player,Boolean> test) { return of(test,null); }
    public static MenuValidator of(Function<Player,Boolean> test,Runnable onFailure) { return new SimpleValidator(test,onFailure); }

    @Override
    protected void encodeAdditional(FriendlyByteBuf buffer) { }

    @Override
    protected void saveAdditional(CompoundTag tag) { }

    @Override
    public boolean stillValid(Player player) {
        try{
            boolean result = this.test.apply(player);
            if(!result && this.onFailure != null)
                this.onFailure.run();
            return result;
        } catch (Throwable t) { return false; } }

    private static class Type extends MenuValidatorType
    {
        protected Type() { super(LightmansCurrency.id("null")); }
        @Override
        public MenuValidator decode(FriendlyByteBuf buffer) { return NULL; }
        @Override
        public MenuValidator load(CompoundTag tag) { return NULL; }
    }

}
