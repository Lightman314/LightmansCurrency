package io.github.lightman314.lightmanscurrency.api.config.options;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigComments;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class ConfigOption<T> implements Supplier<T> {

    public enum LoadSource { FILE, COMMAND, SYNC }

    private ConfigComments comments = ConfigComments.EMPTY;
    public final void setComments(ConfigComments comments) {
        if(this.comments == ConfigComments.EMPTY)
            this.comments = comments;
        else
            LightmansCurrency.LogWarning("Attempted to define an options comments twice!");
    }
    private ConfigFile parent = null;
    @Nullable
    public final ConfigFile getFile() { return this.parent; }
    private String name = "null";
    @Nonnull
    public String getName() { return this.name; }
    @Nullable
    public String getFullName()
    {
        ConfigFile file = this.getFile();
        if(file == null)
            return null;
        for(var entry : file.getAllOptions().entrySet())
        {
            if(entry.getValue() == this)
                return entry.getKey();
        }
        return null;
    }
    private String optionID = "null.null";
    public final void init(@Nonnull ConfigFile parent, @Nonnull String name, @Nonnull String optionID) {
        if(this.parent == null)
        {
            this.parent = parent;
            this.name = name;
            this.optionID = optionID;
        }
        else
            LightmansCurrency.LogWarning("Attempted to define an options parent twice!");
    }
    @Nonnull
    public final List<String> getComments() {
        List<String> list = this.comments.getComments();
        list.addAll(this.bonusComments());
        return list;
    }

    private final Supplier<T> defaultValue;
    private T currentValue = null;
    private T syncedValue = null;

    protected ConfigOption(@Nonnull Supplier<T> defaultValue) { this.defaultValue = defaultValue; }



    @Nonnull
    protected List<String> bonusComments() {
        String bonus = this.bonusComment();
        if(bonus == null)
            return new ArrayList<>();
        return Lists.newArrayList(bonus);
    }
    @Nullable
    protected String bonusComment() { return null; }

    @Nonnull
    protected abstract ConfigParser<T> getParser();

    @Nullable
    public final Pair<Boolean,ConfigParsingException> load(@Nonnull String line, @Nonnull LoadSource source) {
        line = cleanWhitespace(line);
        try {
            T val = this.getParser().tryParse(line);
            if(source == LoadSource.SYNC)
                this.syncedValue = val;
            else
                this.currentValue = val;
            //Trigger on changed code if loaded from command.
            if(source == LoadSource.COMMAND && this.parent != null)
                this.parent.onOptionChanged(this);
            return Pair.of(true,null);
        } catch (ConfigParsingException e) {
            LightmansCurrency.LogError("Error parsing " + this.optionID + "!", e);
            this.currentValue = this.defaultValue.get();
            return Pair.of(false,e);
        }
    }

    public final void clear() { this.currentValue = null; }
    public final boolean isLoaded() { return this.currentValue != null; }
    public final void setToDefault() { this.set(this.getDefaultValue()); }
    public final void loadDefault() { this.currentValue = this.getDefaultValue(); }
    public final void clearSyncedData() { this.syncedValue = null; }

    @Nonnull
    public final String write() { return this.getParser().write(this.getCurrentValue()); }

    public final void write(@Nonnull String name, @Nonnull Consumer<String> writer) {
        ConfigFile.writeComments(this.getComments(), writer);
        writer.accept(name + "=" + this.write());
        writer.accept("");
    }

    @Nonnull
    public static String cleanWhitespace(@Nonnull String line) {
        StringBuilder result = new StringBuilder();
        boolean start = true;
        StringBuilder temp = new StringBuilder();
        for(int i = 0; i < line.length(); ++i)
        {
            char c = line.charAt(i);
            if(Character.isWhitespace(c))
            {
                if(!start)
                    temp.append(c);
            }
            else
            {
                if(start)
                    start = false;
                if(!temp.isEmpty())
                {
                    result.append(temp);
                    temp = new StringBuilder();
                }
                result.append(c);
            }
        }
        return result.toString();
    }

    @Override
    @Nonnull
    public final T get() {
        if(this.syncedValue != null)
            return this.syncedValue;
        return this.getCurrentValue();
    }

    public void set(@Nonnull T newValue)
    {
        if(this.currentValue != null && this.currentValue.equals(newValue))
            return;
        this.currentValue = Objects.requireNonNull(newValue);
        if(this.parent != null)
            this.parent.onOptionChanged(this);
    }

    @Nonnull
    protected final T getCurrentValue() {
        if(this.currentValue == null)
        {
            LightmansCurrency.LogDebug("Attempted to access the value of '" + this.optionID + "' before the config was loaded!");
            return this.getDefaultValue();
        }
        return this.currentValue;
    }

    @Nonnull
    protected T getDefaultValue() { return this.defaultValue.get(); }

}