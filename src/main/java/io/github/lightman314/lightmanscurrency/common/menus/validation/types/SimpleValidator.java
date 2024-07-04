package io.github.lightman314.lightmanscurrency.common.menus.validation.types;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidatorType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SimpleValidator extends MenuValidator {

    public static final MenuValidatorType TYPE = new Type();

    public static final MenuValidator NULL = new SimpleValidator(p -> true);

    private final Function<Player,Boolean> test;

    private SimpleValidator(@Nonnull Supplier<Boolean> test) { super(TYPE); this.test = p -> test.get(); }
    private SimpleValidator(@Nonnull Function<Player,Boolean> test) { super(TYPE); this.test = test; }

    public static MenuValidator of(@Nonnull Supplier<Boolean> test) { return new SimpleValidator(test); }
    public static MenuValidator of(@Nonnull Function<Player,Boolean> test) { return new SimpleValidator(test); }

    @Override
    protected void encodeAdditional(@Nonnull FriendlyByteBuf buffer) { }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) { }

    @Override
    public boolean stillValid(@Nonnull Player player) { try{ return this.test.apply(player); } catch (Throwable t) { return false; } }

    private static class Type extends MenuValidatorType
    {
        protected Type() { super(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "null")); }
        @Nonnull
        @Override
        public MenuValidator decode(@Nonnull FriendlyByteBuf buffer) { return NULL; }
        @Nonnull
        @Override
        public MenuValidator load(@Nonnull CompoundTag tag) { return NULL; }
    }

}
