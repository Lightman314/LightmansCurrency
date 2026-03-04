package io.github.lightman314.lightmanscurrency.api.traders.settings.builtin;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.settings.data.LoadContext;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.settings.EasyTraderNodeSettings;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.InputNode;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;

public class InputSettingsNode extends EasyTraderNodeSettings<TraderData,InputNode> {

    public InputSettingsNode(TraderData trader,InputNode node) { super("input",trader,node); }

    @Override
    public MutableComponent getName() { return LCText.DATA_CATEGORY_INPUT_SETTINGS.get(); }

    @Override
    protected String getRequiredPermission() { return Permissions.InputTrader.EXTERNAL_INPUTS; }

    @Override
    public void saveSettings(SavedSettingData.MutableNodeAccess data) {
        for(Direction side : Direction.values())
            data.setEnumValue(side.toString(),this.node.getSidedState(side));
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess data, LoadContext context) {
        for(Direction side : Direction.values())
        {
            if(data.hasIntValue(side.toString()))
                this.node.setDirectionalState(null,side, data.getEnumValue(side.toString(),DirectionalSettingsState.class,DirectionalSettingsState.NONE));
        }
    }

    //Nothing more to add, the label should be enough
    @Override
    protected void writeLines(SavedSettingData.NodeAccess data, Consumer<Component> lineWriter) {
        int inputCount = 0;
        int outputCount = 0;
        for(Direction side : Direction.values())
        {
            if(data.hasIntValue(side.toString()))
            {
                DirectionalSettingsState state = EnumUtil.enumFromOrdinal(data.getIntValue(side.toString()),DirectionalSettingsState.values(),DirectionalSettingsState.NONE);
                if(state.allowsInputs())
                    inputCount++;
                if(state.allowsOutputs())
                    outputCount++;
            }
        }
        lineWriter.accept(LCText.DATA_ENTRY_INPUT_OUTPUT_SIDES_COUNT.get(inputCount,outputCount));
    }

}
