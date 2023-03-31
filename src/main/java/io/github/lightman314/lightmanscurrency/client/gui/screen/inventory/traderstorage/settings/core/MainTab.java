package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.persistentdata.MessageAddPersistentTrader;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageAddOrRemoveTrade;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class MainTab extends SettingsSubTab {

    public MainTab(@Nonnull TraderSettingsClientTab parent) { super(parent); }

    EditBox nameInput;
    Button buttonSetName;
    Button buttonResetName;

    PlainButton buttonToggleBankLink;

    IconButton buttonToggleCreative;
    Button buttonAddTrade;
    Button buttonRemoveTrade;

    Button buttonSavePersistentTrader;
    EditBox persistentTraderIDInput;
    EditBox persistentTraderOwnerInput;

    @Nonnull
    @Override
    public @NotNull IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }

    @Override
    public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.settings.name"); }

    @Override
    public boolean canOpen() { return true; }

    @Override
    public void onOpen() {

        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;

        this.nameInput = this.addWidget(new EditBox(this.font, this.screen.getGuiLeft() + 20, this.screen.getGuiTop() + 25, 160, 20, EasyText.empty()));
        this.nameInput.setMaxLength(32);
        this.nameInput.setValue(trader.getCustomName());

        this.buttonSetName = this.addWidget(new Button(this.screen.getGuiLeft() + 20, this.screen.getGuiTop() + 50, 74, 20, EasyText.translatable("gui.lightmanscurrency.changename"), this::SetName));
        this.buttonResetName = this.addWidget(new Button(this.screen.getGuiLeft() + this.screen.getXSize() - 93, this.screen.getGuiTop() + 50, 74, 20, EasyText.translatable("gui.lightmanscurrency.resetname"), this::ResetName));

        //Creative Toggle
        this.buttonToggleCreative = this.addWidget(IconAndButtonUtil.creativeToggleButton(this.screen.getGuiLeft() + 176, this.screen.getGuiTop() + 140 - 30, this::ToggleCreative, () -> this.menu.getTrader().isCreative()));
        this.buttonAddTrade = this.addWidget(new PlainButton(this.screen.getGuiLeft() + 166, this.screen.getGuiTop() + 140 - 30, 10, 10, this::AddTrade, IconAndButtonUtil.WIDGET_TEXTURE, 0, 200));
        this.buttonRemoveTrade = this.addWidget(new PlainButton(this.screen.getGuiLeft() + 166, this.screen.getGuiTop() + 140 - 20, 10, 10, this::RemoveTrade, IconAndButtonUtil.WIDGET_TEXTURE, 0, 220));

        this.buttonToggleBankLink = this.addWidget(new PlainButton(this.screen.getGuiLeft() + 20, this.screen.getGuiTop() + 72, 10, 10, this::ToggleBankLink, IconAndButtonUtil.WIDGET_TEXTURE, 10, trader.getLinkedToBank() ? 200 : 220));
        this.buttonToggleBankLink.visible = this.menu.hasPermission(Permissions.BANK_LINK);

        this.buttonSavePersistentTrader = this.addWidget(new IconButton(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 140 - 30, this::SavePersistentTraderData, IconAndButtonUtil.ICON_PERSISTENT_DATA, IconAndButtonUtil.TOOLTIP_PERSISTENT_TRADER));
        this.buttonSavePersistentTrader.visible = CommandLCAdmin.isAdminPlayer(this.menu.player);


        int idWidth = this.font.width(EasyText.translatable("gui.lightmanscurrency.settings.persistent.id"));
        this.persistentTraderIDInput = this.addWidget(new EditBox(this.font, this.screen.getGuiLeft() + 37 + idWidth, this.screen.getGuiTop() + 140 - 30, 108 - idWidth, 18, EasyText.empty()));

        int ownerWidth = this.font.width(EasyText.translatable("gui.lightmanscurrency.settings.persistent.owner"));
        this.persistentTraderOwnerInput = this.addWidget(new EditBox(this.font, this.screen.getGuiLeft() + 12 + ownerWidth, this.screen.getGuiTop() + 140 - 55, 178 - ownerWidth, 18, EasyText.empty()));

        this.tick();

    }

    @Override
    public void onClose() {

    }

    @Override
    public void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {

        TraderData trader = this.menu.getTrader();

        this.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.customname"), this.screen.getGuiLeft() + 20, this.screen.getGuiTop() + 15, 0x404040);

        if(this.menu.hasPermission(Permissions.BANK_LINK))
            this.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.settings.banklink"), this.screen.getGuiLeft() + 32, this.screen.getGuiTop() + 73, 0x404040);

        //Draw current trade count
        if(CommandLCAdmin.isAdminPlayer(this.menu.player) && trader != null)
        {
            String count = String.valueOf(trader.getTradeCount());
            int width = this.font.width(count);
            this.font.draw(pose, count, this.screen.getGuiLeft() + 164 - width, this.screen.getGuiTop() + 140 - 25, 0x404040);

            if(this.persistentTraderIDInput != null)
            {
                //Draw ID input label
                this.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.settings.persistent.id"), this.screen.getGuiLeft() + 35, this.screen.getGuiTop() + 140 - 25, 0xFFFFFF);
                //Draw Owner input label
                this.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.settings.persistent.owner"), this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 140 - 50, 0xFFFFFF);

            }

        }

    }

    @Override
    public void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY) {

        //Render button tooltips
        if(this.buttonAddTrade.isMouseOver(mouseX, mouseY))
        {
            this.screen.renderTooltip(pose, EasyText.translatable("tooltip.lightmanscurrency.trader.creative.addTrade"), mouseX, mouseY);
        }
        else if(this.buttonRemoveTrade.isMouseOver(mouseX, mouseY))
        {
            this.screen.renderTooltip(pose, EasyText.translatable("tooltip.lightmanscurrency.trader.creative.removeTrade"), mouseX, mouseY);
        }

    }

    @Override
    public void tick() {
        boolean canChangeName = this.menu.hasPermission(Permissions.CHANGE_NAME);
        this.nameInput.setEditable(canChangeName);
        this.nameInput.tick();

        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;

        this.buttonSetName.active = !this.nameInput.getValue().contentEquals(trader.getCustomName());
        this.buttonSetName.visible = canChangeName;
        this.buttonResetName.active = trader.hasCustomName();
        this.buttonResetName.visible = canChangeName;

        boolean isAdmin = CommandLCAdmin.isAdminPlayer(this.menu.player);
        this.buttonToggleCreative.visible = isAdmin;
        if(this.buttonToggleCreative.visible)
        {
            this.buttonAddTrade.visible = true;
            this.buttonAddTrade.active = trader.getTradeCount() < TraderData.GLOBAL_TRADE_LIMIT;
            this.buttonRemoveTrade.visible = true;
            this.buttonRemoveTrade.active = trader.getTradeCount() > 1;
        }
        else
        {
            this.buttonAddTrade.visible = false;
            this.buttonRemoveTrade.visible = false;
        }

        boolean canLinkAccount = this.menu.hasPermission(Permissions.BANK_LINK);
        this.buttonToggleBankLink.visible = canLinkAccount;
        if(canLinkAccount)
        {
            this.buttonToggleBankLink.setResource(IconAndButtonUtil.WIDGET_TEXTURE, 10, trader.getLinkedToBank() ? 200 : 220);
            this.buttonToggleBankLink.active = trader.canLinkBankAccount() || trader.getLinkedToBank();
        }


        if(this.buttonSavePersistentTrader != null)
        {
            this.buttonSavePersistentTrader.visible = isAdmin;
            this.buttonSavePersistentTrader.active = trader.hasValidTrade();
        }
        if(this.persistentTraderIDInput != null)
        {
            this.persistentTraderIDInput.visible = isAdmin;
            this.persistentTraderIDInput.tick();
        }
        if(this.persistentTraderOwnerInput != null)
        {
            this.persistentTraderOwnerInput.visible = isAdmin;
            this.persistentTraderOwnerInput.tick();
        }
    }

    private void SetName(Button button)
    {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        String customName = trader.getCustomName();
        if(!customName.contentEquals(this.nameInput.getValue()))
        {
            CompoundTag message = new CompoundTag();
            message.putString("ChangeName", this.nameInput.getValue());
            this.sendNetworkMessage(message);
            //LightmansCurrency.LogInfo("Sent 'Change Name' message with value:" + this.nameInput.getValue());
        }
    }

    private void ResetName(Button button)
    {
        this.nameInput.setValue("");
        this.SetName(button);
    }

    private void ToggleCreative(Button button)
    {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        CompoundTag message = new CompoundTag();
        message.putBoolean("MakeCreative", !trader.isCreative());
        this.sendNetworkMessage(message);
    }

    private void ToggleBankLink(Button button)
    {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        CompoundTag message = new CompoundTag();
        message.putBoolean("LinkToBankAccount", !trader.getLinkedToBank());
        this.sendNetworkMessage(message);
    }

    private void AddTrade(Button button)
    {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade(trader.getID(), true));
    }

    private void RemoveTrade(Button button)
    {
        TraderData trader = this.menu.getTrader();
        if(trader == null)
            return;
        LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddOrRemoveTrade(trader.getID(), false));
    }

    private void SavePersistentTraderData(Button button)
    {
        TraderData trader = this.menu.getTrader();
        if(trader != null && trader.canMakePersistent())
            LightmansCurrencyPacketHandler.instance.sendToServer(new MessageAddPersistentTrader(trader.getID(), this.persistentTraderIDInput.getValue(), this.persistentTraderOwnerInput.getValue()));
    }

}