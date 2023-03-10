package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.UniversalTraderButton;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.terminal.filters.TraderSearchFilter;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageOpenTrades;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public class TradingTerminalScreen extends Screen implements IScrollable{
	
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/trader_selection.png");
	public static final Comparator<TraderData> TERMINAL_SORTER = new TraderSorter(true, true, true);
	public static final Comparator<TraderData> NAME_ONLY_SORTER = new TraderSorter(false, false, false);
	
	private final int xSize = 176;
	private final int ySize = 187;
	
	private TextFieldWidget searchField;
	private static int scroll = 0;
	
	ScrollBarWidget scrollBar;
	
	List<UniversalTraderButton> traderButtons;
	
	private List<TraderData> traderList(){
		List<TraderData> traderList = TraderSaveData.GetAllTerminalTraders(true);
		//No longer need to remove the auction house, as the 'showInTerminal' function now confirms the auction houses enabled/visible status.
		//traderList.removeIf(d -> d instanceof AuctionHouseTrader && !Config.SERVER.enableAuctionHouse.get());
		traderList.sort(TERMINAL_SORTER);
		return traderList;
	}
	private List<TraderData> filteredTraderList = new ArrayList<>();
	
	public TradingTerminalScreen()
	{
		super(EasyText.translatable("block.lightmanscurrency.terminal"));
	}
	
	@Override
	protected void init()
	{
		
		super.init();
		
		int guiLeft = (this.width - this.xSize) / 2;
		int guiTop = (this.height - this.ySize) / 2;
		
		this.searchField = this.addButton(new TextFieldWidget(this.font, guiLeft + 28, guiTop + 6, 101, 9, EasyText.translatable("gui.lightmanscurrency.terminal.search")));
		this.searchField.setBordered(false);
		this.searchField.setMaxLength(32);
		this.searchField.setTextColor(0xFFFFFF);
		
		this.scrollBar = this.addButton(new ScrollBarWidget(guiLeft + 16 + UniversalTraderButton.WIDTH, guiTop + 17, UniversalTraderButton.HEIGHT * 5 + 2, this));
		
		this.initTraderButtons(guiLeft, guiTop);
		
		this.tick();
		
		this.updateTraderList();
		
		this.validateScroll();
		
	}
	
	@Override
	public boolean isPauseScreen() { return false; }
	
	private void initTraderButtons(int guiLeft, int guiTop)
	{
		this.traderButtons = new ArrayList<>();
		for(int y = 0; y < 5; y++)
		{
			UniversalTraderButton newButton = this.addButton(new UniversalTraderButton(guiLeft + 15, guiTop + 18 + (y * UniversalTraderButton.HEIGHT), this::OpenTrader, this.font));
			this.traderButtons.add(newButton);
		}
	}
	
	@Override
	public void tick()
	{
		super.tick();
		this.searchField.tick();
	}
	
	@Override
	public void render(@Nonnull MatrixStack pose, int mouseX, int mouseY, float partialTicks)
	{
		if(this.minecraft == null)
			this.minecraft = Minecraft.getInstance();
		
		this.renderBackground(pose);

		RenderUtil.bindTexture(GUI_TEXTURE);
		RenderUtil.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		int startX = (this.width - this.xSize) / 2;
		int startY = (this.height - this.ySize) / 2;
		//Render the background
		this.blit(pose, startX, startY, 0, 0, this.xSize, this.ySize);
		
		this.scrollBar.beforeWidgetRender(mouseY);
		
		super.render(pose, mouseX, mouseY, partialTicks);
		
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
		return this.searchField.isFocused() && this.searchField.isVisible() && key != GLFW_KEY_ESCAPE || super.keyPressed(key, scanCode, mods);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.scrollBar.onMouseClicked(mouseX, mouseY, button);
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		this.scrollBar.onMouseReleased(mouseX, mouseY, button);
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if(delta < 0)
		{			
			if(scroll < this.getMaxScroll())
				this.setScroll(scroll + 1);
		}
		else if(delta > 0)
		{
			if(scroll > 0)
				this.setScroll(scroll - 1);
		}
		return super.mouseScrolled(mouseX, mouseY, delta);
	}
	
	private void OpenTrader(Button button)
	{
		int index = getTraderIndex(button);
		if(index >= 0 && index < this.filteredTraderList.size())
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades(this.filteredTraderList.get(index).getID()));
		}
	}
	
	private int getTraderIndex(Button button)
	{
		if(!traderButtons.contains(button))
			return -1;
		int index = traderButtons.indexOf(button);
		index += scroll;
		return index;
	}
	
	private void updateTraderList()
	{
		//Filtering of results moved to the TradingOffice.filterTraders
		this.filteredTraderList = this.searchField.getValue().isEmpty() ? this.traderList() : TraderSearchFilter.FilterTraders(this.traderList(), this.searchField.getValue());
		//Validate the scroll
		this.validateScroll();
		//Update the trader buttons
		this.updateTraderButtons();
	}
	
	private void updateTraderButtons()
	{
		int startIndex = scroll;
		for(int i = 0; i < this.traderButtons.size(); i++)
		{
			if(startIndex + i < this.filteredTraderList.size())
				this.traderButtons.get(i).SetData(this.filteredTraderList.get(startIndex + i));
			else
				this.traderButtons.get(i).SetData(null);
		}
	}

	private static class TraderSorter implements Comparator<TraderData> {

		private final boolean creativeAtTop;
		private final boolean emptyAtBottom;
		private final boolean auctionHousePriority;

		public TraderSorter(boolean creativeAtTop, boolean emptyAtBottom, boolean auctionHousePriority) {
			this.creativeAtTop = creativeAtTop;
			this.emptyAtBottom = emptyAtBottom;
			this.auctionHousePriority = auctionHousePriority;
		}

		@Override
		public int compare(TraderData a, TraderData b) {
			try {
				if (this.auctionHousePriority) {
						boolean ahA = a instanceof AuctionHouseTrader;
						boolean ahB = b instanceof AuctionHouseTrader;
						if (ahA && !ahB)
							return -1;
						else if (ahB && !ahA)
							return 1;
					}

				if (this.emptyAtBottom) {
						boolean emptyA = !a.hasValidTrade();
						boolean emptyB = !b.hasValidTrade();
						if (emptyA != emptyB)
							return emptyA ? 1 : -1;
					}

					if (this.creativeAtTop) {
						//Prioritize creative traders at the top of the list
						if (a.isCreative() && !b.isCreative())
							return -1;
						else if (b.isCreative() && !a.isCreative())
							return 1;
						//If both or neither are creative, sort by name.
					}

					//Sort by trader name
					int sort = a.getName().getString().toLowerCase().compareTo(b.getName().getString().toLowerCase());
					//Sort by owner name if trader name is equal
					if (sort == 0)
						sort = a.getOwner().getOwnerName(true).compareToIgnoreCase(b.getOwner().getOwnerName(true));

					return sort;

			} catch (Throwable t) {
				return 0;
			}
		}
	}

	private void validateScroll() { scroll = Math.min(scroll, this.getMaxScroll()); }
	
	@Override
	public int currentScroll() { return scroll; }

	@Override
	public void setScroll(int newScroll) {
		scroll = newScroll;
		this.updateTraderButtons();
	}

	@Override
	public int getMaxScroll() {
		return Math.max(0, this.filteredTraderList.size() - this.traderButtons.size());
	}

}