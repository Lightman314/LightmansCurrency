package io.github.lightman314.lightmanscurrency.common.menus.validation.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidatorType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;
import java.util.function.Supplier;

public final class SimpleValidator extends MenuValidator {

    public static final MenuValidatorType TYPE = new Type();

    public static final MenuValidator NULL = new SimpleValidator(p -> true);

    private final Function<Player,Boolean> test;

    private SimpleValidator(Supplier<Boolean> test) { this(p -> test.get()); }
    private SimpleValidator(Function<Player,Boolean> test) { super(TYPE); this.test = test; }

    public static MenuValidator of(Supplier<Boolean> test) { return new SimpleValidator(test); }
    public static MenuValidator of(Function<Player,Boolean> test) { return new SimpleValidator(test); }

    @Override
    protected void encodeAdditional(FriendlyByteBuf buffer) { }

    @Override
    protected void saveAdditional(CompoundTag tag) { }

    @Override
    public boolean stillValid(Player player) { try{ return this.test.apply(player); } catch (Throwable t) { return false; } }

    private static class Type extends MenuValidatorType
    {
        protected Type() { super(LightmansCurrency.id("null")); }
        @Override
        public MenuValidator decode(FriendlyByteBuf buffer) { return NULL; }
        @Override
        public MenuValidator load(CompoundTag tag) { return NULL; }
    }

}
