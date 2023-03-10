package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.List;

import com.google.common.collect.Lists;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton.Size;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountReference;
import io.github.lightman314.lightmanscurrency.common.bank.BankAccount.AccountType;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.menus.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageBankTransferPlayer;
import io.github.lightman314.lightmanscurrency.network.message.bank.MessageBankTransferTeam;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.Items;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class TransferTab extends ATMTab {

	public TransferTab(ATMScreen screen) { super(screen); }
	
	//Response should be 100 ticks or 5 seconds
	public static final int RESPONSE_DURATION = 100;
	
	private int responseTimer = 0;
	
	CoinValueInput amountWidget;
	
	TextFieldWidget playerInput;
	TeamSelectWidget teamSelection;
	
	IconButton buttonToggleMode;
	Button buttonTransfer;
	
	long selectedTeam = -1;
	
	boolean playerMode = true;
	
	@Nonnull
    @Override
	public IconData getIcon() { return IconAndButtonUtil.ICON_STORE_COINS; }

	@Override
	public ITextComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.atm.transfer"); }

	@Override
	public void init() {
		
		SimpleSlot.SetInactive(this.screen.getMenu());
		
		this.responseTimer = 0;
		this.screen.getMenu().clearMessage();
		
		this.amountWidget = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiLeft(), this.screen.getGuiTop(), EasyText.translatable("gui.lightmanscurrency.bank.transfertip"), CoinValue.EMPTY, this.screen.getFont(), value -> {}, this.screen::addRenderableTabWidget));
		this.amountWidget.init();
		this.amountWidget.allowFreeToggle = false;
		this.amountWidget.drawBG = false;
		
		this.buttonToggleMode = this.screen.addRenderableTabWidget(new IconButton(this.screen.getGuiLeft() + this.screen.getXSize() - 30, this.screen.getGuiTop() + 64, this::ToggleMode, this.playerMode ? IconData.of(Items.PLAYER_HEAD) : IconData.of(ItemRenderUtil.getAlexHead()), new IconAndButtonUtil.ToggleTooltip(() -> this.playerMode, EasyText.translatable("tooltip.lightmanscurrency.atm.transfer.mode.team"), EasyText.translatable("tooltip.lightmanscurrency.atm.transfer.mode.player"))));
		
		this.playerInput = this.screen.addRenderableTabWidget(new TextFieldWidget(this.screen.getFont(), this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 104, this.screen.getXSize() - 20, 20, EasyText.empty()));
		this.playerInput.visible = this.playerMode;
		
		this.teamSelection = this.screen.addRenderableTabWidget(new TeamSelectWidget(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 84, 2, Size.NORMAL, this::getTeamList, this::selectedTeam, this::SelectTeam));
		this.teamSelection.init(this.screen::addRenderableTabWidget, this.screen.getFont());
		this.teamSelection.visible = !this.playerMode;
		
		this.buttonTransfer = this.screen.addRenderableTabWidget(new Button(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 126, this.screen.getXSize() - 20, 20, EasyText.translatable(this.playerMode ? "gui.button.bank.transfer.player" : "gui.button.bank.transfer.team"), this::PressTransfer));
		this.buttonTransfer.active = false;
		
	}
	
	private List<Team> getTeamList()
	{
		List<Team> results = Lists.newArrayList();
		AccountReference source = this.screen.getMenu().getBankAccountReference();
		Team blockTeam = null;
		if(source != null && source.accountType == AccountType.Team)
			blockTeam = TeamSaveData.GetTeam(true, source.teamID);
		for(Team team : TeamSaveData.GetAllTeams(true))
		{
			if(team.hasBankAccount() && team != blockTeam)
				results.add(team);
		}
		return results;
	}
	
	public Team selectedTeam()
	{
		if(this.selectedTeam >= 0)
			return TeamSaveData.GetTeam(true, this.selectedTeam);
		return null;
	}
	
	public void SelectTeam(int teamIndex)
	{
		try {
			Team team = this.getTeamList().get(teamIndex);
			if(team.getID() == this.selectedTeam)
				return;
			this.selectedTeam = team.getID();
		} catch(Exception ignored) { }
	}
	
	private void PressTransfer(Button button)
	{
		if(this.playerMode)
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageBankTransferPlayer(this.playerInput.getValue(), this.amountWidget.getCoinValue()));
			this.playerInput.setValue("");
			this.amountWidget.setCoinValue(CoinValue.EMPTY);
		}
		else if(this.selectedTeam >= 0)
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageBankTransferTeam(this.selectedTeam, this.amountWidget.getCoinValue()));
			this.amountWidget.setCoinValue(CoinValue.EMPTY);
		}
	}

	private void ToggleMode(Button button) {
		this.playerMode = !this.playerMode;
		this.buttonTransfer.setMessage(EasyText.translatable(this.playerMode ? "gui.button.bank.transfer.player" : "gui.button.bank.transfer.team"));
		this.teamSelection.visible = !this.playerMode;
		this.playerInput.visible = this.playerMode;
		this.buttonToggleMode.setIcon(this.playerMode ? IconData.of(Items.PLAYER_HEAD) : IconData.of(ItemRenderUtil.getAlexHead()));
	}
	
	@Override
	public void preRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.hideCoinSlots(pose);
		
		//this.screen.getFont().draw(pose, this.getTooltip(), this.screen.getGuiLeft() + 8f, this.screen.getGuiTop() + 6f, 0x404040);
		ITextComponent balance = this.screen.getMenu().getBankAccount() == null ? EasyText.translatable("gui.lightmanscurrency.bank.null") : EasyText.translatable("gui.lightmanscurrency.bank.balance", this.screen.getMenu().getBankAccount().getCoinStorage().getString("0"));
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
	public void postRender(MatrixStack pose, int mouseX, int mouseY) {
		IconAndButtonUtil.renderButtonTooltips(pose, mouseX, mouseY, Lists.newArrayList(this.buttonToggleMode));
	}
	
	@Override
	public void tick() {
		
		this.amountWidget.tick();
		
		if(this.playerMode)
		{
			this.playerInput.tick();
			this.buttonTransfer.active = !this.playerInput.getValue().isEmpty() && this.amountWidget.getCoinValue().isValid();
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
	
	private IFormattableTextComponent getMessage() { return this.screen.getMenu().getTransferMessage(); }

	@Override
	public void onClose() {
		SimpleSlot.SetActive(this.screen.getMenu());
		this.responseTimer = 0;
		this.screen.getMenu().clearMessage();
	}

	@Override
	public boolean blockInventoryClosing() { return this.playerMode; }
	
}