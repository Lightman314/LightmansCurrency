package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.UniversalTraderButton;
import io.github.lightman314.lightmanscurrency.common.universal_traders.data.UniversalTraderData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenTrades2;
import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageRequestTraders;
//import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
//import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageOpenTrades2;
//import io.github.lightman314.lightmanscurrency.network.message.universal_trader.MessageRequestTraders;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public class TerminalScreen extends Screen{
	
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/trader_selection.png");
	
	private int xSize = 176;
	private int ySize = 187;
	
	Player player;
	
	private EditBox searchField;
	static int page = 0;
	
	Button buttonNextPage;
	Button buttonPreviousPage;
	List<UniversalTraderButton> traderButtons;
	
	private List<UniversalTraderData> traderList = new ArrayList<>();
	private List<UniversalTraderData> filteredTraderList = new ArrayList<>();
	
	public TerminalScreen(Player player)
	{
		super(new TranslatableComponent("block.lightmanscurrency.terminal"));
		this.player = player;
	}
	
	@Override
	protected void init()
	{
		
		super.init();
		
		int guiLeft = (this.width - this.xSize) / 2;
		int guiTop = (this.height - this.ySize) / 2;
		
		this.searchField = this.addRenderableWidget(new EditBox(this.font, guiLeft + 28, guiTop + 6, 101, 9, new TranslatableComponent("gui.lightmanscurrency.terminal.search")));
		this.searchField.setBordered(false);
		this.searchField.setMaxLength(32);
		this.searchField.setTextColor(0xFFFFFF);
		
		this.buttonPreviousPage = this.addRenderableWidget(new IconButton(guiLeft - 6, guiTop + 18, this::PreviousPage, GUI_TEXTURE, this.xSize, 0));
		this.buttonNextPage = this.addRenderableWidget(new IconButton(guiLeft + this.xSize - 14, guiTop + 18, this::NextPage, GUI_TEXTURE, this.xSize + 16, 0));
		
		this.initTraderButtons(guiLeft, guiTop);
		
		page = MathUtil.clamp(page, 0, this.pageLimit());
		
		this.tick();
		
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageRequestTraders());
		
	}
	
	private void initTraderButtons(int guiLeft, int guiTop)
	{
		this.traderButtons = new ArrayList<>();
		for(int y = 0; y < 5; y++)
		{
			UniversalTraderButton newButton = this.addRenderableWidget(new UniversalTraderButton(guiLeft + 15, guiTop + 18 + (y * UniversalTraderButton.HEIGHT), this::OpenTrader, this.font));
			this.traderButtons.add(newButton);
		}
	}
	
	@Override
	public void tick()
	{
		super.tick();
		this.searchField.tick();
		if(this.buttonPreviousPage != null)
		this.buttonPreviousPage.visible = this.pageLimit() > 0;
		this.buttonPreviousPage.active = page > 0;
		this.buttonNextPage.visible = this.pageLimit() > 0;
		this.buttonNextPage.active = page < this.pageLimit();
	}
	
	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		int startX = (this.width - this.xSize) / 2;
		int startY = (this.height - this.ySize) / 2;
		//Render the background
		this.blit(matrixStack, startX, startY, 0, 0, this.xSize, this.ySize);
		
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		this.searchField.render(matrixStack, mouseX, mouseY, partialTicks);
		
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
	
	private void PreviousPage(Button button)
	{
		if(page > 0)
		{
			page--;
			this.updateTraderButtons();
		}
	}
	
	private void NextPage(Button button)
	{
		if(page < this.pageLimit())
		{
			page++;
			this.updateTraderButtons();
		}
	}
	
	private void OpenTrader(Button button)
	{
		int index = getTraderIndex(button);
		if(index >= 0 && index < this.filteredTraderList.size())
		{
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageOpenTrades2(this.filteredTraderList.get(index).getTraderID()));
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
	
	public void updateTraders(List<UniversalTraderData> traders)
	{
		this.traderList = traders;
		updateTraderList();
	}
	
	private void updateTraderList()
	{
		if(this.searchField.getValue().isEmpty())
		{
			this.filteredTraderList = this.traderList;
			updateTraderButtons();
		}
		else
		{
			Stream<UniversalTraderData> stream = this.traderList.stream().filter(entry ->{
				String searchText = this.searchField.getValue().toLowerCase(Locale.ENGLISH).trim();
				//Search the display name of the traders
				if(entry.getName().getString().toLowerCase().contains(searchText))
					return true;
				//Search the owner name of the traders
				return entry.getOwnerName().toLowerCase().contains(searchText);
			});
			this.filteredTraderList = stream.collect(Collectors.toList());
			//Limit the page
			if(page > pageLimit())
				page = pageLimit();
			updateTraderButtons();
		}
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
