package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.PlayerTradeMenu;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.playertrading.CMessagePlayerTradeInteraction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class PlayerTradeScreen extends EasyMenuScreen<PlayerTradeMenu> {

    public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/player_trading.png");

    private CoinValueInput valueInput;

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
        this.resize(176, 222 + CoinValueInput.HEIGHT);
    }

    @Override
    protected void renderBG(@Nonnull EasyGuiGraphics gui)
    {
        gui.resetColor();

        //Render BG
        gui.blit(GUI_TEXTURE, 0, CoinValueInput.HEIGHT, 0, 0, this.getXSize(), this.getYSize() - CoinValueInput.HEIGHT);

        //Render my arrow
        this.setShaderColorForState(gui, this.menu.myState());
        gui.blit(GUI_TEXTURE, 77, CoinValueInput.HEIGHT + 50, this.getXSize(), 0, 22, 15);

        //Render their arrow
        this.setShaderColorForState(gui, this.menu.otherState());
        gui.blit(GUI_TEXTURE, 77, CoinValueInput.HEIGHT + 65, this.getXSize(), 15, 22, 15);

        gui.resetColor();

        //Draw names
        Component leftName = this.menu.isHost() ? this.menu.getTradeData().getHostName() : this.menu.getTradeData().getGuestName();
        Component rightName = this.menu.isHost() ? this.menu.getTradeData().getGuestName() : this.menu.getTradeData().getHostName();
        gui.drawString(leftName, 8, 6 + CoinValueInput.HEIGHT, 0x404040);
        gui.drawString(rightName, this.getXSize() - 8 - this.font.width(rightName), 6 + CoinValueInput.HEIGHT, 0x404040);

        //Draw money values
        leftName = this.menu.isHost() ? this.menu.getTradeData().getHostMoney().getComponent() : this.menu.getTradeData().getGuestMoney().getComponent();
        rightName = this.menu.isHost() ? this.menu.getTradeData().getGuestMoney().getComponent() : this.menu.getTradeData().getHostMoney().getComponent();
        gui.drawString(leftName, 8, 16 + CoinValueInput.HEIGHT, 0x404040);
        gui.drawString(rightName, this.getXSize() - 8 - this.font.width(rightName), 16 + CoinValueInput.HEIGHT, 0x404040);


    }

    @Override
    protected void initialize(ScreenArea screenArea) {

        this.valueInput = this.addChild(new CoinValueInput(screenArea.pos, EasyText.empty(), CoinValue.EMPTY, this.font, this::onValueChanged));
        this.valueInput.allowFreeToggle = false;

        this.buttonPropose = this.addChild(new EasyTextButton(screenArea.pos.offset(8, 110 + CoinValueInput.HEIGHT), 70, 20, EasyText.translatable("gui.lightmanscurrency.button.player_trading.propose"), this::OnPropose));

        this.buttonAccept = this.addChild(new EasyTextButton(screenArea.pos.offset(98, 110 + CoinValueInput.HEIGHT), 70, 20, EasyText.translatable("gui.lightmanscurrency.button.player_trading.accept"), this::OnAccept));
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
        this.buttonPropose.setMessage(EasyText.translatable(myState <= 0 ? "gui.lightmanscurrency.button.player_trading.propose" : "gui.lightmanscurrency.button.player_trading.cancel"));
        //Update Accept Button text/active state
        this.buttonAccept.active = myState > 0 && otherState > 0;
        this.buttonAccept.setMessage(EasyText.translatable(myState <= 1 ? "gui.lightmanscurrency.button.player_trading.accept" : "gui.lightmanscurrency.button.player_trading.cancel"));

    }

    private void onValueChanged(CoinValue newValue) {
        CompoundTag message = new CompoundTag();
        CoinValue availableFunds = this.menu.getAvailableFunds();
        if(newValue.getValueNumber() > availableFunds.getValueNumber())
        {
            newValue = availableFunds;
            this.valueInput.setCoinValue(newValue);
        }
        message.put("ChangeMoney", newValue.save());
        LightmansCurrencyPacketHandler.instance.sendToServer(new CMessagePlayerTradeInteraction(this.menu.tradeID, message));
    }

    private void OnPropose(EasyButton button) {
        CompoundTag message = new CompoundTag();
        message.putBoolean("TogglePropose", true);
        LightmansCurrencyPacketHandler.instance.sendToServer(new CMessagePlayerTradeInteraction(this.menu.tradeID, message));
    }

    private void OnAccept(EasyButton button) {
        CompoundTag message = new CompoundTag();
        message.putBoolean("ToggleActive", true);
        LightmansCurrencyPacketHandler.instance.sendToServer(new CMessagePlayerTradeInteraction(this.menu.tradeID, message));
    }

}
