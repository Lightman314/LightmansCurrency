package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.PlayerTradeMenu;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.playertrading.CMessagePlayerTradeInteraction;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class PlayerTradeScreen extends ContainerScreen<PlayerTradeMenu> {

    public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/player_trading.png");

    private CoinValueInput valueInput;

    private Button buttonPropose;
    private Button buttonAccept;

    private void setShaderColorForState(int state) {
        switch (state) {
            case 1: RenderUtil.color4f(0f, 1f, 1f, 1f); return;
            case 2: RenderUtil.color4f(0f, 1f, 0f, 1f); return;
            default: RenderUtil.color4f(139f/255f, 139f/255f, 139f/255f, 1f);
        }
    }

    public PlayerTradeScreen(PlayerTradeMenu menu, PlayerInventory inventory, ITextComponent title) {
        super(menu, inventory, title);
        this.imageHeight = 222 + CoinValueInput.HEIGHT;
        this.imageWidth = 176;
    }

    @Override
    protected void renderBg(@Nonnull MatrixStack pose, float partialTicks, int mouseX, int mouseY)
    {

        this.renderBackground(pose);

        RenderUtil.bindTexture(GUI_TEXTURE);
        RenderUtil.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        //Render BG
        this.blit(pose, this.leftPos, this.topPos + CoinValueInput.HEIGHT, 0, 0, this.imageWidth, this.imageHeight - CoinValueInput.HEIGHT);

        //Render my arrow
        this.setShaderColorForState(this.menu.myState());
        this.blit(pose, this.leftPos + 77, this.topPos + CoinValueInput.HEIGHT + 50, this.imageWidth, 0, 22, 15);

        //Render their arrow
        this.setShaderColorForState(this.menu.otherState());
        this.blit(pose, this.leftPos + 77, this.topPos + CoinValueInput.HEIGHT + 65, this.imageWidth, 15, 22, 15);

    }

    @Override
    protected void renderLabels(@Nonnull MatrixStack pose, int mouseX, int mouseY)
    {
        //Draw names
        ITextComponent leftName = this.menu.isHost() ? this.menu.getTradeData().getHostName() : this.menu.getTradeData().getGuestName();
        ITextComponent rightName = this.menu.isHost() ? this.menu.getTradeData().getGuestName() : this.menu.getTradeData().getHostName();
        this.font.draw(pose, leftName, 8f, 6f + CoinValueInput.HEIGHT, 0x404040);
        this.font.draw(pose, rightName, this.imageWidth - 8f - this.font.width(rightName), 6f + CoinValueInput.HEIGHT, 0x404040);

        //Draw money values
        leftName = this.menu.isHost() ? this.menu.getTradeData().getHostMoney().getComponent() : this.menu.getTradeData().getGuestMoney().getComponent();
        rightName = this.menu.isHost() ? this.menu.getTradeData().getGuestMoney().getComponent() : this.menu.getTradeData().getHostMoney().getComponent();
        this.font.draw(pose, leftName, 8f, 16f + CoinValueInput.HEIGHT, 0x404040);
        this.font.draw(pose, rightName, this.imageWidth - 8f - this.font.width(rightName), 16f + CoinValueInput.HEIGHT, 0x404040);

    }

    @Override
    protected void init() {
        super.init();

        this.valueInput = this.addButton(new CoinValueInput(this.leftPos, this.topPos, EasyText.empty(), CoinValue.EMPTY, this.font, this::onValueChanged, this::addButton));
        this.valueInput.allowFreeToggle = false;
        this.valueInput.init();

        this.buttonPropose = this.addButton(new Button(this.leftPos + 8, this.topPos + 110 + CoinValueInput.HEIGHT, 70, 20, EasyText.translatable("gui.lightmanscurrency.button.player_trading.propose"), this::OnPropose));

        this.buttonAccept = this.addButton(new Button(this.leftPos + 98, this.topPos + 110 + CoinValueInput.HEIGHT, 70, 20, EasyText.translatable("gui.lightmanscurrency.button.player_trading.accept"), this::OnAccept));
        this.buttonAccept.active = false;

    }

    @Override
    public void tick() {
        super.tick();

        this.valueInput.tick();

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
        CompoundNBT message = new CompoundNBT();
        CoinValue availableFunds = this.menu.getAvailableFunds();
        if(newValue.getRawValue() > availableFunds.getRawValue())
        {
            newValue = availableFunds;
            this.valueInput.setCoinValue(newValue);
        }
        newValue.save(message, "ChangeMoney");
        LightmansCurrencyPacketHandler.instance.sendToServer(new CMessagePlayerTradeInteraction(this.menu.tradeID, message));
    }

    private void OnPropose(Button button) {
        CompoundNBT message = new CompoundNBT();
        message.putBoolean("TogglePropose", true);
        LightmansCurrencyPacketHandler.instance.sendToServer(new CMessagePlayerTradeInteraction(this.menu.tradeID, message));
    }

    private void OnAccept(Button button) {
        CompoundNBT message = new CompoundNBT();
        message.putBoolean("ToggleActive", true);
        LightmansCurrencyPacketHandler.instance.sendToServer(new CMessagePlayerTradeInteraction(this.menu.tradeID, message));
    }

}