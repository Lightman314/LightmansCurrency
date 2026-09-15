package io.github.lightman314.lightmanscurrency.api.money.values.source.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.MoneyValueOption;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.source.MoneyValueSource;
import io.github.lightman314.lightmanscurrency.api.money.values.source.MoneyValueSourceType;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public class ConfiguredSource extends MoneyValueSource {

    private static final MapCodec<ConfiguredSource> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Identifier.CODEC.fieldOf("fileID").forGetter(s -> s.file),
            Codec.STRING.fieldOf("option").forGetter(s -> s.option)
    ).apply(builder,ConfiguredSource::new));
    private static final StreamCodec<ByteBuf,ConfiguredSource> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,s -> s.file,
            ByteBufCodecs.STRING_UTF8,s -> s.option,
            ConfiguredSource::new);

    public static final MoneyValueSourceType<ConfiguredSource> TYPE = new MoneyValueSourceType<>(MAP_CODEC,STREAM_CODEC);

    public static MoneyValueSource configured(MoneyValueOption option) {
        Identifier file = option.getFile().getFileID();
        String optionLocation = option.getFullName();
        return new ConfiguredSource(file,optionLocation,option);
    }

    private final Identifier file;
    private final String option;
    private boolean lookup = true;
    private MoneyValueOption cache = null;
    private ConfiguredSource(Identifier file,String option) {
        this.file = file;
        this.option = option;
    }
    private ConfiguredSource(Identifier file,String option,MoneyValueOption actualOption) {
        this.file = file;
        this.option = option;
        this.cache = actualOption;
        this.lookup = this.cache == null;
    }

    @Override
    public MoneyValue getMoneyValue() {
        if(this.lookup) {
            this.lookup = false;
            ConfigFile file = ConfigFile.lookupFile(this.file);
            if(file != null) {
                ConfigOption<?> o = file.getAllOptions().get(this.option);
                if(o instanceof MoneyValueOption result)
                    this.cache = result;
            }
        }
        return this.cache == null ? MoneyValue.empty() : this.cache.get();
    }

    @Override
    public MoneyValueSourceType<?> getType() { return TYPE; }

    @Override
    protected int hash() { return Objects.hash(this.file,this.option); }
    @Override
    protected boolean equals(MoneyValueSource source) { return source instanceof ConfiguredSource cs && cs.file.equals(this.file) && cs.option.equals(this.option); }

}
