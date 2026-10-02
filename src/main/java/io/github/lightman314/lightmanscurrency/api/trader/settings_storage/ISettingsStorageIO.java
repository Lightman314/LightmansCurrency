package io.github.lightman314.lightmanscurrency.api.trader.settings_storage;

import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Comparator;

public interface ISettingsStorageIO {

    Comparator<ISettingsStorageIO> SORTER = Comparator.comparingInt(PriorityLoad::getSettingsLoadPriority);

    DualKey getSettingsKey();
    Component getSettingsName();
    void encodeSettings(ValueOutput output);
    void decodeSettings(ValueInput data,SettingsLoadContext context);
    void appendDisplay(ValueInput data,SettingsDisplayOutput output);

    interface PriorityLoad extends ISettingsStorageIO {
        default int getSettingsLoadPriority() { return 0; }
        @Override
        default void decodeSettings(ValueInput data,SettingsLoadContext context) {
            if(context instanceof SettingsLoadContext.Mutable m)
                this.decodeSettings(data,m);
        }
        void decodeSettings(ValueInput data,SettingsLoadContext.Mutable context);
        static int getSettingsLoadPriority(ISettingsStorageIO entry) {
            if(entry instanceof PriorityLoad pl)
                return pl.getSettingsLoadPriority();
            return Integer.MAX_VALUE;
        }
    }

}
