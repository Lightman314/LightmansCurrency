package io.github.lightman314.lightmanscurrency.api.config.conditions;

import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.BooleanOption;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ConfigCraftingCondition implements ICondition {

    public static final ResourceLocation ID = VersionUtil.lcResource("configured");
    public static final IConditionSerializer<ConfigCraftingCondition> SERIALIZER = new Serializer();

    private final ResourceLocation fileID;
    private final String optionPath;

    private ConfigCraftingCondition(ResourceLocation fileID, String optionPath)
    {
        this.fileID = fileID;
        this.optionPath = optionPath;
    }

    public static ConfigCraftingCondition of(ResourceLocation fileID,String optionPath) { return new ConfigCraftingCondition(fileID,optionPath); }
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
    public ResourceLocation getID() { return ID; }

    private static class Serializer implements IConditionSerializer<ConfigCraftingCondition>
    {
        @Override
        public ResourceLocation getID() { return ID; }
        @Override
        public void write(JsonObject json, ConfigCraftingCondition condition) {
            json.addProperty("fileID",condition.fileID.toString());
            json.addProperty("option",condition.optionPath);
        }

        @Override
        public ConfigCraftingCondition read(JsonObject json) {
            String optionPath = GsonHelper.getAsString(json,"option");
            ResourceLocation fileID = VersionUtil.parseResource(GsonHelper.getAsString(json,"fileID"));
            return of(fileID,optionPath);
        }

    }

}