package io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list.data.MutableChainData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list.data.MutableMasterCoinList;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MCLChainSelectScreen extends MCLSubscreen implements IScrollable {

    public MCLChainSelectScreen(Screen parentScreen) { super(new MutableMasterCoinList(),parentScreen); }

    @Override
    protected void addTitleSections(List<Component> list) { }

    private int scroll = 0;
    private int visibleChains = 1;

    @Override
    public int currentScroll() { return this.scroll; }
    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.visibleChains,this.getData().size()); }

    TextBoxWrapper<String> newChainInput;

    @Override
    protected void initialize(ScreenArea screenArea) {

        int centerX = screenArea.centerX();

        //Calculate visible chains
        this.visibleChains = Math.max(1,(screenArea.height - this.footerSize() - this.headerSize() - 50)/25);
        //Validate the scroll since the screen may have resized
        this.validateScroll();
        int yPos = this.headerSize() + 5;
        for(int i = 0; i < this.visibleChains; ++i)
        {
            final int index = i;
            //Add Chain Selection Button
            this.addChild(EasyTextButton.builder()
                    .position(centerX - 100,yPos)
                    .width(200)
                    .text(() -> this.getButtonText(index))
                    .pressAction(() -> this.selectChain(index))
                    .addon(EasyAddonHelper.tooltips(() -> this.getButtonTooltip(index)))
                    .addon(EasyAddonHelper.visibleCheck(() -> this.hasDataEntry(index)))
                    .build());
            yPos += 25;
        }

        //Create Chain Input & Button
        this.newChainInput = this.addChild(TextInputUtil.stringBuilder()
                .position(centerX - 100,screenArea.height - this.footerSize() - 45)
                .width(200)
                .maxLength(16)
                .filter(TextInputUtil::noEmptySpaces)
                .wrap().addon(EasyAddonHelper.visibleCheck(this::canEdit)).build());

        this.addChild(EasyTextButton.builder()
                .position(centerX - 100, screenArea.height - this.footerSize() - 25)
                .width(200)
                .text(LCText.CONFIG_MCL_CHAIN_SELECT_NEW_CHAIN)
                .pressAction(this::createNewChain)
                .addon(EasyAddonHelper.activeCheck(this::validChainInput))
                .addon(EasyAddonHelper.visibleCheck(this::canEdit))
                .build());

        //Scroll Area
        this.addChild(ScrollListener.builder().area(screenArea).listener(this).build());
        this.addChild(ScrollBarWidget.builder()
                .position(centerX + 110,this.headerSize() + 5)
                .height((this.visibleChains * 25) - 5)
                .scrollable(this)
                .build());


        //Leave without Saving button
        this.addChild(EasyTextButton.builder()
                .position(centerX - 100,screenArea.height - BOTTOM_BUTTON_OFFSET)
                .width(98)
                .text(this::getBackButtonText)
                .pressAction(this::onClose)
                .build());
        //Save Changes button
        this.addChild(EasyTextButton.builder()
                .position(centerX + 2, screenArea.height - BOTTOM_BUTTON_OFFSET)
                .width(98)
                .text(LCText.CONFIG_MCL_SAVE_CHANGES.get())
                .addon(EasyAddonHelper.activeCheck(this::canEdit))
                .pressAction(this::uploadChanges)
                .build());

    }

    private Component getBackButtonText() { return this.canEdit() ? LCText.CONFIG_MCL_EXIT_WITHOUT_SAVING.get() : LCText.CONFIG_BACK.get(); }

    private boolean hasDataEntry(int localIndex) { return this.getDataEntry(localIndex) != null; }

    private List<String> sortedKeyList()
    {
        List<String> keys = new ArrayList<>(this.getData().keySet());
        keys.sort(String::compareToIgnoreCase);
        return keys;
    }

    @Nullable
    private MutableChainData getDataEntry(int localIndex)
    {
        int trueIndex = localIndex + this.scroll;
        List<String> keyList = this.sortedKeyList();
        if(trueIndex < 0 || trueIndex >= keyList.size())
            return null;
        return this.getData().get(keyList.get(trueIndex));
    }

    private Component getButtonText(int localIndex)
    {
        MutableChainData chain = this.getDataEntry(localIndex);
        if(chain != null)
            return chain.displayName.copy();
        return EasyText.empty();
    }

    private List<Component> getButtonTooltip(int localIndex)
    {
        List<Component> tooltip = new ArrayList<>();
        MutableChainData chain = this.getDataEntry(localIndex);
        if(chain != null)
            tooltip.addAll(LCText.CONFIG_MCL_CHAIN_SELECT_CHAIN_INFO.get(chain.getChain(),chain.coreChain.entryCount(),chain.sideChains.size(),LCText.GUI_SETTINGS_VALUE_TRUE_FALSE.get(chain.isEvent)));
        return tooltip;
    }

    private void selectChain(int localIndex)
    {
        MutableChainData chain = this.getDataEntry(localIndex);
        if(chain != null)
            this.selectChain(chain);
    }

    private void selectChain(MutableChainData chain) { this.minecraft.setScreen(new MCLChainEditScreen(this,chain)); }

    private boolean validChainInput()
    {
        if(this.newChainInput == null)
            return false;
        return this.validChainID(this.newChainInput.getValue());
    }

    private boolean validChainID(String newChainID) { return !newChainID.isBlank() && !this.getData().containsKey(newChainID); }

    private void createNewChain()
    {
        if(this.newChainInput != null)
        {
            String newChainID = this.newChainInput.getValue();
            if(this.validChainID(newChainID))
            {
                MutableChainData newChain = this.createChain(newChainID);
                newChain.displayName = TextEntry.chain(newChainID).get();
                this.newChainInput.setValue("");
                this.selectChain(newChain);
            }
        }
    }

    private void uploadChanges()
    {
        //TODO turn MutableMasterCoinList into MasterCoinList data and forcibly load it (or save it to file if client-side)
    }

}