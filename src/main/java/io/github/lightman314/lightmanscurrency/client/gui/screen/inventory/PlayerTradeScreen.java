package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.PlayerTradeMenu;
import io.github.lightman314.lightmanscurrency.network.message.playertrading.CPacketPlayerTradeInteraction;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class PlayerTradeScreen extends EasyMenuScreen<PlayerTradeMenu> implements IScrollable {

    public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/container/player_trading.png");
    public static final ResourceLocation GUI_CHAT_TEXTURE = VersionUtil.lcResource("textures/gui/container/player_trading_chat.png");
    public static final ResourceLocation GUI_MONEY_TEXTURE = VersionUtil.lcResource("textures/gui/container/player_trading_money.png");

    private int scroll = 0;
    private static final int CHAT_ROWS = 11;
    private static final int CHAT_SIZE = 10;

    private MoneyValueWidget valueInput;

    private EasyButton buttonPropose;
    private EasyButton buttonAccept;

    private IconButton buttonToggleMoneyMode;
    private boolean moneyMode = false;

    private IconButton buttonToggleChat;
    private boolean chatWarning = false;
    private EditBox chatBox;
    private boolean chatMode = false;
    private ScrollListener chatScrollListener;

    private final List<FormattedCharSequence> chatHistory = new ArrayList<>();

    private void setShaderColorForState(@Nonnull EasyGuiGraphics gui, int state) {
        switch (state) {
            case 1 -> gui.setColor(0f, 1f, 1f);
            case 2 -> gui.setColor(0f, 1f, 0f);
            default -> gui.setColor(139f/255f, 139f/255f, 139f/255f);
        }
    }

    public PlayerTradeScreen(PlayerTradeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.resize(176, 222);
        this.menu.setChatReceiver(this::receiveChat);
    }

    @Override
    protected void renderBG(@Nonnull EasyGuiGraphics gui)
    {
        gui.resetColor();

        if(this.chatMode)
        {
            //Render Chat History
            gui.renderNormalBackground(GUI_CHAT_TEXTURE,this);

            int yPos = 106;
            for(int i = this.scroll; i < CHAT_ROWS + this.scroll && i < this.chatHistory.size(); ++i)
            {
                gui.drawString(this.chatHistory.get(i), 7, yPos, 0x404040);
                yPos -= CHAT_SIZE;
            }
        }
        else
        {
            if(this.moneyMode)
            {
                //Render Money BG
                gui.renderNormalBackground(GUI_MONEY_TEXTURE,this);
            }
            else
            {
                //Render Normal BG
                gui.renderNormalBackground(GUI_TEXTURE, this);

                //Render my arrow
                this.setShaderColorForState(gui, this.menu.myState());
                gui.blit(GUI_TEXTURE, 77, 50, this.getXSize(), 0, 22, 15);

                //Render their arrow
                this.setShaderColorForState(gui, this.menu.otherState());
                gui.blit(GUI_TEXTURE, 77, 65, this.getXSize(), 15, 22, 15);

                gui.resetColor();

            }

            //Draw names
            Component leftName = this.menu.isHost() ? this.menu.getTradeData().getHostName() : this.menu.getTradeData().getGuestName();
            Component rightName = this.menu.isHost() ? this.menu.getTradeData().getGuestName() : this.menu.getTradeData().getHostName();
            gui.drawString(leftName, 8, 6, 0x404040);
            gui.drawString(rightName, this.getXSize() - 8 - this.font.width(rightName), 6, 0x404040);

            //Draw money values
            leftName = this.menu.isHost() ? this.menu.getTradeData().getHostMoney().getText() : this.menu.getTradeData().getGuestMoney().getText();
            rightName = this.menu.isHost() ? this.menu.getTradeData().getGuestMoney().getText() : this.menu.getTradeData().getHostMoney().getText();
            gui.drawString(leftName, 8, 16, 0x404040);
            gui.drawString(rightName, this.getXSize() - 8 - this.font.width(rightName), 16, 0x404040);

        }

    }

    @Override
    protected void initialize(ScreenArea screenArea) {

        this.valueInput = this.addChild(MoneyValueWidget.builder()
                .position(screenArea.pos.offset(0,30))
                .old(this.valueInput)
                .valueHandler(this::onValueChanged)
                .blockFreeInputs()
                .build());
        this.valueInput.setVisible(false);

        this.buttonPropose = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(8,110))
                .width(70)
                .text(LCText.BUTTON_PLAYER_TRADING_PROPOSE)
                .pressAction(this::OnPropose)
                .build());

        this.buttonAccept = this.addChild(EasyTextButton.builder()
                .position(screenArea.pos.offset(98,110))
                .width(70)
                .text(LCText.BUTTON_PLAYER_TRADING_ACCEPT)
                .pressAction(this::OnAccept)
                .build());
        this.buttonAccept.active = false;

        this.buttonToggleMoneyMode = this.addChild(IconButton.builder()
                .position(screenArea.pos.offset(screenArea.width,20))
                .pressAction(this::ToggleMoneyMode)
                .icon(this::getToggleMoneyIcon)
                .addon(EasyAddonHelper.activeCheck(() -> !this.chatMode))
                .addon(EasyAddonHelper.tooltip(this::getToggleMoneyTooltip))
                .build());

        this.buttonToggleChat = this.addChild(IconButton.builder()
                .position(screenArea.pos.offset(screenArea.width,0))
                .pressAction(this::ToggleChatMode)
                .icon(this::getToggleIcon)
                .color(() -> this.chatWarning ? 0xFFFF00 : 0xFFFFFF)
                .addon(EasyAddonHelper.tooltip(this::getToggleTooltip))
                .build());
        this.chatBox = this.addChild(new EditBox(this.font, screenArea.pos.x + 9, screenArea.pos.y + 120, screenArea.width - 22, 12, EasyText.empty()));
        this.chatBox.setBordered(false);
        this.chatBox.setMaxLength(256);

        this.chatScrollListener = this.addChild(ScrollListener.builder()
                .area(screenArea.ofSize(screenArea.width,118))
                .listener(this)
                .invert()
                .build());

        this.validateWidgetStates();

    }

    @Override
    protected void screenTick() {

        int myState = this.menu.myState();
        int otherState = this.menu.otherState();
        //Lock Value Input if state > 0
        this.valueInput.active = myState < 1;
        //Update Propose Button text/active state
        this.buttonPropose.active = myState < 2;
        this.buttonPropose.setMessage(myState <= 0 ?  LCText.BUTTON_PLAYER_TRADING_PROPOSE.get() :  LCText.BUTTON_PLAYER_TRADING_CANCEL.get());
        //Update Accept Button text/active state
        this.buttonAccept.active = myState > 0 && otherState > 0;
        this.buttonAccept.setMessage(myState <= 1 ? LCText.BUTTON_PLAYER_TRADING_ACCEPT.get() : LCText.BUTTON_PLAYER_TRADING_CANCEL.get());

        if(this.chatMode)
            this.setFocused(this.chatBox);

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifier) {
        if(this.chatMode && !this.chatBox.getValue().isBlank())
        {
            //Send chat message when hitting "Enter"/"Return"
            if(keyCode == InputConstants.KEY_RETURN)
            {
                this.menu.SendChatToServer(this.chatBox.getValue());
                this.chatBox.setValue("");
                return true;
            }
        }
        //Manually block closing by inventory key, to allow usage of all letters while typing player names, etc.
        if (this.minecraft.options.keyInventory.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode))) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifier);
    }

    private void onValueChanged(MoneyValue newValue) {
        CompoundTag message = new CompoundTag();
        MoneyView availableFunds = this.menu.getAvailableFunds();
        if(!availableFunds.containsValue(newValue))
        {
            newValue = availableFunds.valueOf(newValue.getUniqueName());
            this.valueInput.changeValue(newValue);
        }
        message.put("ChangeMoney", newValue.save());
        new CPacketPlayerTradeInteraction(this.menu.tradeID, message).send();
    }

    private void OnPropose(EasyButton button) {
        CompoundTag message = new CompoundTag();
        message.putBoolean("TogglePropose", true);
        new CPacketPlayerTradeInteraction(this.menu.tradeID, message).send();
    }

    private void OnAccept(EasyButton button) {
        CompoundTag message = new CompoundTag();
        message.putBoolean("ToggleActive", true);
        new CPacketPlayerTradeInteraction(this.menu.tradeID, message).send();
    }

    private void ToggleMoneyMode(EasyButton button) {
        this.moneyMode = !this.moneyMode;
        this.valueInput.setVisible(!this.valueInput.visible);
        this.validateWidgetStates();
    }

    private void ToggleChatMode(EasyButton button) {
        this.chatMode = !this.chatMode;
        if(this.chatMode)
            this.chatWarning = false;
        this.validateWidgetStates();
    }

    private IconData getToggleIcon() { return this.chatWarning ? ItemIcon.ofItem(Items.WRITABLE_BOOK) : ItemIcon.ofItem(Items.BOOK); }
    private Component getToggleTooltip() { return this.chatMode ? LCText.TOOLTIP_PLAYER_TRADING_CHAT_CLOSE.get() : LCText.TOOLTIP_PLAYER_TRADING_CHAT_OPEN.get(); }

    private IconData getToggleMoneyIcon() { return this.moneyMode ? ItemIcon.ofItem(Items.CHEST) : ItemIcon.ofItem(ModBlocks.COINPILE_GOLD); }
    private Component getToggleMoneyTooltip() { return this.moneyMode ? LCText.TOOLTIP_PLAYER_TRADING_MONEY_CLOSE.get() : LCText.TOOLTIP_PLAYER_TRADING_MONEY_OPEN.get(); }

    private void validateWidgetStates()
    {
        this.chatBox.setVisible(this.chatScrollListener.active = this.chatMode);
        this.buttonAccept.visible = this.buttonPropose.visible = !this.chatMode && !this.moneyMode;
        this.valueInput.visible = this.moneyMode && !this.chatMode;
        if(this.chatMode || this.moneyMode)
            this.menu.hideSlots();
        else
            this.menu.showSlots();
    }

    private void receiveChat(@Nonnull Component chat)
    {
        //Split the message into multiple lines so that scrolling doesn't have to factor in message size
        List<FormattedCharSequence> newLines = this.font.split(chat, this.getXSize() - 14);
        for(FormattedCharSequence line : newLines)
            this.chatHistory.add(0,line);
        if(!this.chatMode)
            this.chatWarning = true;
    }

    @Override
    public int currentScroll() { return this.scroll; }

    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }

    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(CHAT_ROWS, this.chatHistory.size()); }

}