package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.ListScreenSettings;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListItemScaleOption;
import io.github.lightman314.lightmanscurrency.client.config.CustomItemScaleConfigOption;
import io.github.lightman314.lightmanscurrency.client.config.CustomItemScaleData;
import io.github.lightman314.lightmanscurrency.client.config.ItemTest;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.world.item.Items;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CustomItemScaleSettings extends ListScreenSettings {

    private final CustomItemScaleConfigOption option;
    public CustomItemScaleSettings(CustomItemScaleConfigOption option, Consumer<Object> changeHandler) {
        super(changeHandler);
        this.option = option;
    }

    private List<Pair<ItemTest,Float>> getList() { return this.option.get().getRawData(); }

    public Pair<ItemTest,Float> getValue(int index)
    {
        List<Pair<ItemTest,Float>> list = this.getList();
        if(index < 0 || index >= list.size())
            return this.createDummyEntry();
        return list.get(index);
    }

    private Pair<ItemTest,Float> createDummyEntry() { return Pair.of(ItemTest.create(Items.AIR),1f); }

    @Override
    public AbstractWidget buildEntry(int index) { return ListItemScaleOption.create(this.option,index,this); }

    @Override
    public int getListSize() { return this.option.get().getRawData().size(); }

    private void setList(List<Pair<ItemTest,Float>> list) { this.setValue(new CustomItemScaleData(list)); }

    @Override
    public void addEntry() {
        List<Pair<ItemTest,Float>> list = this.getList();
        list.add(this.createDummyEntry());
        this.setList(list);
    }

    @Override
    public void removeEntry(int index) {
        List<Pair<ItemTest,Float>> list = this.getList();
        if(index < 0 || index >= list.size())
            return;
        list.remove(index);
        this.setList(list);
    }

    @Override
    public void setEntry(int index, Object newValue) {
        if(newValue instanceof Pair<?,?> pair && pair.getFirst() instanceof ItemTest && pair.getSecond() instanceof Float)
        {
            List<Pair<ItemTest,Float>> list = this.getList();
            if(index < 0 || index >= list.size())
                return;
            list.set(index,(Pair<ItemTest,Float>)pair);
            this.setList(list);
        }
    }

}
