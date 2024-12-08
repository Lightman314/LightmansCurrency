package io.github.lightman314.lightmanscurrency.api.config.conditions;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.BooleanOption;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

import javax.annotation.Nonnull;

public class ConfigCraftingCondition implements ICondition {

    public static final ResourceLocation ID = VersionUtil.lcResource("configured");
    public static final IConditionSerializer<ConfigCraftingCondition> SERIALIZER = new Serializer();

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

    @Override
    public ResourceLocation getID() { return ID; }

    private static class Serializer implements IConditionSerializer<ConfigCraftingCondition>
    {
        @Override
        public ResourceLocation getID() { return ID; }
        @Override
        public void write(JsonObject json, ConfigCraftingCondition condition) {
            json.addProperty("fileName",condition.fileName);
            json.addProperty("option",condition.optionPath);
        }

        @Override
        public ConfigCraftingCondition read(JsonObject json) {
            String fileName = GsonHelper.getAsString(json,"fileName");
            String optionPath = GsonHelper.getAsString(json,"option");
            return of(fileName,optionPath);
        }

    }

}