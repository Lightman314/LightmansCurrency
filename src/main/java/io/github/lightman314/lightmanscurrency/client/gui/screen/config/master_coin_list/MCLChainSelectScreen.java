package io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list.data.MutableMasterCoinList;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class MCLChainSelectScreen extends MCLSubscreen {

    public MCLChainSelectScreen(Screen parentScreen) {
        super(new MutableMasterCoinList(),parentScreen);
    }

    @Override
    protected void addTitleSections(List<Component> list) {

    }

    @Override
    protected void initialize(ScreenArea screenArea) {

        int centerX = screenArea.centerX();




        //Leave without Saving button
        this.addChild(EasyTextButton.builder()
                .position(centerX - 150,screenArea.height - BOTTOM_BUTTON_OFFSET)
                .width(145)
                .text(this::getBackButtonText)
                .pressAction(this::onClose)
                .build());
        //Save Changes button
        this.addChild(EasyTextButton.builder()
                .position(centerX + 5, screenArea.height - BOTTOM_BUTTON_OFFSET)
                .width(145)
                .text(LCText.CONFIG_MCL_SAVE_CHANGES.get())
                .addon(EasyAddonHelper.activeCheck(this::canEdit))
                .pressAction(this::uploadChanges)
                .build());


    }

    private Component getBackButtonText() { return this.canEdit() ? LCText.CONFIG_MCL_EXIT_WITHOUT_SAVING.get() : LCText.CONFIG_BACK.get(); }

    private void uploadChanges()
    {

    }

}
