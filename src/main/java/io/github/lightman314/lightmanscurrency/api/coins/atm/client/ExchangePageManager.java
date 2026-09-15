package io.github.lightman314.lightmanscurrency.api.coins.atm.client;

import com.google.common.base.Predicates;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.interfaces.IWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.dropdown.DropdownOption;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.api.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.coins.atm.ATMData;
import io.github.lightman314.lightmanscurrency.api.coins.atm.ATMExchangeButtonData;
import io.github.lightman314.lightmanscurrency.api.coins.atm.commands.ATMCommand;
import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class ExchangePageManager implements IWidgetHolder {

    private static String lastSelected = CoinAPI.DEFAULT_CHAIN;

    private ATMData selectedData = null;

    private final IWidgetHolder holder;
    private final Consumer<ATMCommand> commandProcessor;

    private final List<ATMData> validData;

    private ScreenPosition corner = ScreenPosition.ZERO;
    private final List<Object> buttons = new ArrayList<>();
    private final Predicate<ATMCommand> selected;

    @Override
    public <T> T addChild(T child) { return this.holder.addChild(child); }

    @Override
    public void removeChild(Object child) { this.holder.removeChild(child); }

    @Override
    public void removeAllChildren() {
        for(Object c : this.buttons)
            this.removeChild(c);
        this.buttons.clear();
    }

    public ExchangePageManager(Player player, IWidgetHolder parent, Consumer<ATMCommand> commandProcessor) { this(player,parent,commandProcessor, Predicates.alwaysFalse()); }
    public ExchangePageManager(Player player, IWidgetHolder parent, Consumer<ATMCommand> commandProcessor, Predicate<ATMCommand> selected) {
        this.holder = parent;
        this.commandProcessor = commandProcessor;
        this.selected = selected;
        Map<String,ATMData> mapTemp = new HashMap<>();
        for(ChainData chain : LCApi.getCoinAPI().lookupAllChains()) {
            if(chain.hasATMData() && chain.isVisibleTo(player))
                mapTemp.put(chain.chain,chain.getAtmData());
        }
        this.validData = List.copyOf(mapTemp.values());
        if(mapTemp.containsKey(lastSelected))
            this.selectedData = mapTemp.get(lastSelected);
        else if(!this.validData.isEmpty()) {
            this.selectedData = this.validData.getFirst();
            lastSelected = this.selectedData.chain.chain;
        }
    }

    public void initialize(ScreenArea area) {
        this.corner = area.pos;

        if(this.validData.size() > 1) {
            this.addChild(DropdownWidget.builder()
                    .atPos(area.pos.offset(area.width - 70,6))
                    .ofWidth(64)
                    .withCurrentlySelected(this.validData.indexOf(this.selectedData))
                    .withHandler(this::changeSelection)
                    .withOptions(this.getOptions())
                    .build());
        }

        this.removeAllChildren();

        if(this.selectedData != null) {
            for(ATMExchangeButtonData data : this.selectedData.getExchangeButtons())
                this.addButton(data);
        }

    }

    private void addButton(ATMExchangeButtonData data) {
        ATMExchangeButton b = this.addChild(ATMExchangeButton.builder(data)
                .withCorner(this.corner)
                .withHandler(this.commandProcessor)
                .selected(this.selected)
                .build());
        this.buttons.add(b);
    }

    private List<DropdownOption> getOptions() {
        List<DropdownOption> result = new ArrayList<>();
        for(ATMData data : this.validData) {
            result.add(new DropdownOption(data.chain.getDisplayName(),data.chain.getDisplaySprite()));
        }
        return result;
    }

    public void changeSelection(int newSelection) {
        if(newSelection >= 0 && newSelection < this.validData.size()) {
            ATMData newData = this.validData.get(newSelection);
            if(newData == this.selectedData)
                return;
            this.selectedData = newData;
            lastSelected = this.selectedData.chain.chain;
            //Remove old buttons
            this.removeAllChildren();
            //Add new buttons
            for(ATMExchangeButtonData data : this.selectedData.getExchangeButtons())
                this.addButton(data);
        }
    }


}