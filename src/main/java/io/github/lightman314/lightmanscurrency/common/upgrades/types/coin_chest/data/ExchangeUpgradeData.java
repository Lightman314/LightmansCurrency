package io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestExchangeUpgrade;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public record ExchangeUpgradeData(boolean exchangeWhileOpen, List<String> exchangeCommands) {

    public static final ExchangeUpgradeData DEFAULT = new ExchangeUpgradeData(true, ImmutableList.of());
    public static final Codec<ExchangeUpgradeData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.BOOL.fieldOf("ExchangeWhileOpen").forGetter(ExchangeUpgradeData::exchangeWhileOpen),
                    Codec.STRING.optionalFieldOf("ExchangeCommand").forGetter(d -> Optional.empty()),
                    Codec.STRING.sizeLimitedListOf(CoinChestExchangeUpgrade.MAX_COMMANDS).optionalFieldOf("ExchangeCommands").forGetter(d -> Optional.of(d.exchangeCommands()))
            ).apply(builder,ExchangeUpgradeData::load)
    );
    public static final StreamCodec<FriendlyByteBuf,ExchangeUpgradeData> STREAM_CODEC = StreamCodec.of(
            (b,d) -> {
                b.writeBoolean(d.exchangeWhileOpen);
                b.writeInt(d.exchangeCommands.size());
                for(String c : d.exchangeCommands)
                    b.writeUtf(c);
    },
            b -> {
                boolean exchangeWhileOpen = b.readBoolean();
                int count = b.readInt();
                List<String> list = new ArrayList<>();
                for(int i = 0; i < count; ++i)
                    list.add(b.readUtf());
                return new ExchangeUpgradeData(exchangeWhileOpen,ImmutableList.copyOf(list));
    });

    private static ExchangeUpgradeData load(boolean exchangeWhileOpen,Optional<String> oldCommand,Optional<List<String>> commandList)
    {
        return commandList.map(strings -> new ExchangeUpgradeData(exchangeWhileOpen, strings)).orElseGet(() -> oldCommand.map(s -> new ExchangeUpgradeData(exchangeWhileOpen, ImmutableList.of(s))).orElseGet(() -> new ExchangeUpgradeData(exchangeWhileOpen, ImmutableList.of())));
    }

    public boolean hasCommand(String exchangeCommand) { return this.exchangeCommands.contains(exchangeCommand); }

    public ExchangeUpgradeData withExchangeWhileOpen(boolean newValue) { return new ExchangeUpgradeData(newValue,this.exchangeCommands); }
    public ExchangeUpgradeData withAddedExchangeCommand(String newCommand) {
        if(this.hasCommand(newCommand) || newCommand.isBlank() || this.exchangeCommands.size() >= CoinChestExchangeUpgrade.MAX_COMMANDS)
            return this;
        List<String> list = new ArrayList<>(this.exchangeCommands);
        list.add(newCommand);
        return new ExchangeUpgradeData(this.exchangeWhileOpen,ImmutableList.copyOf(list));
    }
    public ExchangeUpgradeData withRemovedExchangeCommand(String removedCommand) {
        if(!this.hasCommand(removedCommand))
            return this;
        List<String> list = new ArrayList<>(this.exchangeCommands);
        list.remove(removedCommand);
        return new ExchangeUpgradeData(this.exchangeWhileOpen,ImmutableList.copyOf(list));
    }

    @Override
    public int hashCode() { return Objects.hash(this.exchangeWhileOpen,this.exchangeCommands); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ExchangeUpgradeData other)
            return other.exchangeWhileOpen == this.exchangeWhileOpen && other.exchangeCommands.equals(this.exchangeCommands);
        return false;
    }

}
