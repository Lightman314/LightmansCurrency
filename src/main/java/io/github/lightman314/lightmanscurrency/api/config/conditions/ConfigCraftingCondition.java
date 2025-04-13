package io.github.lightman314.lightmanscurrency.api.config.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.BooleanOption;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ConfigCraftingCondition implements ICondition {

    public static final MapCodec<ConfigCraftingCondition> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Codec.STRING.optionalFieldOf("fileName").forGetter(c -> Optional.empty()),
                    ResourceLocation.CODEC.optionalFieldOf("fileID").forGetter(c -> Optional.of(c.fileID)),
                    Codec.STRING.fieldOf("option").forGetter(c -> c.optionPath))
                    .apply(builder,ConfigCraftingCondition::load));

    private final ResourceLocation fileID;
    private final String optionPath;

    private ConfigCraftingCondition(ResourceLocation fileID, String optionPath)
    {
        this.fileID = fileID;
        this.optionPath = optionPath;
    }

    public static ConfigCraftingCondition of(ResourceLocation fileName,String optionPath) { return new ConfigCraftingCondition(fileName,optionPath); }
    public static ConfigCraftingCondition of(BooleanOption option) {
        String path = null;
        ConfigFile file = option.getFile();
        if(file == null)
            throw new IllegalArgumentException("Config Option was not attached to a config file!");
        for(var entry : file.getAllOptions().entrySet())
        {
            if(entry.getValue() == option)
                return of(file.getFileID(),entry.getKey());
        }
        throw new IllegalArgumentException("Config Option was not a member of the config file!");
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

    @SuppressWarnings("deprecation")
    private static ConfigCraftingCondition load(Optional<String> fileName, Optional<ResourceLocation> fileID, String option)
    {
        if(fileName.isPresent())
        {
            ConfigFile file = ConfigFile.lookupFile(fileName.get());
            if(file != null)
                return new ConfigCraftingCondition(file.getFileID(),option);
            return new ConfigCraftingCondition(ConfigFile.forceGenerateID(fileName.get()),option);
        }
        else if(fileID.isPresent())
            return new ConfigCraftingCondition(fileID.get(),option);
        return new ConfigCraftingCondition(VersionUtil.modResource("unknown","unknown"),option);
    }

}
