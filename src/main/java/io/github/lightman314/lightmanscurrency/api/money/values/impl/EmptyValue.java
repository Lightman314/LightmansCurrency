package io.github.lightman314.lightmanscurrency.api.money.values.impl;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValueType;
import io.github.lightman314.lightmanscurrency.api.money.values.parsing.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Range;

import java.util.function.Consumer;

public final class EmptyValue extends MoneyValue {

    public static final EmptyValue EMPTY_INSTANCE = new EmptyValue();
    public static final EmptyValue FREE_INSTANCE = new EmptyValue();

    public static final MoneyValueType<EmptyValue> EMPTY_TYPE = new MoneyValueType<>(MapCodec.unit(EMPTY_INSTANCE),StreamCodec.unit(EMPTY_INSTANCE),Parser.INSTANCE);
    public static final MoneyValueType<EmptyValue> FREE_TYPE = new MoneyValueType<>(MapCodec.unit(FREE_INSTANCE),StreamCodec.unit(FREE_INSTANCE),Parser.INSTANCE);

    private EmptyValue() {}

    @Override
    public MoneyValueType<?> getType() { return this.isFree() ? FREE_TYPE : EMPTY_TYPE; }

    @Override
    protected MoneyKey generateKey() { return MoneyKey.create(this.getType()); }

    @Override
    @Range(from = 0, to = Long.MAX_VALUE)
    public long getInternalValue() { return 0; }

    @Override
    public Component getText(Component emptyText) { return emptyText; }

    @Override
    public void spawnInWorld(Consumer<ItemStack> itemSpawner,OwnerHolder owner) { }

    @Override
    protected MoneyValue copyWithInternalValue(long value) { return this; }

    private static final class Parser extends MoneyValueParser<EmptyValue>
    {
        private static final MoneyValueParser<EmptyValue> INSTANCE = new Parser();

        private Parser() { super("null"); }

        @Override
        protected MoneyValue parseValueArgument(StringReader reader) { return reader.getRemaining().equalsIgnoreCase("free") ? MoneyValue.free() : MoneyValue.empty(); }

        @Override
        protected String writeValueArgument(EmptyValue value) { return value.isFree() ? "free" : "empty"; }
    }


}
