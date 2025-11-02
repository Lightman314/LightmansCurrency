package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import com.mojang.datafixers.util.Either;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.ListScreenSettings;
import io.github.lightman314.lightmanscurrency.api.config.options.ListOption;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class EasyListSettings<T,O extends ListOption<T>> extends ListScreenSettings
{
    protected final O option;
    public EasyListSettings(O option, Consumer<Object> changeHandler)
    {
        super(changeHandler);
        this.option = option;
    }

    protected List<T> getList() { return new ArrayList<>(this.option.get()); }
    protected abstract T getBackupValue();
    protected T getValue(int index)
    {
        List<T> list = this.getList();
        if(index < 0 || index >= list.size())
            return this.getBackupValue();
        return list.get(index);
    }
    protected abstract T getNewEntryValue();
    protected abstract Either<T,Void> tryCastValue(Object newValue);

    @Override
    public int getListSize() { return this.getList().size(); }

    @Override
    public void addEntry() {
        List<T> list = this.getList();
        list.add(this.getNewEntryValue());
        this.setValue(list);
    }

    @Override
    public void removeEntry(int index) {
        List<T> list = this.getList();
        if(index < 0 || index >= list.size())
            return;
        list.remove(index);
        this.setValue(list);
    }

    @Override
    public void setEntry(int index, Object newValue) {
        List<T> list = this.getList();
        if(index < 0 || index >= list.size())
            return;
        this.tryCastValue(newValue).ifLeft(value -> {
            list.set(index,value);
            this.setValue(list);
        });
    }
}