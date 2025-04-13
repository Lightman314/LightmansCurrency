package io.github.lightman314.lightmanscurrency.common.enchantments.data;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.MoneyValueOption;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValueParser;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@FieldsAreNonnullByDefault
public class ValueInput {

    public static final Codec<ValueInput> CODEC = Codec.STRING.comapFlatMap(s -> DataResult.success(new ValueInput(s)),v -> v.costInput);

    protected final String costInput;
    @Nullable
    private MoneyValue cachedCost;
    public final boolean hasCache() { return this.cachedCost == null; }
    public final MoneyValue getCost() {
        if(this.cachedCost == null)
            this.updateCache();
        return this.cachedCost;
    }
    public final void updateCache() { this.cachedCost = safeParse(this.costInput, this.getMatch()); }
    @Nullable
    protected MoneyValue getMatch() { return null; }
    public ValueInput(String costInput) { this.costInput = costInput; }

    public static String writeConfig(MoneyValueOption option) {
        ConfigFile file = option.getFile();
        if(file == null)
            throw new IllegalArgumentException("Config Option was not attached to a config file!");
        for(var entry : file.getAllOptions().entrySet())
        {
            if(entry.getValue() == option)
                return "config;" + file.getFileID() + ";" + entry.getKey();
        }
        throw new IllegalArgumentException("Config Option was not a member of the config file!");
    }

    @Nonnull
    protected static MoneyValue safeParse(String input, @Nullable MoneyValue mustMatch) {
        try {
            if(input.isEmpty())
                return MoneyValue.empty();
            MoneyValue result = MoneyValue.empty();
            if(input.startsWith("config;"))
            {
                //Get the configured value
                String[] entries = input.split(";");
                if(entries.length != 3)
                    return MoneyValue.empty();
                ResourceLocation fileID = VersionUtil.parseResource(entries[1]);
                String optionPath = entries[2];
                ConfigFile file = ConfigFile.lookupFile(fileID);
                if(file != null)
                {
                    ConfigOption<?> option = file.getAllOptions().get(optionPath);
                    if(option instanceof MoneyValueOption mvo)
                        result = mvo.get();
                }
            }
            else
                result = MoneyValueParser.parse(new StringReader(input),mustMatch != null);
            if(mustMatch != null && !result.sameType(mustMatch))
                return MoneyValue.empty();
            return result;
        } catch (CommandSyntaxException exception) {
            return MoneyValue.empty();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ValueInput other && other.getClass() == this.getClass())
            return other.costInput.equals(this.costInput);
        return false;
    }

    @Override
    public int hashCode() { return Objects.hash(this.costInput); }

}
