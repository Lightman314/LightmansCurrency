package io.github.lightman314.lightmanscurrency.api.money.coins.atm.data;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class ATMPageManager {

    private static String lastSelected = CoinAPI.MAIN_CHAIN;

    private ATMData selectedData = null;

    private final Consumer<Object> addChild;
    private final Consumer<Object> removeChild;
    private final Consumer<String> commandProcessor;

    private final List<ATMData> validData;

    private final List<Object> buttons = new ArrayList<>();

    private ScreenPosition corner = ScreenPosition.ZERO;

    private final Predicate<ATMExchangeButton> selected;

    private ATMPageManager(@Nonnull Player player, @Nonnull Consumer<Object> addChild, @Nonnull Consumer<Object> removeChild, @Nonnull Consumer<String> commandProcessor, @Nonnull Predicate<ATMExchangeButton> selected)
    {
        this.addChild = addChild;
        this.removeChild = removeChild;
        this.commandProcessor = commandProcessor;
        this.selected = selected;
        Map<String,ATMData> mapTemp = new HashMap<>();
        for(ChainData chain : CoinAPI.getApi().AllChainData())
        {
            if(chain.hasATMData() && chain.isVisibleTo(player))
                mapTemp.put(chain.chain, chain.getAtmData());
        }
        this.validData = ImmutableList.copyOf(mapTemp.values());
        if(mapTemp.containsKey(lastSelected))
            this.selectedData = mapTemp.get(lastSelected);
        else if(!this.validData.isEmpty())
        {
            this.selectedData = this.validData.get(0);
            lastSelected = this.selectedData.chain.chain;
        }

    }

    public void initialize(@Nonnull ScreenArea screen)
    {
        this.corner = screen.pos;
        //Create dropdown widget if more than one valid option
        if(this.validData.size() > 1)
        {
            this.addChild.accept(DropdownWidget.builder()
                    .position(screen.pos.offset(screen.width - 70, 6))
                    .width(64)
                    .selected(this.validData.indexOf(this.selectedData))
                    .selectAction(this::changeSelection)
                    .options(this.getOptions())
                    .build());
        }

        this.buttons.clear();
        if(this.selectedData != null)
        {
            for(ATMExchangeButtonData data : this.selectedData.getExchangeButtons())
                this.addButton(data);
        }
    }

    private void addButton(@Nonnull ATMExchangeButtonData data)
    {
        ATMExchangeButton button = ATMExchangeButton.builder(data)
                .screenCorner(this.corner)
                .commandHandler(this.commandProcessor)
                .selected(this.selected)
                .build();
        this.buttons.add(button);
        this.addChild.accept(button);
    }

    private List<Component> getOptions()
    {
        List<Component> text = new ArrayList<>();
        for(ATMData data : this.validData)
            text.add(data.chain.getDisplayName());
        return text;
    }

    public void changeSelection(int newSelection)
    {
        if(newSelection >= 0 && newSelection < this.validData.size())
        {
            ATMData newData = this.validData.get(newSelection);
            if(newData == this.selectedData)
                return;
            this.selectedData = newData;
            //Remove old buttons
            for(Object b : this.buttons)
                this.removeChild.accept(b);
            this.buttons.clear();
            //Add new buttons
            for(ATMExchangeButtonData data : this.selectedData.getExchangeButtons())
                this.addButton(data);
        }
    }


    public static ATMPageManager create(@Nonnull Player player, @Nonnull Consumer<Object> addChild, @Nonnull Consumer<Object> removeChild, @Nonnull Consumer<String> commandProcessor) { return new ATMPageManager(player, addChild, removeChild, commandProcessor, Predicates.alwaysFalse()); }
    public static ATMPageManager create(@Nonnull Player player, @Nonnull Consumer<Object> addChild, @Nonnull Consumer<Object> removeChild, @Nonnull Consumer<String> commandProcessor,@Nonnull Predicate<ATMExchangeButton> selected) { return new ATMPageManager(player, addChild, removeChild, commandProcessor, selected); }

}