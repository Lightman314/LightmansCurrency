package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.itemInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.ClientTradingOffice;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ItemInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.UniversalTraderButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.universal_traders.TradingOffice;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.interfacebe.MessageSetTrader;
import io.github.lightman314.lightmanscurrency.trader.IItemTrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class TraderSelectTab extends ItemInterfaceTab {

	public TraderSelectTab(ItemInterfaceScreen screen) { super(screen, true); }
	
	EditBox searchField;
	
	Button buttonPreviousPage;
	Button buttonNextPage;
	
	List<UniversalTraderButton> traderButtons;
	
	private int page = 0;
	
	private List<UniversalTraderData> filteredTraderList = new ArrayList<>();
	
	private List<UniversalTraderData> traderList() {
		List<UniversalTraderData> traderList = this.filterItemTraders(ClientTradingOffice.getTraderList());
		traderList.sort(TradingTerminalScreen.TRADER_SORTER);
		return traderList;
	}
	
	private List<UniversalTraderData> filterItemTraders(List<UniversalTraderData> allTraders) {
		List<UniversalTraderData> itemTraders = new ArrayList<>();
		for(UniversalTraderData trader : allTraders)
		{
			if(trader instanceof IItemTrader)
			{
				InteractionType interaction = this.screen.getMenu().blockEntity.getInteractionType();
				if(!interaction.requiresPermissions || (trader.hasPermission(this.screen.getMenu().blockEntity.getOwner(), Permissions.INTERACTION_LINK)))
					itemTraders.add(trader);
			}
		}
		return itemTraders;
	}
	
	@Override
	public boolean blockInventoryClosing() { return true; }
	
	@Override
	public IconData getIcon() { return IconData.of(ModBlocks.TERMINAL); }

	@Override
	public Component getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.interface.trader"); }

	@Override
	public boolean valid(InteractionType interaction) { return true; }
	
	@Override
	public void init() {
		
		this.searchField = this.screen.addRenderableTabWidget(new EditBox(this.screen.getFont(), this.screen.getGuiLeft() + 28, this.screen.getGuiTop() + 6, 101, 9, new TranslatableComponent("gui.lightmanscurrency.terminal.search")));
		this.searchField.setBordered(false);;
		this.searchField.setMaxLength(32);
		this.searchField.setTextColor(0xFFFFFF);
		
		this.buttonPreviousPage = this.screen.addRenderableTabWidget(IconAndButtonUtil.leftButton(this.screen.getGuiLeft(), this.screen.getGuiTop() - 20, this::PreviousPage));
		this.buttonNextPage = this.screen.addRenderableTabWidget(IconAndButtonUtil.rightButton(this.screen.getGuiLeft() + this.screen.getXSize() - 20, this.screen.getGuiTop() - 20, this::NextPage));
		
		this.initTraderButtons(this.screen.getGuiLeft(), this.screen.getGuiTop());
		
		this.page = MathUtil.clamp(page, 0, this.pageLimit());
		
		this.tick();
		
		this.updateTraderList();
		
	}
	
	private void initTraderButtons(int guiLeft, int guiTop)
	{
		this.traderButtons = new ArrayList<>();
		for(int y = 0; y < 3; y++)
		{
			UniversalTraderButton newButton = this.screen.addRenderableTabWidget(new UniversalTraderButton(guiLeft + 15, guiTop + 18 + (y * UniversalTraderButton.HEIGHT), this::SelectTrader, this.screen.getFont()));
			this.traderButtons.add(newButton);
		}
	}

	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
	}

	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY) {
		
		
		
	}

	@Override
	public void tick() {
		
		this.searchField.tick();
		this.buttonPreviousPage.visible = this.pageLimit() > 0;
		this.buttonPreviousPage.active = page > 0;
		this.buttonNextPage.visible = this.pageLimit() > 0;
		this.buttonNextPage.active = page < this.pageLimit();
		
		for(int i = 0; i < this.traderButtons.size(); ++i)
		{
			UniversalTraderButton button = this.traderButtons.get(i);
			if(button.getData() != null && button.getData() == this.screen.getMenu().blockEntity.getTrader())
				button.selected = true;
			else
				button.selected = false;
		}
		
	}
	
	@Override
	public boolean charTyped(char c, int code)
	{
		String s = this.searchField.getValue();
		if(this.searchField.charTyped(c, code))
		{
			if(!Objects.equals(s, this.searchField.getValue()))
			{
				this.updateTraderList();
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean keyPressed(int key, int scanCode, int mods)
	{
		String s = this.searchField.getValue();
		if(this.searchField.keyPressed(key, scanCode, mods))
		{
			if(!Objects.equals(s,  this.searchField.getValue()))
			{
				this.updateTraderList();
			}
			return true;
		}
		return false;
	}

	@Override
	public void onClose() {
		
	}
	
	private void PreviousPage(Button button) {
		if(this.page > 0)
		{
			this.page--;
			this.updateTraderButtons();
		}
	}
	
	private void NextPage(Button button) {
		if(this.page < this.pageLimit())
		{
			this.page++;
			this.updateTraderButtons();
		}
	}
	
	private void SelectTrader(Button button) {
		int index = getTraderIndex(button);
		if(index >= 0 && index < this.filteredTraderList.size())
		{
			UUID traderID = this.filteredTraderList.get(index).getTraderID();
			this.screen.getMenu().blockEntity.setTrader(traderID);
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetTrader(this.screen.getMenu().blockEntity.getBlockPos(), traderID));
		}
	}
	
	private int getTraderIndex(Button button)
	{
		if(!traderButtons.contains(button))
			return -1;
		int index = traderButtons.indexOf(button);
		index += page * this.traderButtons();
		return index;
	}
	
	private int pageLimit()
	{
		return (this.filteredTraderList.size() - 1) / this.traderButtons();
	}
	
	private int traderButtons()
	{
		return this.traderButtons.size();
	}
	
	private void updateTraderList()
	{
		//Filtering of results moved to the TradingOffice.filterTraders
		this.filteredTraderList = TradingOffice.filterTraders(this.searchField.getValue(), this.traderList());
		this.updateTraderButtons();
		//Limit the page
		if(page > pageLimit())
			page = pageLimit();
	}
	
	private void updateTraderButtons()
	{
		int startIndex = page * this.traderButtons();
		for(int i = 0; i < this.traderButtons.size(); i++)
		{
			if(startIndex + i < this.filteredTraderList.size())
				this.traderButtons.get(i).SetData(this.filteredTraderList.get(startIndex + i));
			else
				this.traderButtons.get(i).SetData(null);
		}
	}

}
