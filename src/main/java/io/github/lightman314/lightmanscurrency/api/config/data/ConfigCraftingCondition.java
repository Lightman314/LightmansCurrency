package io.github.lightman314.lightmanscurrency.api.config.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.BooleanOption;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.conditions.ICondition;

public class ConfigCraftingCondition implements ICondition {

    public static final MapCodec<ConfigCraftingCondition> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                            Identifier.CODEC.fieldOf("fileID").forGetter(c -> c.fileID),
                            Codec.STRING.fieldOf("option").forGetter(c -> c.optionPath))
                    .apply(builder,ConfigCraftingCondition::new));

    private final Identifier fileID;
    private final String optionPath;

    private ConfigCraftingCondition(Identifier fileID, String optionPath)
    {
        this.fileID = fileID;
        this.optionPath = optionPath;
    }

    public static ConfigCraftingCondition of(Identifier fileName,String optionPath) { return new ConfigCraftingCondition(fileName,optionPath); }
    public static ConfigCraftingCondition of(BooleanOption option) {
        String path = null;
        ConfigFile file = option.getFile();
        if(file == null)
            throw new IllegalArgumentException("Config Option was not attached to a config file!");
        String fullKey = option.getFullName();
        if(fullKey == null)
            throw new IllegalArgumentException("Config Option was not a member of the config file!");
        return of(file.getFileID(),fullKey);
    }

    @Override
    public boolean test(IContext context) {
        ConfigFile file = ConfigFile.lookupFile(this.fileID);
        if(file != null)
        {
            ConfigOption<?> option = file.getAllOptions().get(this.optionPath);
            if(option != null && option.get() instanceof Boolean bool)
                return bool;
        }
        return false;
    }

    @Override
    public MapCodec<ConfigCraftingCondition> codec() { return CODEC; }

}