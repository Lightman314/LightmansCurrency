package io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.MoneyValueConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list.settings.*;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.trade_mod.ConfiguredTradeModConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.trade_mod.VillagerTradeModConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.ScreenPositionOptionInput;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.SimpleButtonOption;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.SimpleEditBoxOption;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.config.options.basic.*;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.*;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.client.config.CustomItemScaleConfigOption;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.*;
import io.github.lightman314.lightmanscurrency.common.config.VillagerTradeModsOption;
import io.github.lightman314.lightmanscurrency.common.villager_merchant.listings.configured.ConfiguredTradeModOption;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ConfigWidgetHelper {

    private static final List<WidgetBuilder> builders;

    static {
        builders = new ArrayList<>();
        registerWidgetBuilder(new DefaultWidgetBuilder());
    }

    public static void registerWidgetBuilder(WidgetBuilder builder) { builders.add(builder); }

    public static AbstractWidget buildWidgetForOption(Screen screen, ConfigFileOption file, ConfigOption<?> option, Consumer<Object> changeValueConsumer, Supplier<Boolean> canEdit)
    {
        for(WidgetBuilder builder : builders)
        {
            AbstractWidget result = builder.createWidgetForOption(screen,file,option,changeValueConsumer,canEdit);
            if(result != null)
                return result;
        }
        //Create Default unsupported button
        return SimpleButtonOption.createUndefined(option);
    }

    private static class DefaultWidgetBuilder implements WidgetBuilder
    {

        @Nullable
        @Override
        public AbstractWidget createWidgetForOption(Screen screen, ConfigFileOption file, ConfigOption<?> o, Consumer<Object> changeValueConsumer, Supplier<Boolean> canEdit) {
            //Basic Options (not including the list options)
            if(o instanceof BooleanOption option)
            {
                return SimpleButtonOption.builder(option,changeValueConsumer,canEdit)
                        .buttonText(() -> LCText.GUI_SETTINGS_VALUE_TRUE_FALSE.get(option.get()).get())
                        .clickHandler2(handler -> handler.accept(!option.get()))
                        .build();
            }
            if(o instanceof DoubleOption option)
            {
                return SimpleEditBoxOption.builder(option,changeValueConsumer,canEdit)
                        .inputBoxSetup(handler ->
                                TextInputUtil.doubleBuilder()
                                        .apply(DoubleParser.builder().min(option.lowerLimit).max(option.upperLimit).consumer())
                                        .startingValue(option.get())
                                        .handler(handler::accept))
                        .optionChangeHandler(text -> {
                            double newVal = option.get();
                            if(newVal == 0d && text.getValue().isEmpty())
                                return;
                            if(newVal == 0d)
                                text.setValue("0");
                            else
                                text.setValue(String.valueOf(option.get()));
                        })
                        .build();
            }
            if(o instanceof EnumOption<?> option)
            {
                return SimpleButtonOption.builder(option,changeValueConsumer,canEdit)
                        .buttonText(() -> EasyText.literal(option.get().name()))
                        .clickHandler((left,handler) -> {
                            Enum<?> current = option.get();
                            int nextOrdinal = current.ordinal() + (left ? 1 : -1);
                            Enum<?>[] all = option.clazz.getEnumConstants();
                            Enum<?> wrapDefault = left ? all[0] : all[all.length - 1];
                            handler.accept(EnumUtil.enumFromOrdinal(nextOrdinal,all,wrapDefault));
                        })
                        .build();
            }
            if(o instanceof FloatOption option)
            {
                return SimpleEditBoxOption.builder(option,changeValueConsumer,canEdit)
                        .inputBoxSetup(handler ->
                                TextInputUtil.floatBuilder()
                                        .apply(FloatParser.builder().min(option.lowerLimit).max(option.upperLimit).consumer())
                                        .startingValue(option.get())
                                        .handler(handler::accept))
                        .optionChangeHandler(text -> {
                            float newVal = option.get();
                            if(newVal == 0d && text.getValue().isEmpty())
                                return;
                            if(newVal == 0d)
                                text.setValue("0");
                            else
                                text.setValue(String.valueOf(option.get()));
                        })
                        .build();
            }
            if(o instanceof IntOption option)
            {
                return SimpleEditBoxOption.builder(option,changeValueConsumer,canEdit)
                        .inputBoxSetup(handler ->
                            TextInputUtil.intBuilder()
                                    .apply(IntParser.builder().min(option.lowerLimit).max(option.upperLimit).consumer())
                                    .startingValue(option.get())
                                    .handler(handler::accept))
                        .optionChangeHandler(text -> {
                            int newVal = option.get();
                            if(newVal == 0d && text.getValue().isEmpty())
                                return;
                            text.setValue(String.valueOf(option.get()));
                        })
                        .build();
            }
            if(o instanceof LongOption option)
            {
                return SimpleEditBoxOption.builder(option,changeValueConsumer,canEdit)
                        .inputBoxSetup(handler ->
                                TextInputUtil.longBuilder()
                                        .apply(LongParser.builder().min(option.lowerLimit).max(option.upperLimit).consumer())
                                        .startingValue(option.get())
                                        .handler(handler::accept))
                        .optionChangeHandler(text -> {
                            long newVal = option.get();
                            if(newVal == 0d && text.getValue().isEmpty())
                                return;
                            text.setValue(String.valueOf(option.get()));
                        })
                        .build();
            }
            if(o instanceof StringOption option)
            {
                return SimpleEditBoxOption.builder(option,changeValueConsumer,canEdit)
                        .inputBoxSetup(handler ->
                                TextInputUtil.stringBuilder()
                                        .startingValue(option.get())
                                        .handler(handler::accept))
                        .optionChangeHandler(text -> text.setValue(option.get()))
                        .build();
            }
            if(o instanceof ItemOption option)
            {
                return SimpleEditBoxOption.builder(option,changeValueConsumer,canEdit)
                        .inputBoxSetup(handler ->
                                TextInputUtil.stringBuilder()
                                        .startingValue(BuiltInRegistries.ITEM.getKey(option.get()).toString())
                                        .handler(string -> {
                                            if(string.isEmpty() && option.allowedValue(Items.AIR))
                                                handler.accept(Items.AIR);
                                            try {
                                                ResourceLocation id = VersionUtil.parseResource(string);
                                                if(BuiltInRegistries.ITEM.containsKey(id))
                                                {
                                                    Item item = BuiltInRegistries.ITEM.get(id);
                                                    if(option.allowedValue(item))
                                                        handler.accept(item);
                                                }
                                            } catch (ResourceLocationException ignored) {}
                                        }))
                        .optionChangeHandler(text -> text.setValue(BuiltInRegistries.ITEM.getKey(option.get()).toString()))
                        .build();
            }
            if(o instanceof MoneyValueOption option)
            {
                return SimpleButtonOption.builder(option,changeValueConsumer,canEdit)
                        .buttonText(() -> option.get().getText(LCText.GUI_MONEY_STORAGE_EMPTY.get()))
                        .openScreen((handler) -> new MoneyValueConfigScreen(screen,file,option,handler))
                        .build();
            }
            if(o instanceof ResourceOption option)
            {
                return SimpleEditBoxOption.builder(option,changeValueConsumer,canEdit)
                        .inputBoxSetup(handler ->
                                TextInputUtil.stringBuilder()
                                        .startingValue(option.get().toString())
                                        .handler(string -> {
                                            try { handler.accept(VersionUtil.parseResource(string));
                                            } catch (ResourceLocationException ignored) {}
                                        }))
                        .optionChangeHandler(text -> text.setValue(option.get().toString()))
                        .build();
            }
            if(o instanceof ScreenPositionOption option)
                return ScreenPositionOptionInput.create(option,changeValueConsumer,canEdit);
            if(o instanceof DoubleListOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new DoubleListSettings(option,handler));
            if(o instanceof FloatListOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new FloatListSettings(option,handler));
            if(o instanceof IntListOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new IntListSettings(option,handler));
            if(o instanceof LongListOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new LongListSettings(option,handler));
            if(o instanceof StringListOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new StringListSettings(option,handler));
            if(o instanceof ItemListOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new ItemListSettings(option,handler));
            if(o instanceof MoneyValueListOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new MoneyValueListSettings(option,handler));
            if(o instanceof ResourceListOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new ResourceListSettings(option,handler));
            if(o instanceof WildcardSelectorListOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new WildcardSelectorListSettings(option,handler));
            if(o instanceof CustomItemScaleConfigOption option)
                return SimpleButtonOption.createForList(option,changeValueConsumer,screen,file,handler -> new CustomItemScaleSettings(option,handler));
            if(o instanceof ConfiguredTradeModOption option)
                return SimpleButtonOption.builder(option,changeValueConsumer,canEdit)
                        .buttonText(LCText.CONFIG_OPTION_EDIT)
                        .openScreen(handler -> new ConfiguredTradeModConfigScreen(screen,file,option,changeValueConsumer))
                        .build();
            if(o instanceof VillagerTradeModsOption option)
                return SimpleButtonOption.builder(option,changeValueConsumer,canEdit)
                        .buttonText(LCText.CONFIG_OPTION_EDIT)
                        .openScreen(handler -> new VillagerTradeModConfigScreen(screen,file,option,changeValueConsumer))
                        .build();
            return null;
        }
    }

}
