package io.github.lightman314.lightmanscurrency.api.stats.interfaces;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.stats.StatKey;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;

public interface StatHolder extends StatViewer,StatListener {

    TextEntry BUTTON_CLEAR_STATS = TextEntry.button(LCApi.MODID,"stats.clear");

    interface Clearable extends StatHolder {
        default void clear() { this.clear(false); }
        default void clear(boolean fullClear) {
            for(StatKey<?,?> key : this.getKeys())
                this.resetStat(key,fullClear);
        }
        default void resetStat(StatKey<?,?> key) { this.resetStat(key,false); }
        void resetStat(StatKey<?,?> key,boolean fullClear);
    }

}