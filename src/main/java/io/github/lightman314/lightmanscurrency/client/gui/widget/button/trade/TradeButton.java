package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class TradeButton extends Button{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/trade.png");
	
	public final int ARROW_WIDTH = 22;
	public final int ARROW_HEIGHT = 18;
	
	public final int TEMPLATE_WIDTH = 212;
	public final int TEMPLATE_HEIGHT = 100;
	
	private final Supplier<ITradeData> tradeSource;
	public final boolean displayMode;
	public boolean horizontalFlip = false;
	
	public TradeButton(int x, int y, int width, int height, boolean displayMode, Supplier<ITradeData> tradeSource, OnPress onPress) {
		super(x, y, width, height, new TextComponent(""), onPress);
		this.tradeSource = tradeSource;
		this.displayMode = displayMode;
	}
	
	public void move(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks)
	{
		if(!this.visible)
			return;
		
		ITradeData trade = tradeSource.get();
		if(trade == null)
			return;
		
		this.renderBackground(pose);
		
		
		
		
		
	}
	
	private void renderBackground(PoseStack pose)
	{
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		if(this.active)
			RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		else
			RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 1f);
		
		//int vOffset = this.isHovered ? TEMPLATE_HEIGHT : 0;
		
		//int centerX = this.x + (this.width / 2);
		//int centerY = this.y + (this.height / 2);
		
		
		
		
	}
	
	public void renderTooltips(PoseStack pose, int mouseX, int mouseY)
	{
		if(!this.visible)
			return;
		
		ITradeData trade = tradeSource.get();
		if(trade == null)
			return;
		
		//Draw the background
		
		
	}
	
	public DisplayEntry getInputDisplay(int index) {
		ITradeData trade = tradeSource.get();
		if(trade == null)
			return DisplayEntry.EMPTY;
		List<DisplayEntry> inputDisplays = trade.getInputDisplays();
		if(index < 0 || index >= inputDisplays.size())
			return DisplayEntry.EMPTY;
		return inputDisplays.get(index);
	}
	
	private int inputDisplayCount() {
		ITradeData trade = tradeSource.get();
		if(trade == null)
			return 0;
		return trade.getInputDisplays().size();
	}
	
	public Pair<Integer,Integer> getInputDisplayPosition(int index) {
		ITradeData trade = tradeSource.get();
		if(trade == null)
			return Pair.of(0, 0);
		
		int xPos = 0;
		int yPos = 0;
		int count = this.inputDisplayCount();
		if(index < 0 || index >= count)
			return Pair.of(0, 0);
		
		return Pair.of(this.x + xPos, this.y + yPos);
		
	}
	
	public boolean isMouseOverInputDisplay(int mouseX, int mouseY, int index) {
		if(!this.visible)
			return false;
		ITradeData trade = tradeSource.get();
		if(trade == null)
			return false;
		List<DisplayEntry> inputDisplays = trade.getInputDisplays();
		for(int i = 0; i < inputDisplays.size(); ++i)
		{
			
		}
		return false;
	}
	
	public static interface ITradeData
	{
		/**
		 * Whether the trade should render an arrow pointing from the inputs to the outputs.
		 */
		default boolean hasArrow() { return true; }
		/**
		 * The input display entries. For a sale this would be the trades price.
		 */
		public List<DisplayEntry> getInputDisplays();
		/**
		 * The output display entries. For a sale this would be the product being sold.
		 */
		public List<DisplayEntry> getOutputDisplays();
		/**
		 * Render trade-specific icons for the trade, such as the fluid traders drainable/fillable icons.
		 * @param x The x position of the button.
		 * @param y The y position of the button.
		 * @param displayMode Whether the button is in display mode (storage screen).
		 */
		default void renderAdditional(GuiComponent gui, PoseStack pose, int x, int y, boolean displayMode) { }
		/**
		 * Render trade-specific tooltips for the trade, such as the fluid traders drainable/fillable icons.
		 * @param mouseX
		 * @param mouseY
		 * @return The list of tooltip text. Return null to display no tooltip.
		 */
		default List<Component> getAdditionalTooltips(int mouseX, int mouseY) { return null; }
	}
	
	public static abstract class DisplayEntry
	{
		
		private static final DisplayEntry EMPTY = of(ItemStack.EMPTY, 0, () -> null);
		
		private final Supplier<List<Component>> tooltip;
		
		@Deprecated
		protected DisplayEntry() { this.tooltip = () -> null; }
		
		protected DisplayEntry (Supplier<List<Component>> tooltip) { this.tooltip = tooltip; }
		
		protected final Font getFont() { 
			Minecraft m = Minecraft.getInstance();
			return m.font;
		}
		
		protected List<Component> getTooltip(boolean displayMode) {
			return this.tooltip.get();
		}
		
		protected abstract void render(GuiComponent gui, PoseStack pose, int x, int y);
		
		public static DisplayEntry of(ItemStack item, int count, Supplier<List<Component>> tooltip) { return new ItemEntry(item, count, tooltip); }
		
		private static class ItemEntry extends DisplayEntry
		{
			private final ItemStack item;
			private final int count;

			private ItemEntry(ItemStack item, int count, Supplier<List<Component>> tooltip) { super(tooltip); this.item = item.copy(); this.item.setCount(1); this.count = count; }
			
			@Override
			protected void render(GuiComponent gui, PoseStack pose, int x, int y) {
				if(this.item.isEmpty())
					return;
				Font font = this.getFont();
				ItemRenderUtil.drawItemStack(gui, font, this.item, x, y);
				if(this.count > 1)
				{
					String text = String.valueOf(this.count);
					int width = font.width(text);
					font.draw(pose, text, x + 16 - width, y + 16 - font.lineHeight, 0xFFFFFF);
				}
			}
		}
		
	}
	
}
