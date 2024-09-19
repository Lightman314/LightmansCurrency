package io.github.lightman314.lightmanscurrency.api.money.types.builtin;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.capability.money.CapabilityMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.misc.ISidedObject;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.other.ContainerMoneyHandlerWrapper;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class NullCurrencyType extends CurrencyType {

    public static final ResourceLocation TYPE = ResourceLocation.fromNamespaceAndPath(MoneyAPI.MODID, "null");
    public static final NullCurrencyType INSTANCE = new NullCurrencyType();

    private NullCurrencyType() { super(TYPE); }

    @Nonnull
    @Override
    protected MoneyValue sumValuesInternal(@Nonnull List<MoneyValue> values) { return MoneyValue.empty(); }

    @Nullable
    @Override
    public IPlayerMoneyHandler createMoneyHandlerForPlayer(@Nonnull Player player) { return null; }

    @Nullable
    @Override
    public IMoneyHandler createMoneyHandlerForContainer(@Nonnull Container container, @Nonnull Consumer<ItemStack> overflowHandler, @Nonnull IClientTracker tracker) { return new ContainerMoneyHandlerWrapper(container,tracker); }

    @Override
    public MoneyValue loadMoneyValue(@Nonnull CompoundTag valueTag) {
        if(valueTag.contains("Free", Tag.TAG_BYTE) && valueTag.getBoolean("Free"))
            return MoneyValue.free();
        return MoneyValue.empty();
    }
    @Override
    public MoneyValue loadMoneyValueJson(@Nonnull JsonObject json) { return GsonHelper.getAsBoolean(json, "Free", false) ? MoneyValue.free() : MoneyValue.empty(); }

    @Nonnull
    @Override
    public MoneyValueParser getValueParser() { return DefaultValueParser.INSTANCE; }

    @Override
    public List<Object> getInputHandlers(@Nullable Player player) { return new ArrayList<>(); }

    @Override
    public boolean allowItemInMoneySlot(@Nonnull Player player, @Nonnull ItemStack item) {
        return item.getCapability(CapabilityMoneyHandler.MONEY_HANDLER_ITEM) != null;
    }

    private static class DefaultValueParser extends MoneyValueParser {

        private static final SimpleCommandExceptionType NOT_EMPTY_OR_FREE_EXCEPTION = new SimpleCommandExceptionType(LCText.ARGUMENT_MONEY_VALUE_NOT_EMPTY_OR_FREE.get());
        private static final DefaultValueParser INSTANCE = new DefaultValueParser();

        protected DefaultValueParser() { super("null"); }

        @Override
        protected MoneyValue parseValueArgument(@Nonnull StringReader reader) throws CommandSyntaxException {
            String text = reader.getRemaining();
            if(text.equalsIgnoreCase("free"))
                return MoneyValue.free();
            if(text.equalsIgnoreCase("empty"))
                return MoneyValue.empty();
            throw NOT_EMPTY_OR_FREE_EXCEPTION.createWithContext(reader);
        }

        @Override
        protected String writeValueArgument(@Nonnull MoneyValue value) {
            if(value.isFree())
                return "free";
            if(value.isEmpty())
                return "empty";
            return null;
        }

    }

}
