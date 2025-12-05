package io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list.data.MutableChainData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;

public class MCLChainEditScreen extends MCLChainSubscreen {

    public MCLChainEditScreen(MCLSubscreen parentScreen, MutableChainData chain) { super(parentScreen,chain); }

    @Override
    protected void initialize(ScreenArea screenArea) {

        int centerX = screenArea.centerX();
        int centerY = screenArea.centerY();

        //Chain ID
        this.addChild(TextInputUtil.stringBuilder()
                .position(centerX - 100,centerY - 100)
                .width(200)
                .startingValue(this.chain.getChain())
                .filter(TextInputUtil::noEmptySpaces)
                .handler(this.chain::renameChain)
                .wrap()
                .addon(EasyAddonHelper.activeCheck(this::canEdit))
                .build());

        //Chain Display Name
        this.addChild(TextInputUtil.textBuilder()
                .position(centerX - 150,centerY - 65)
                .width(300)
                .startingValue(this.chain.displayName)
                .handler(text -> this.chain.displayName = text)
                .wrap()
                .addon(EasyAddonHelper.activeCheck(this::canEdit))
                .build());

        //Event Toggle
        this.addChild(EasyTextButton.builder()
                .position(centerX - 100,centerY - 40)
                .width(200)
                .text(() -> LCText.CONFIG_MCL_CHAIN_EDIT_EVENT_CHAIN.get(LCText.GUI_SETTINGS_VALUE_TRUE_FALSE.get(this.chain.isEvent)))
                .pressAction(() -> this.chain.isEvent = !this.chain.isEvent)
                .addon(EasyAddonHelper.activeCheck(this::canEdit))
                .build());

        //Input Type
        this.addChild(EasyTextButton.builder()
                .position(centerX - 100, centerY - 15)
                .width(200)
                .text(() -> LCText.CONFIG_MCL_CHAIN_EDIT_INPUT_TYPE.get(LCText.CONFIG_MCL_CHAIN_EDIT_INPUT_TYPE_VALUES.get(this.chain.inputType).get()))
                .addon(EasyAddonHelper.activeCheck(this::canEdit))
                .pressAction(() -> this.chain.inputType = EnumUtil.nextEnum(this.chain.inputType))
                .altPressAction(() -> this.chain.inputType = EnumUtil.previousEnum(this.chain.inputType))
                .build());

        //Core Chain
        this.addChild(EasyTextButton.builder()
                .position(centerX - 100,centerY + 10)
                .width(200)
                .text(LCText.CONFIG_MCL_CHAIN_EDIT_CORE_CHAIN)
                .pressAction(this::openCoreChains)
                .build());

        //Core Chain
        this.addChild(EasyTextButton.builder()
                .position(centerX - 100,centerY + 35)
                .width(200)
                .text(LCText.CONFIG_MCL_CHAIN_EDIT_SIDE_CHAINS)
                .pressAction(this::openSideChains)
                .build());

        //Display Data
        this.addChild(EasyTextButton.builder()
                .position(centerX - 100,centerY + 60)
                .width(200)
                .text(LCText.CONFIG_MCL_CHAIN_EDIT_DISPLAY_DATA)
                .pressAction(this::openDisplayData)
                .build());

        //ATM Data
        this.addChild(EasyTextButton.builder()
                .position(centerX - 100, centerY + 85)
                .width(200)
                .text(LCText.CONFIG_MCL_CHAIN_EDIT_ATM_DATA)
                .pressAction(this::openATMData)
                .build());

        //Back Button
        this.addChild(EasyTextButton.builder()
                .position(centerX - 150,screenArea.height - BOTTOM_BUTTON_OFFSET)
                .width(145)
                .text(LCText.CONFIG_BACK)
                .pressAction(this::onClose)
                .build());
        //Delete Button
        this.addChild(EasyTextButton.builder()
                .position(centerX + 5, screenArea.height - BOTTOM_BUTTON_OFFSET)
                .width(145)
                .text(LCText.CONFIG_MCL_CHAIN_EDIT_DELETE_CHAIN)
                .addon(EasyAddonHelper.activeCheck(this::canDelete))
                .pressAction(this::deleteChain)
                .build());


    }

    @Override
    protected void renderAdditionalBG(EasyGuiGraphics gui) {
        //Render labels
        int centerX = this.getArea().centerX();
        int centerY = this.getArea().centerY();
        TextRenderUtil.drawCenteredText(gui,LCText.CONFIG_MCL_CHAIN_EDIT_CHAIN_ID.get(),centerX,centerY - 110,0xFFFFFF,true);
        TextRenderUtil.drawCenteredText(gui,LCText.CONFIG_MCL_CHAIN_EDIT_DISPLAY_NAME.get(),centerX,centerY - 75,0xFFFFFF,true);
    }

    private void openCoreChains()
    {
        //TODO
    }

    private void openSideChains()
    {
        //TODO
    }

    private void openDisplayData()
    {
        //TODO
    }

    private void openATMData() { this.minecraft.setScreen(new MCLATMDataEditScreen(this)); }

    private boolean canDelete() { return this.canEdit() && !this.chain.getChain().equals(CoinAPI.MAIN_CHAIN); }

    private void deleteChain()
    {
        if(this.canDelete())
        {
            this.getData().remove(this.chain.getChain());
            this.onClose();
        }
    }

}