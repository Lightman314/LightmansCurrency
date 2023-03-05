package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.common.menus.PlayerTradeMenu;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.playertrading.CMessagePlayerTradeInteraction;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class PlayerTradeScreen extends AbstractContainerScreen<PlayerTradeMenu> {

    public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/player_trading.png");

    private CoinValueInput valueInput;

    private Button buttonPropose;
    private Button buttonAccept;

    private void setShaderColorForState(int state) {
        switch (state) {
            case 1 -> RenderSystem.setShaderColor(0f, 1f, 1f, 1f);
            case 2 -> RenderSystem.setShaderColor(0f, 1f, 0f, 1f);
            default -> RenderSystem.setShaderColor(139f/255f, 139f/255f, 139f/255f, 1f);
        }
    }

    public PlayerTradeScreen(PlayerTradeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 222 + CoinValueInput.HEIGHT;
        this.imageWidth = 176;
    }

    @Override
    protected void renderBg(@NotNull PoseStack pose, float partialTicks, int mouseX, int mouseY)
    {

        this.renderBackground(pose);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

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
    protected void renderLabels(@NotNull PoseStack pose, int mouseX, int mouseY)
    {
        //Draw names
        Component leftName = this.menu.isHost() ? this.menu.getTradeData().getHostName() : this.menu.getTradeData().getGuestName();
        Component rightName = this.menu.isHost() ? this.menu.getTradeData().getGuestName() : this.menu.getTradeData().getHostName();
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

        this.valueInput = this.addRenderableWidget(new CoinValueInput(this.leftPos, this.topPos, Component.empty(), CoinValue.EMPTY, this.font, this::onValueChanged, this::addRenderableWidget));
        this.valueInput.allowFreeToggle = false;
        this.valueInput.init();

        this.buttonPropose = this.addRenderableWidget(new Button(this.leftPos + 8, this.topPos + 110 + CoinValueInput.HEIGHT, 70, 20, Component.translatable("gui.lightmanscurrency.button.player_trading.propose"), this::OnPropose));

        this.buttonAccept = this.addRenderableWidget(new Button(this.leftPos + 98, this.topPos + 110 + CoinValueInput.HEIGHT, 70, 20, Component.translatable("gui.lightmanscurrency.button.player_trading.accept"), this::OnAccept));
        this.buttonAccept.active = false;

    }

    @Override
    protected void containerTick() {
        super.containerTick();

        this.valueInput.tick();

        int myState = this.menu.myState();
        int otherState = this.menu.otherState();
        //Lock Value Input if state > 0
        this.valueInput.active = myState < 1;
        //Update Propose Button text/active state
        this.buttonPropose.active = myState < 2;
        this.buttonPropose.setMessage(Component.translatable(myState <= 0 ? "gui.lightmanscurrency.button.player_trading.propose" : "gui.lightmanscurrency.button.player_trading.cancel"));
        //Update Accept Button text/active state
        this.buttonAccept.active = myState > 0 && otherState > 0;
        this.buttonAccept.setMessage(Component.translatable(myState <= 1 ? "gui.lightmanscurrency.button.player_trading.accept" : "gui.lightmanscurrency.button.player_trading.cancel"));

    }

    private void onValueChanged(CoinValue newValue) {
        CompoundTag message = new CompoundTag();
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
        CompoundTag message = new CompoundTag();
        message.putBoolean("TogglePropose", true);
        LightmansCurrencyPacketHandler.instance.sendToServer(new CMessagePlayerTradeInteraction(this.menu.tradeID, message));
    }

    private void OnAccept(Button button) {
        CompoundTag message = new CompoundTag();
        message.putBoolean("ToggleActive", true);
        LightmansCurrencyPacketHandler.instance.sendToServer(new CMessagePlayerTradeInteraction(this.menu.tradeID, message));
    }

}
