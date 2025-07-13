package io.github.lightman314.lightmanscurrency.api.traders.rules;

import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;

public interface ICopySupportingRule {

    void resetToDefaultState();
    void writeSettings(SavedSettingData.MutableNodeAccess node);
    void loadSettings(SavedSettingData.NodeAccess node);

}