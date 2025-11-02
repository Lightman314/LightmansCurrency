package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import com.mojang.datafixers.util.Either;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.subscreens.ItemOverrideListConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListButtonOption;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.common.config.ItemOverrideListOption;
import io.github.lightman314.lightmanscurrency.common.enchantments.data.ItemOverride;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.ArrayList;
import java.util.function.Consumer;

public class MMItemOverrideSettings extends EasyListSettings<ItemOverride, ItemOverrideListOption> {

    public MMItemOverrideSettings(ItemOverrideListOption option, Consumer<Object> changeHandler) {
        super(option, changeHandler);
    }

    @Override
    protected ItemOverride getBackupValue() { return new ItemOverride(MoneyValue.empty(),new ArrayList<>()); }
    @Override
    protected ItemOverride getNewEntryValue() { return this.getBackupValue(); }

    @Override
    protected Either<ItemOverride, Void> tryCastValue(Object newValue) {
        if(newValue instanceof ItemOverride override)
            return Either.left(override);
        return Either.right(null);
    }

    @Override
    public AbstractWidget buildEntry(int index) {
        return ListButtonOption.builder(this.option,index,this)
                .buttonText(LCText.CONFIG_OPTION_EDIT)
                .openScreen((handler) -> new ItemOverrideListConfigScreen(this.getScreen(),this.getScreen().file,this.option,index,handler))
                .build();
    }
}
