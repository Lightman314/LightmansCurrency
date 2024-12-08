package io.github.lightman314.lightmanscurrency.api.config.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.BooleanOption;
import net.neoforged.neoforge.common.conditions.ICondition;

import javax.annotation.Nonnull;

public class ConfigCraftingCondition implements ICondition {

    public static final MapCodec<ConfigCraftingCondition> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Codec.STRING.fieldOf("fileName").forGetter(c -> c.fileName),
                    Codec.STRING.fieldOf("option").forGetter(c -> c.optionPath))
                    .apply(builder,ConfigCraftingCondition::new));

    private final String fileName;
    private final String optionPath;

    private ConfigCraftingCondition(@Nonnull String fileName, @Nonnull String optionPath)
    {
        this.fileName = fileName;
        this.optionPath = optionPath;
    }

    public static ConfigCraftingCondition of(@Nonnull String fileName,@Nonnull String optionPath) { return new ConfigCraftingCondition(fileName,optionPath); }
    public static ConfigCraftingCondition of(@Nonnull BooleanOption option) {
        String path = null;
        ConfigFile file = option.getFile();
        if(file == null)
            throw new IllegalArgumentException("Config Option was not attached to a config file!");
        for(var entry : file.getAllOptions().entrySet())
        {
            if(entry.getValue() == option)
                return of(file.getFileName(),entry.getKey());
        }
        throw new IllegalArgumentException("Config Option was not a member of the config file!");
    }

    @Override
    public boolean test(@Nonnull IContext context) {
        ConfigFile file = ConfigFile.lookupFile(this.fileName);
        if(file != null)
        {
            ConfigOption<?> option = file.getAllOptions().get(this.optionPath);
            if(option != null && option.get() instanceof Boolean bool)
                return bool;
        }
        return false;
    }

    @Nonnull
    @Override
    public MapCodec<ConfigCraftingCondition> codec() { return CODEC; }

}
