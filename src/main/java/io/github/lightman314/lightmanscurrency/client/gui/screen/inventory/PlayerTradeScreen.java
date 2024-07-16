package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.PlayerTradeMenu;
import io.github.lightman314.lightmanscurrency.network.message.playertrading.CPacketPlayerTradeInteraction;
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

    public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/player_trading.png");
    public static final ResourceLocation GUI_CHAT_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/player_trading_chat.png");

    private int scroll = 0;
    private static final int CHAT_ROWS = 11;
    private static final int CHAT_SIZE = 10;

    private MoneyValueWidget valueInput;

    private EasyButton buttonPropose;
    private EasyButton buttonAccept;

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
        this.resize(176, 222 + MoneyValueWidget.HEIGHT);
        this.menu.setChatReceiver(this::receiveChat);
    }

    @Override
    protected void renderBG(@Nonnull EasyGuiGraphics gui)
    {
        gui.resetColor();

        if(this.chatMode)
        {
            //Render Chat History
            gui.blit(GUI_CHAT_TEXTURE, 0, MoneyValueWidget.HEIGHT, 0, 0, this.getXSize(), this.getYSize() - MoneyValueWidget.HEIGHT);

            int yPos = 106 + MoneyValueWidget.HEIGHT;
            for(int i = this.scroll; i < CHAT_ROWS + this.scroll && i < this.chatHistory.size(); ++i)
            {
                gui.drawString(this.chatHistory.get(i), 7, yPos, 0x404040);
                yPos -= CHAT_SIZE;
            }
        }
        else
        {
            //Render BG
            gui.blit(GUI_TEXTURE, 0, MoneyValueWidget.HEIGHT, 0, 0, this.getXSize(), this.getYSize() - MoneyValueWidget.HEIGHT);

            //Render my arrow
            this.setShaderColorForState(gui, this.menu.myState());
            gui.blit(GUI_TEXTURE, 77, MoneyValueWidget.HEIGHT + 50, this.getXSize(), 0, 22, 15);

            //Render their arrow
            this.setShaderColorForState(gui, this.menu.otherState());
            gui.blit(GUI_TEXTURE, 77, MoneyValueWidget.HEIGHT + 65, this.getXSize(), 15, 22, 15);

            gui.resetColor();

            //Draw names
            Component leftName = this.menu.isHost() ? this.menu.getTradeData().getHostName() : this.menu.getTradeData().getGuestName();
            Component rightName = this.menu.isHost() ? this.menu.getTradeData().getGuestName() : this.menu.getTradeData().getHostName();
            gui.drawString(leftName, 8, 6 + MoneyValueWidget.HEIGHT, 0x404040);
            gui.drawString(rightName, this.getXSize() - 8 - this.font.width(rightName), 6 + MoneyValueWidget.HEIGHT, 0x404040);

            //Draw money values
            leftName = this.menu.isHost() ? this.menu.getTradeData().getHostMoney().getText() : this.menu.getTradeData().getGuestMoney().getText();
            rightName = this.menu.isHost() ? this.menu.getTradeData().getGuestMoney().getText() : this.menu.getTradeData().getHostMoney().getText();
            gui.drawString(leftName, 8, 16 + MoneyValueWidget.HEIGHT, 0x404040);
            gui.drawString(rightName, this.getXSize() - 8 - this.font.width(rightName), 16 + MoneyValueWidget.HEIGHT, 0x404040);

        }

    }

    @Override
    protected void initialize(ScreenArea screenArea) {

        this.valueInput = this.addChild(new MoneyValueWidget(screenArea.pos, this.valueInput, MoneyValue.empty(), this::onValueChanged));
        this.valueInput.allowFreeInput = false;

        this.buttonPropose = this.addChild(new EasyTextButton(screenArea.pos.offset(8, 110 + MoneyValueWidget.HEIGHT), 70, 20, LCText.BUTTON_PLAYER_TRADING_PROPOSE.get(), this::OnPropose));

        this.buttonAccept = this.addChild(new EasyTextButton(screenArea.pos.offset(98, 110 + MoneyValueWidget.HEIGHT), 70, 20,  LCText.BUTTON_PLAYER_TRADING_ACCEPT.get(), this::OnAccept));
        this.buttonAccept.active = false;

        this.buttonToggleChat = this.addChild(new IconButton(screenArea.pos.offset(screenArea.width, MoneyValueWidget.HEIGHT), this::ToggleChatMode, this::getToggleIcon)
                .withAddons(EasyAddonHelper.tooltip(this::getToggleTooltip)));
        this.chatBox = this.addChild(new EditBox(this.font, screenArea.pos.x + 9, screenArea.pos.y + 120 + MoneyValueWidget.HEIGHT, screenArea.width - 22, 12, EasyText.empty()));
        this.chatBox.setBordered(false);
        this.chatBox.setMaxLength(256);

        this.chatScrollListener = this.addChild(new ScrollListener(screenArea.ofSize(screenArea.width,118).offsetPosition(0,MoneyValueWidget.HEIGHT), this));
        this.chatScrollListener.inverted = true;

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
        this.buttonToggleChat.bgColor = this.chatWarning ? 0xFFFF00 : 0xFFFFFF;

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

    private void ToggleChatMode(EasyButton button) {
        this.chatMode = !this.chatMode;
        if(this.chatMode)
        {
            this.menu.hideSlots();
            this.chatWarning = false;
        }
        else
            this.menu.showSlots();
        this.validateWidgetStates();
    }

    private IconData getToggleIcon() { return this.chatWarning ? IconData.of(Items.WRITABLE_BOOK) : IconData.of(Items.BOOK); }
    private Component getToggleTooltip() { return this.chatMode ? LCText.TOOLTIP_PLAYER_TRADING_CHAT_CLOSE.get() : LCText.TOOLTIP_PLAYER_TRADING_CHAT_OPEN.get(); }

    private void validateWidgetStates()
    {
        this.chatBox.setVisible(this.chatScrollListener.active = this.chatMode);
        this.buttonAccept.visible = this.buttonPropose.visible = this.valueInput.visible = !this.chatMode;
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
