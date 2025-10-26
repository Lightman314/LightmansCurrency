package io.github.lightman314.lightmanscurrency.api.config.options;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigComments;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParser;
import io.github.lightman314.lightmanscurrency.api.config.options.parsing.ConfigParsingException;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.text.MultiLineTextEntry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ConfigOption<T> implements Supplier<T> {

    public enum LoadSource { FILE, COMMAND, SYNC }

    private final List<Consumer<ConfigOption<?>>> listeners = new ArrayList<>();
    public void addListener(Consumer<ConfigOption<?>> listener){
        if(!this.listeners.contains(listener))
            this.listeners.add(listener);
    }
    public void removeListener(Consumer<ConfigOption<?>> listener) { this.listeners.remove(listener); }

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
    public final void init(ConfigFile parent, String name, String optionID) {
        if(this.parent == null)
        {
            this.parent = parent;
            this.name = name;
            this.optionID = optionID;
        }
        else
            LightmansCurrency.LogWarning("Attempted to define an options parent twice!");
    }
    
    public final List<String> getComments() {
        List<String> list = this.comments.getComments();
        list.addAll(this.bonusComments());
        return list;
    }

    public Component getDisplayName() { return EasyText.translatable(ConfigFile.translationForOption(this.parent.getFileID(),this.getFullName())); }
    public final List<Component> getCommentTooltips() {
        List<Component> list = new MultiLineTextEntry(ConfigFile.translationForComment(this.parent.getFileID(),this.getFullName())).get();
        list.addAll(this.bonusCommentTooltips());
        return list;
    }

    private final Supplier<T> defaultValue;
    private T currentValue = null;
    private T syncedValue = null;

    protected ConfigOption(Supplier<T> defaultValue) { this.defaultValue = defaultValue; }
    
    protected List<String> bonusComments() {
        String bonus = this.bonusComment();
        if(bonus == null)
            return new ArrayList<>();
        return Lists.newArrayList(bonus);
    }
    @Nullable
    protected String bonusComment() { return null; }

    protected List<Component> bonusCommentTooltips() {
        Component bonus = this.bonusCommentTooltip();
        if(bonus == null)
            return new ArrayList<>();
        return Lists.newArrayList(bonus);
    }
    @Nullable
    protected Component bonusCommentTooltip() { return null; }

    
    protected abstract ConfigParser<T> getParser();

    private void alertListeners(boolean includingFile)
    {
        if(includingFile && this.parent != null)
            this.parent.onOptionChanged(this);
        for(var l : new ArrayList<>(this.listeners))
            l.accept(this);
    }

    @Nullable
    public final Pair<Boolean,ConfigParsingException> load(String line, LoadSource source) {
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
            //Inform relevant listeners about the change of value
            if(source == LoadSource.SYNC)
                this.alertListeners(false);
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

    
    public final String write() { return this.getParser().write(this.getCurrentValue()); }
    @Nullable
    public final String writeUnsafe(Object object)
    {
        try { return this.getParser().write((T)object);
        } catch (ClassCastException ignored) { return null; }
    }

    public final void write(String name, Consumer<String> writer) {
        ConfigFile.writeComments(this.getComments(), writer);
        writer.accept(name + "=" + this.write());
    }

    
    public static String cleanWhitespace(String line) {
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
    
    public final T get() {
        if(this.syncedValue != null)
            return this.syncedValue;
        return this.getCurrentValue();
    }

    public void set(T newValue)
    {
        if(this.currentValue != null && this.isSame(newValue,this.currentValue))
            return;
        if(!this.allowedValue(newValue))
            return;
        this.currentValue = Objects.requireNonNull(newValue);
        this.alertListeners(true);
    }

    public boolean allowedValue(T newValue) { return true; }

    public void setUnsafe(Object newValue)
    {
        T oldValue = this.get();
        boolean synced = this.syncedValue != null;
        try {
            T nv = (T)newValue;
            if(nv == null || this.isSame(nv,oldValue))
                return;
            if(!this.allowedValue(nv))
                return;
            //If we have synced data from the server
            if(synced)
                this.syncedValue = nv;
            else
                this.currentValue = nv;
            //Call the get method as it will trigger a ClassCastException if we defined the value in an invalid manner
            this.get();
            //Tell the listeners that the file has been changed
            this.alertListeners(!synced);
        } catch (ClassCastException e) {
            LightmansCurrency.LogDebug("SetUnsafe operation failed!",e);
            if(synced)
                this.syncedValue = oldValue;
            else
                this.set(oldValue);
        }
    }

    protected final boolean isSame(T newValue,T currentValue)
    {
        ConfigParser<T> parser = this.getParser();
        return Objects.equals(parser.write(Objects.requireNonNull(newValue)),parser.write(currentValue));
    }

    protected final T getCurrentValue() {
        if(this.currentValue == null)
        {
            LightmansCurrency.LogDebug("Attempted to access the value of '" + this.optionID + "' before the config was loaded!");
            return this.getDefaultValue();
        }
        return this.currentValue;
    }

    public T getDefaultValue() { return this.defaultValue.get(); }

}
