package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput.ICoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton.Size;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountType;
import io.github.lightman314.lightmanscurrency.containers.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageBankTransferPlayer;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageBankTransferTeam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;

public class TransferTab extends ATMTab implements ICoinValueInput {

	public TransferTab(ATMScreen screen) { super(screen); }

	CoinValueInput amountWidget;
	
	TextFieldWidget playerInput;
	TeamSelectWidget teamSelection;
	
	IconButton buttonToggleMode;
	Button buttonTransfer;
	
	UUID selectedTeam = null;
	
	boolean playerMode = true;
	
	@Override
	public IconData getIcon() { return IconAndButtonUtil.ICON_STORE_COINS; }

	@Override
	public ITextComponent getTooltip() { return new TranslationTextComponent("tooltip.lightmanscurrency.atm.transfer"); }

	@Override
	public void init() {
		
		SimpleSlot.SetInactive(this.screen.getContainer());
		
		this.screen.getContainer().setMessage(new StringTextComponent(""));
		
		this.amountWidget = this.screen.addTabListener(new CoinValueInput(this.screen.getGuiTop() - CoinValueInput.HEIGHT, new TranslationTextComponent("gui.lightmanscurrency.bank.transfertip"), CoinValue.EMPTY, this));
		//this.amountWidget.init();
		this.amountWidget.allowFreeToggle = false;
		
		this.buttonToggleMode = this.screen.addRenderableTabWidget(new IconButton(this.screen.getGuiLeft() + this.screen.getXSize() - 30, this.screen.getGuiTop() + 10, this::ToggleMode, this.playerMode ? IconData.of(Items.PLAYER_HEAD) : IconData.of(ItemRenderUtil.getAlexHead()), new IconAndButtonUtil.ToggleTooltip(() -> this.playerMode, new TranslationTextComponent("tooltip.lightmanscurrency.atm.transfer.mode.team"), new TranslationTextComponent("tooltip.lightmanscurrency.atm.transfer.mode.player"))));
		
		this.playerInput = this.screen.addRenderableTabWidget(new TextFieldWidget(this.screen.getFont(), this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 50, this.screen.getXSize() - 20, 20, new StringTextComponent("")));
		this.playerInput.visible = this.playerMode;
		
		this.teamSelection = this.screen.addRenderableTabWidget(new TeamSelectWidget(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 30, 2, Size.NORMAL, this::getTeamList, this::selectedTeam, this::SelectTeam));
		this.teamSelection.init(this.screen::addRenderableTabWidget, this.screen.getFont());
		this.teamSelection.setVisible(!this.playerMode);
		
		this.buttonTransfer = this.screen.addRenderableTabWidget(new Button(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 72, this.screen.getXSize() - 20, 20, new TranslationTextComponent(this.playerMode ? "gui.button.bank.transfer.player" : "gui.button.bank.transfer.team"), this::PressTransfer));
		this.buttonTransfer.active = false;
		
	}
	
	private List<Team> getTeamList()
	{
		List<Team> results = Lists.newArrayList();
		AccountReference source = this.screen.getContainer().getAccountSource();
		Team blockTeam = null;
		if(source != null && source.accountType == AccountType.Team)
			blockTeam = ClientTradingOffice.getTeam(source.id);
		for(Team team : ClientTradingOffice.getTeamList())
		{
			if(team.hasBankAccount() && team != blockTeam)
				results.add(team);
		}
		return results;
	}
	
	public Team selectedTeam()
	{
		if(this.selectedTeam != null)
			return ClientTradingOffice.getTeam(this.selectedTeam);
		return null;
	}
	
	public void SelectTeam(int teamIndex)
	{
		try {
			Team team = this.getTeamList().get(teamIndex);
			if(team.getID().equals(this.selectedTeam))
				return;
			this.selectedTeam = team.getID();
		} catch(Exception e) { }
	}
	
	private void PressTransfer(Button button)
	{
		if(this.playerMode)
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageBankTransferPlayer(this.playerInput.getText(), this.amountWidget.getCoinValue()));
			this.playerInput.setText("");
			this.amountWidget.setCoinValue(CoinValue.EMPTY);
		}
		else if(this.selectedTeam != null)
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageBankTransferTeam(this.selectedTeam, this.amountWidget.getCoinValue()));
			this.amountWidget.setCoinValue(CoinValue.EMPTY);
		}
	}

	private void ToggleMode(Button button) {
		this.playerMode = !this.playerMode;
		this.buttonTransfer.setMessage(new TranslationTextComponent(this.playerMode ? "gui.button.bank.transfer.player" : "gui.button.bank.transfer.team"));
		this.teamSelection.setVisible(!this.playerMode);
		this.playerInput.visible = this.playerMode;
		this.buttonToggleMode.setIcon(this.playerMode ? IconData.of(Items.PLAYER_HEAD) : IconData.of(ItemRenderUtil.getAlexHead()));
	}
	
	@Override
	public void preRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		this.amountWidget.render(pose, mouseX, mouseY, partialTicks);
		this.screen.getFont().drawString(pose, this.getTooltip().getString(), this.screen.getGuiLeft() + 8f, this.screen.getGuiTop() + 6f, 0x404040);
		ITextComponent balance = this.screen.getContainer().getAccount() == null ? new TranslationTextComponent("gui.lightmanscurrency.bank.null") : new TranslationTextComponent("gui.lightmanscurrency.bank.balance", this.screen.getContainer().getAccount().getCoinStorage().getString("0"));
		this.screen.getFont().drawString(pose, balance.getString(), this.screen.getGuiLeft() + 8f, this.screen.getGuiTop() + 18, 0x404040);
		
		Minecraft.getInstance().getTextureManager().bindTexture(ATMScreen.GUI_TEXTURE);
		this.screen.blit(pose, this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 97, 7, 79, 162, 18);
		List<ITextProperties> lines = this.screen.getFont().getCharacterManager().func_238362_b_(this.screen.getContainer().getLastMessage(), this.screen.getXSize() - 15, Style.EMPTY);
		for(int i = 0; i < lines.size(); ++i)
			this.screen.getFont().drawString(pose, lines.get(i).getString(), this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 97 + (this.screen.getFont().FONT_HEIGHT * i), 0x404040);
	}

	@Override
	public void postRender(MatrixStack pose, int mouseX, int mouseY) {
		IconAndButtonUtil.renderButtonTooltips(pose, mouseX, mouseY, Lists.newArrayList(this.buttonToggleMode));
	}
	
	@Override
	public void tick() {
		
		this.amountWidget.tick();
		
		if(this.playerMode)
		{
			this.playerInput.tick();
			this.buttonTransfer.active = !this.playerInput.getText().isEmpty() && this.amountWidget.getCoinValue().isValid();
		}
		else
		{
			Team team = this.selectedTeam();
			this.buttonTransfer.active = team != null && team.hasBankAccount() && this.amountWidget.getCoinValue().isValid();
		}
	}

	@Override
	public void onClose() {
		SimpleSlot.SetActive(this.screen.getContainer());
	}

	@Override
	public <T extends Button> T addCustomButton(T button) {
		return this.screen.addRenderableTabWidget(button);
	}
	
	@Override
	public <T extends IGuiEventListener> T addCustomListener(T listener) {
		return this.screen.addTabListener(listener);
	}

	@Override
	public int getWidth() {
		return this.screen.width;
	}

	@Override
	public FontRenderer getFont() {
		return this.screen.getFont();
	}

	@Override
	public void OnCoinValueChanged(CoinValueInput input) { }

	@Override
	public boolean blockInventoryClosing() { return this.playerMode; }
	
}
