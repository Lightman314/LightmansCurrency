package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import javax.annotation.Nonnull;
import java.util.Objects;

public record ExchangeUpgradeData(boolean exchangeWhileOpen, @Nonnull String exchangeCommand) {

    public static final ExchangeUpgradeData DEFAULT = new ExchangeUpgradeData(true,"");
    public static final Codec<ExchangeUpgradeData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.BOOL.fieldOf("ExchangeWhileOpen").forGetter(ExchangeUpgradeData::exchangeWhileOpen),
                    Codec.STRING.fieldOf("ExchangeCommand").forGetter(ExchangeUpgradeData::exchangeCommand)
            ).apply(builder,ExchangeUpgradeData::new)
    );

    @Nonnull
    public ExchangeUpgradeData withExchangeWhileOpen(boolean newValue) { return new ExchangeUpgradeData(newValue,this.exchangeCommand); }
    public ExchangeUpgradeData withExchangeCommand(@Nonnull String newValue) { return new ExchangeUpgradeData(this.exchangeWhileOpen,newValue); }

    @Override
    public int hashCode() { return Objects.hash(this.exchangeWhileOpen,this.exchangeCommand); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ExchangeUpgradeData other)
            return other.exchangeWhileOpen == this.exchangeWhileOpen && other.exchangeCommand.equals(this.exchangeCommand);
        return false;
    }

}
