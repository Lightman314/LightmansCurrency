package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings;

import com.mojang.datafixers.util.Either;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.subscreens.BonusForEnchantmentListConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.list.ListButtonOption;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.common.config.BonusForEnchantmentListOption;
import io.github.lightman314.lightmanscurrency.common.enchantments.data.BonusForEnchantment;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.function.Consumer;

public class MMBonusForEnchantmentListSettings extends EasyListSettings<BonusForEnchantment,BonusForEnchantmentListOption> {

    public MMBonusForEnchantmentListSettings(BonusForEnchantmentListOption option, Consumer<Object> changeHandler) { super(option, changeHandler); }

    @Override
    protected BonusForEnchantment getBackupValue() { return new BonusForEnchantment(MoneyValue.empty(), VersionUtil.vanillaResource("null"),0); }

    @Override
    protected BonusForEnchantment getNewEntryValue() { return this.getBackupValue(); }

    @Override
    protected Either<BonusForEnchantment, Void> tryCastValue(Object newValue) {
        if(newValue instanceof BonusForEnchantment val)
            return Either.left(val);
        return Either.right(null);
    }

    @Override
    public AbstractWidget buildEntry(int index) {
        return ListButtonOption.builder(this.option,index,this)
                .buttonText(LCText.CONFIG_OPTION_EDIT.get())
                .openScreen(handler -> new BonusForEnchantmentListConfigScreen(this.getScreen(),this.getScreen().file,this.option,index,handler))
                .build();
    }
}
