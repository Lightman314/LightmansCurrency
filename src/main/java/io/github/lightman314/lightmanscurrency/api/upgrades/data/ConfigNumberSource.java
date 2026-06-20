package io.github.lightman314.lightmanscurrency.api.upgrades.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.DoubleOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.FloatOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.IntOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.LongOption;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.Objects;

public class ConfigNumberSource extends NumberSource {

    private static final MapCodec<ConfigNumberSource> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Identifier.CODEC.fieldOf("file").forGetter(s -> s.file),
            Codec.STRING.fieldOf("option").forGetter(s -> s.key)
    ).apply(builder,ConfigNumberSource::new));
    private static final StreamCodec<ByteBuf,ConfigNumberSource> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,s -> s.file,
            ByteBufCodecs.STRING_UTF8,s -> s.key,
            ConfigNumberSource::new);
    public static final NumberSourceType<ConfigNumberSource> TYPE = new NumberSourceType<>(MAP_CODEC,STREAM_CODEC);

    private final Identifier file;
    private final String key;
    private boolean lookupOption = true;
    @Nullable
    private ConfigOption<? extends Number> option = null;
    public ConfigNumberSource(ConfigOption<? extends Number> option)
    {
        this.file = option.getFile().getFileID();
        this.key = option.getFullName();
        this.option = option;
        this.lookupOption = false;
    }
    private ConfigNumberSource(Identifier configFile,String optionKey)
    {
        this.file = configFile;
        this.key = optionKey;
    }

    @Override
    public NumberSourceType<?> getType() { return TYPE; }

    private void assertOptionLookup() {
        if(this.lookupOption)
        {
            this.lookupOption = false;
            ConfigFile f = ConfigFile.lookupFile(this.file);
            if(f != null)
            {
                ConfigOption<?> option = f.getAllOptions().get(this.key);
                if(option instanceof IntOption o)
                    this.option = o;
                if(option instanceof FloatOption o)
                    this.option = o;
                if(option instanceof LongOption o)
                    this.option = o;
                if(option instanceof DoubleOption o)
                    this.option = o;
            }
        }
    }

    @Override
    public double get() {
        this.assertOptionLookup();
        return this.option == null ? 0d : this.option.get().doubleValue();
    }

    @Override
    public long getLong() {
        this.assertOptionLookup();
        //Get the long directly if this is a long config option
        if(this.option instanceof LongOption o)
            return o.get();
        //Otherwise the value should be fine as-is, let it convert to a double and back
        return super.getLong();
    }

    @Override
    protected boolean equals(NumberSource source) {
        if(source instanceof ConfigNumberSource s)
            return this.file.equals(s.file) && this.key.equals(s.key);
        return false;
    }

    @Override
    public int hashCode() { return Objects.hash(this.file,this.key); }

}
