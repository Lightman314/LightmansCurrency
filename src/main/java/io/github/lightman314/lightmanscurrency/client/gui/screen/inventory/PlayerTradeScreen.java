package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.input.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.menus.PlayerTradeMenu;
import io.github.lightman314.lightmanscurrency.network.message.playertrading.CPacketPlayerTradeInteraction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class PlayerTradeScreen extends EasyMenuScreen<PlayerTradeMenu> {

    public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/player_trading.png");

    private MoneyValueWidget valueInput;

    private EasyButton buttonPropose;
    private EasyButton buttonAccept;

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
    }

    @Override
    protected void renderBG(@Nonnull EasyGuiGraphics gui)
    {
        gui.resetColor();

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

    @Override
    protected void initialize(ScreenArea screenArea) {

        this.valueInput = this.addChild(new MoneyValueWidget(screenArea.pos, this.valueInput, MoneyValue.empty(), this::onValueChanged));
        this.valueInput.allowFreeInput = false;

        this.buttonPropose = this.addChild(new EasyTextButton(screenArea.pos.offset(8, 110 + MoneyValueWidget.HEIGHT), 70, 20, LCText.BUTTON_PLAYER_TRADING_PROPOSE.get(), this::OnPropose));

        this.buttonAccept = this.addChild(new EasyTextButton(screenArea.pos.offset(98, 110 + MoneyValueWidget.HEIGHT), 70, 20,  LCText.BUTTON_PLAYER_TRADING_ACCEPT.get(), this::OnAccept));
        this.buttonAccept.active = false;

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

}
