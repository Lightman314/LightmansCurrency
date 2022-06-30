package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton.Size;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.universal_traders.bank.BankAccount.AccountType;
import io.github.lightman314.lightmanscurrency.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageBankTransferPlayer;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageBankTransferTeam;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

public class TransferTab extends ATMTab {

	public TransferTab(ATMScreen screen) { super(screen); }
	
	//Response should be 100 ticks or 5 seconds
	public static final int RESPONSE_DURATION = 100;
	
	private int responseTimer = 0;
	
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
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.atm.transfer"); }

	@Override
	public void init() {
		
		SimpleSlot.SetInactive(this.screen.getMenu());
		
		this.responseTimer = 0;
		this.screen.getMenu().clearMessage();
		
		this.amountWidget = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiLeft(), this.screen.getGuiTop(), Component.translatable("gui.lightmanscurrency.bank.transfertip"), CoinValue.EMPTY, this.screen.getFont(), value -> {}, this.screen::addRenderableTabWidget));
		this.amountWidget.init();
		this.amountWidget.allowFreeToggle = false;
		this.amountWidget.drawBG = false;
		
		this.buttonToggleMode = this.screen.addRenderableTabWidget(new IconButton(this.screen.getGuiLeft() + this.screen.getXSize() - 30, this.screen.getGuiTop() + 64, this::ToggleMode, this.playerMode ? IconData.of(Items.PLAYER_HEAD) : IconData.of(ItemRenderUtil.getAlexHead()), new IconAndButtonUtil.ToggleTooltip(() -> this.playerMode, Component.translatable("tooltip.lightmanscurrency.atm.transfer.mode.team"), Component.translatable("tooltip.lightmanscurrency.atm.transfer.mode.player"))));
		
		this.playerInput = this.screen.addRenderableTabWidget(new EditBox(this.screen.getFont(), this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 104, this.screen.getXSize() - 20, 20, Component.empty()));
		this.playerInput.visible = this.playerMode;
		
		this.teamSelection = this.screen.addRenderableTabWidget(new TeamSelectWidget(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 84, 2, Size.NORMAL, this::getTeamList, this::selectedTeam, this::SelectTeam));
		this.teamSelection.init(this.screen::addRenderableTabWidget, this.screen.getFont());
		this.teamSelection.visible = !this.playerMode;
		
		this.buttonTransfer = this.screen.addRenderableTabWidget(new Button(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 126, this.screen.getXSize() - 20, 20, Component.translatable(this.playerMode ? "gui.button.bank.transfer.player" : "gui.button.bank.transfer.team"), this::PressTransfer));
		this.buttonTransfer.active = false;
		
	}
	
	private List<Team> getTeamList()
	{
		List<Team> results = Lists.newArrayList();
		AccountReference source = this.screen.getMenu().getBankAccountReference();
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
		this.buttonTransfer.setMessage(Component.translatable(this.playerMode ? "gui.button.bank.transfer.player" : "gui.button.bank.transfer.team"));
		this.teamSelection.visible = !this.playerMode;
		this.playerInput.visible = this.playerMode;
		this.buttonToggleMode.setIcon(this.playerMode ? IconData.of(Items.PLAYER_HEAD) : IconData.of(ItemRenderUtil.getAlexHead()));
	}
	
	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.hideCoinSlots(pose);
		
		//this.screen.getFont().draw(pose, this.getTooltip(), this.screen.getGuiLeft() + 8f, this.screen.getGuiTop() + 6f, 0x404040);
		Component balance = this.screen.getMenu().getBankAccount() == null ? Component.translatable("gui.lightmanscurrency.bank.null") : Component.translatable("gui.lightmanscurrency.bank.balance", this.screen.getMenu().getBankAccount().getCoinStorage().getString("0"));
		this.screen.getFont().draw(pose, balance, this.screen.getGuiLeft() + 8f, this.screen.getGuiTop() + 72, 0x404040);
		
		if(this.hasMessage())
		{
			//Draw a message background
			TextRenderUtil.drawCenteredMultilineText(pose, this.getMessage(), this.screen.getGuiLeft() + 2, this.screen.getXSize() - 4, this.screen.getGuiTop() + 5, 0x404040);
			this.amountWidget.visible = false;
		}
		else
			this.amountWidget.visible = true;
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
		
		
		if(this.hasMessage())
		{
			this.responseTimer++;
			if(this.responseTimer >= RESPONSE_DURATION)
			{
				this.responseTimer = 0;
				this.screen.getMenu().clearMessage();
			}
		}
	}
	
	private boolean hasMessage() { return this.screen.getMenu().hasTransferMessage(); }
	
	private MutableComponent getMessage() { return this.screen.getMenu().getTransferMessage(); }

	@Override
	public void onClose() {
		SimpleSlot.SetActive(this.screen.getMenu());
		this.responseTimer = 0;
		this.screen.getMenu().clearMessage();
	}

	@Override
	public boolean blockInventoryClosing() { return this.playerMode; }
	
}
