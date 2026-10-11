package io.github.lightman314.lightmanscurrency.api.trader.settings_storage;

import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;

import java.util.ArrayList;
import java.util.List;

public interface SettingsDisplayOutput {

    TextEntry DATA_ENTRY_LABEL = new TextEntry("data.lightmanscurrency.label");

    default void acceptTitle(Component title) { this.acceptLine(title.copy().withStyle(ChatFormatting.BOLD)); }
    default void acceptTitle(TextEntry title) { this.acceptTitle(title.get()); }

    default void acceptEntry(Component label,boolean value) { this.acceptEntry(label,LCText.GUI_SETTINGS_VALUE_TRUE_FALSE.getComponent(value)); }
    default void acceptEntry(Component label,int value) { this.acceptLine(DATA_ENTRY_LABEL.get(label,value)); }
    default void acceptEntry(Component label,float value) { this.acceptLine(DATA_ENTRY_LABEL.get(label,value)); }
    default void acceptEntry(Component label,long value) { this.acceptLine(DATA_ENTRY_LABEL.get(label,value)); }
    default void acceptEntry(Component label,double value) { this.acceptLine(DATA_ENTRY_LABEL.get(label,value)); }
    default void acceptEntry(Component label,String value) { this.acceptLine(DATA_ENTRY_LABEL.get(label,value)); }
    default void acceptEntry(Component label,Component value) { this.acceptLine(DATA_ENTRY_LABEL.get(label,value)); }

    default void acceptEntry(TextEntry label,boolean value) { this.acceptEntry(label.get(),value); }
    default void acceptEntry(TextEntry label,int value) { this.acceptEntry(label.get(),value); }
    default void acceptEntry(TextEntry label,float value) { this.acceptEntry(label.get(),value); }
    default void acceptEntry(TextEntry label,long value) { this.acceptEntry(label.get(),value); }
    default void acceptEntry(TextEntry label,double value) { this.acceptEntry(label.get(),value); }
    default void acceptEntry(TextEntry label,String value) { this.acceptEntry(label.get(),value); }
    default void acceptEntry(TextEntry label,Component value) { this.acceptEntry(label.get(),value); }
    default void acceptEntry(TextEntry label,TextEntry value) { this.acceptEntry(label.get(),value.get()); }

    default void acceptLine(TextEntry line) { this.acceptLine(line.get()); }
    void acceptLine(Component line);

    default void collectData(ValueInput input,List<ISettingsStorageIO> settings) {
        for(ISettingsStorageIO s : settings)
            input.child(s.getSettingsKey().toString()).ifPresent(e -> s.appendDisplay(e,this));
    }

    record Simple(List<Component> lines) implements SettingsDisplayOutput {
        public Simple() { this(new ArrayList<>()); }
        @Override
        public void acceptLine(Component line) { this.lines.add(line); }
    }

}
