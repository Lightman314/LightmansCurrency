package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings.CustomItemScaleSettings;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.client.config.CustomItemScaleData;
import io.github.lightman314.lightmanscurrency.client.config.CustomItemScaleData.ItemTest;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IRemovalListener;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.FloatParser;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ListItemScaleOption extends ListOptionWidget implements IRemovalListener {

    private EditBox box1;
    private EditBox box2;

    private final CustomItemScaleSettings settings;
    protected ListItemScaleOption(Builder builder) {
        super(builder);
        this.settings = builder.settings;
        this.option.addListener(this::onOptionChanged);
    }

    @Override
    protected void addMoreChildren(ScreenArea area) {

        Pair<ItemTest,Float> value = this.settings.getValue(this.index);

        this.box1 = this.addChildAtRelativePosition(TextInputUtil.stringBuilder()
                        .width(HALF_WIDTH - 60)
                        .startingString(value.getFirst().toString())
                        .handler(this::changeItemTest)
                        .build(),
                ScreenPosition.of(HALF_WIDTH + 5,0));

        this.box2 = this.addChildAtRelativePosition(TextInputUtil.floatBuilder()
                        .apply(FloatParser.builder().min(0f).max(10f).consumer())
                        .startingValue(value.getSecond())
                        .handler(this::changeScale)
                        .build(),
                ScreenPosition.of(WIDTH - 50,0));

    }

    public static ListItemScaleOption create(ConfigOption<?> option, int index, CustomItemScaleSettings settings) { return new Builder(option,index,settings).build(); }

    private void onOptionChanged(ConfigOption<?> option)
    {
        Pair<ItemTest,Float> newValue = this.settings.getValue(this.index);
        if(this.box1 != null)
            this.box1.setValue(newValue.getFirst().toString());
        if(this.box2 != null)
        {
            float val = newValue.getSecond();
            if(val == 0f && this.box2.getValue().isEmpty())
                return;
            if(val == 0f)
                this.box2.setValue("0");
            else
                this.box2.setValue(String.valueOf(val));
        }
    }

    @Override
    public void onRemovedFromScreen() {
        this.option.removeListener(this::onOptionChanged);
    }

    private void changeItemTest(String test)
    {
        ItemTest newTest = CustomItemScaleData.tryParseTest(test);
        if(newTest != null)
            this.changeValue(Pair.of(newTest,this.settings.getValue(this.index).getSecond()));
    }

    private void changeScale(float scale)
    {
        this.changeValue(Pair.of(this.settings.getValue(this.index).getFirst(),scale));
    }

    protected static class Builder extends ListOptionBuilder<Builder>
    {

        private final CustomItemScaleSettings settings;
        private Builder(ConfigOption<?> option, int index, CustomItemScaleSettings settings) { super(option, index, settings); this.settings = settings; }

        @Override
        protected Builder getSelf() { return this; }

        public ListItemScaleOption build() { return new ListItemScaleOption(this); }

    }

}