package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

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
import io.github.lightman314.lightmanscurrency.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageBankTransferPlayer;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageBankTransferTeam;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;

public class TransferTab extends ATMTab implements ICoinValueInput {

	public TransferTab(ATMScreen screen) { super(screen); }

	CoinValueInput amountWidget;
	
	EditBox playerInput;
	TeamSelectWidget teamSelection;
	
	IconButton buttonToggleMode;
	Button buttonTransfer;
	
	UUID selectedTeam = null;
	
	boolean playerMode = true;
	
	@Override
	public IconData getIcon() { return IconAndButtonUtil.ICON_STORE_COINS; }

	@Override
	public Component getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.atm.transfer"); }

	@Override
	public void init() {
		
		SimpleSlot.SetInactive(this.screen.getMenu());
		
		this.screen.getMenu().setMessage(new TextComponent(""));
		
		this.amountWidget = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiTop() - CoinValueInput.HEIGHT, new TranslatableComponent("gui.lightmanscurrency.bank.transfertip"), CoinValue.EMPTY, this));
		this.amountWidget.init();
		this.amountWidget.allowFreeToggle = false;
		
		this.buttonToggleMode = this.screen.addRenderableTabWidget(new IconButton(this.screen.getGuiLeft() + this.screen.getXSize() - 30, this.screen.getGuiTop() + 10, this::ToggleMode, this.playerMode ? IconData.of(Items.PLAYER_HEAD) : IconData.of(ItemRenderUtil.getAlexHead()), new IconAndButtonUtil.ToggleTooltip(() -> this.playerMode, new TranslatableComponent("tooltip.lightmanscurrency.atm.transfer.mode.team"), new TranslatableComponent("tooltip.lightmanscurrency.atm.transfer.mode.player"))));
		
		this.playerInput = this.screen.addRenderableTabWidget(new EditBox(this.screen.getFont(), this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 50, this.screen.getXSize() - 20, 20, new TextComponent("")));
		this.playerInput.visible = this.playerMode;
		
		this.teamSelection = this.screen.addRenderableTabWidget(new TeamSelectWidget(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 30, 2, Size.NORMAL, this::getTeamList, this::selectedTeam, this::SelectTeam));
		this.teamSelection.init(this.screen::addRenderableTabWidget, this.screen.getFont());
		this.teamSelection.setVisible(!this.playerMode);
		
		this.buttonTransfer = this.screen.addRenderableTabWidget(new Button(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 72, this.screen.getXSize() - 20, 20, new TranslatableComponent(this.playerMode ? "gui.button.bank.transfer.player" : "gui.button.bank.transfer.team"), this::PressTransfer));
		this.buttonTransfer.active = false;
		
	}
	
	private List<Team> getTeamList()
	{
		List<Team> results = Lists.newArrayList();
		AccountReference source = this.screen.getMenu().getAccountSource();
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
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageBankTransferPlayer(this.playerInput.getValue(), this.amountWidget.getCoinValue()));
			this.playerInput.setValue("");
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
		this.buttonTransfer.setMessage(new TranslatableComponent(this.playerMode ? "gui.button.bank.transfer.player" : "gui.button.bank.transfer.team"));
		this.teamSelection.setVisible(!this.playerMode);
		this.playerInput.visible = this.playerMode;
		this.buttonToggleMode.setIcon(this.playerMode ? IconData.of(Items.PLAYER_HEAD) : IconData.of(ItemRenderUtil.getAlexHead()));
	}
	
	@Override
	public void backgroundRender(PoseStack pose) {
		RenderSystem.setShaderTexture(0, ATMScreen.GUI_TEXTURE);
		this.screen.blit(pose, this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 97, 7, 79, 162, 18);
		List<FormattedText> lines = this.screen.getFont().getSplitter().splitLines(this.screen.getMenu().getLastMessage().getString(), this.screen.getXSize() - 15, Style.EMPTY);
		for(int i = 0; i < lines.size(); ++i)
			this.screen.getFont().draw(pose, lines.get(i).getString(), this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 97 + (this.screen.getFont().lineHeight * i), 0x404040);
	}
	
	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		this.screen.getFont().draw(pose, this.getTooltip(), this.screen.getGuiLeft() + 8f, this.screen.getGuiTop() + 6f, 0x404040);
		Component balance = this.screen.getMenu().getAccount() == null ? new TranslatableComponent("gui.lightmanscurrency.bank.null") : new TranslatableComponent("gui.lightmanscurrency.bank.balance", this.screen.getMenu().getAccount().getCoinStorage().getString("0"));
		this.screen.getFont().draw(pose, balance, this.screen.getGuiLeft() + 8f, this.screen.getGuiTop() + 18, 0x404040);
	}

	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY) {
		IconAndButtonUtil.renderButtonTooltips(pose, mouseX, mouseY, Lists.newArrayList(this.buttonToggleMode));
	}
	
	@Override
	public void tick() {
		
		this.amountWidget.tick();
		
		if(this.playerMode)
		{
			this.playerInput.tick();
			this.buttonTransfer.active = !this.playerInput.getValue().isBlank() && this.amountWidget.getCoinValue().isValid();
		}
		else
		{
			Team team = this.selectedTeam();
			this.buttonTransfer.active = team != null && team.hasBankAccount() && this.amountWidget.getCoinValue().isValid();
		}
	}

	@Override
	public void onClose() {
		SimpleSlot.SetActive(this.screen.getMenu());
	}

	@Override
	public <T extends GuiEventListener & Widget & NarratableEntry> T addCustomWidget(T button) {
		if(button instanceof AbstractWidget)
			this.screen.addRenderableTabWidget((AbstractWidget)button);
		return button;
	}

	@Override
	public int getWidth() {
		return this.screen.width;
	}

	@Override
	public Font getFont() {
		return this.screen.getFont();
	}

	@Override
	public void OnCoinValueChanged(CoinValueInput input) { }

	@Override
	public boolean blockInventoryClosing() { return this.playerMode; }
	
}
