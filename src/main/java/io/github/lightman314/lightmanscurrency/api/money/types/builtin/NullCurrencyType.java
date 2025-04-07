package io.github.lightman314.lightmanscurrency.api.money.types.builtin;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.capability.money.CapabilityMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.capability.money.IMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.types.builtin.other.ContainerMoneyHandlerWrapper;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class NullCurrencyType extends CurrencyType {

    public static final ResourceLocation TYPE = VersionUtil.lcResource("null");
    public static final NullCurrencyType INSTANCE = new NullCurrencyType();

    private NullCurrencyType() { super(TYPE); }

    @Override
    protected MoneyValue sumValuesInternal(List<MoneyValue> values) { return MoneyValue.empty(); }

    @Nullable
    @Override
    public IPlayerMoneyHandler createMoneyHandlerForPlayer(Player player) { return null; }

    @Nullable
    @Override
    public IMoneyHandler createMoneyHandlerForContainer(Container container, Consumer<ItemStack> overflowHandler, IClientTracker tracker) { return new ContainerMoneyHandlerWrapper(container,tracker); }

    @Override
    public MoneyValue loadMoneyValue(CompoundTag valueTag) {
        if(valueTag.contains("Free", Tag.TAG_BYTE) && valueTag.getBoolean("Free"))
            return MoneyValue.free();
        return MoneyValue.empty();
    }
    @Override
    public MoneyValue loadMoneyValueJson(JsonObject json) { return GsonHelper.getAsBoolean(json, "Free", false) ? MoneyValue.free() : MoneyValue.empty(); }

    
    @Override
    public MoneyValueParser getValueParser() { return DefaultValueParser.INSTANCE; }

    @Override
    public List<Object> getInputHandlers(@Nullable Player player) { return new ArrayList<>(); }

    @Override
    public boolean allowItemInMoneySlot(Player player, ItemStack item) {
        return item.getCapability(CapabilityMoneyHandler.MONEY_HANDLER_ITEM) != null;
    }

    private static class DefaultValueParser extends MoneyValueParser {

        private static final SimpleCommandExceptionType NOT_EMPTY_OR_FREE_EXCEPTION = new SimpleCommandExceptionType(LCText.ARGUMENT_MONEY_VALUE_NOT_EMPTY_OR_FREE.get());
        private static final DefaultValueParser INSTANCE = new DefaultValueParser();

        protected DefaultValueParser() { super("null"); }

        @Override
        protected MoneyValue parseValueArgument(StringReader reader) throws CommandSyntaxException {
            String text = reader.getRemaining();
            if(text.equalsIgnoreCase("free"))
                return MoneyValue.free();
            if(text.equalsIgnoreCase("empty"))
                return MoneyValue.empty();
            throw NOT_EMPTY_OR_FREE_EXCEPTION.createWithContext(reader);
        }

        @Override
        protected String writeValueArgument(MoneyValue value) {
            if(value.isFree())
                return "free";
            if(value.isEmpty())
                return "empty";
            return null;
        }

    }

}
