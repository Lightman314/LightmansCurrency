package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.itemInterface;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ItemInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.ItemTradeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.interfacebe.MessageSetInteractionType;
import io.github.lightman314.lightmanscurrency.trader.common.TradeContext.TradeResult;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class InfoTab extends ItemInterfaceTab{

	public InfoTab(ItemInterfaceScreen screen) { super(screen, false); }

	ScrollTextDisplay changesDisplay;
	
	DropdownWidget interactionDropdown;
	
	@Override
	public IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }

	@Override
	public Component getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.interface.info"); }
	
	@Override
	public boolean valid(InteractionType interaction) { return true; }
	
	@Override
	public void init() {
		
		this.changesDisplay = this.screen.addRenderableTabWidget(new ScrollTextDisplay(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 15 + ItemTradeButton.HEIGHT, ItemTradeButton.WIDTH, 30, this.screen.getFont(), this.screen::getTradeChangeMessage));
		//Set background color to clear.
		this.changesDisplay.backgroundColor = 0x00000000;
		
		this.interactionDropdown = this.screen.addRenderableTabWidget(IconAndButtonUtil.interactionTypeDropdown(this.screen.getGuiLeft() + 92, this.screen.getGuiTop() + 15 + ItemTradeButton.HEIGHT, 80, this.screen.getFont(), this.screen.getMenu().blockEntity.getInteractionType(), this::onInteractionSelect, this.screen::addRenderableTabWidget));
		//this.interactionDropdown.init(this.screen::addRenderableTabWidget);
		
	}

	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		//Render the currently referenced trade
		ItemTradeButton.renderItemTradeButton(pose, this.screen, this.screen.getFont(), this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 10, this.screen.getReferencedTrade());
		
		if(this.screen.changeInTrades())
		{
			//Render the actual trade
			ItemTradeButton.renderItemTradeButton(pose, this.screen, this.screen.getFont(), this.screen.getGuiLeft() + this.screen.getXSize() - ItemTradeButton.WIDTH - 10, this.screen.getGuiTop() + 10, this.screen.getTrueTrade());
		}
		//Determine whether the trade differences should be rendered.
		this.changesDisplay.visible = this.screen.changeInTrades();
		
		//Display the last fail message (if applicable)
		if(this.screen.getMenu().blockEntity.getInteractionType().trades)
		{
			TradeResult lastResult = this.screen.getMenu().blockEntity.mostRecentTradeResult();
			if(lastResult.hasMessage())
			{
				int messageWidth = this.screen.getFont().width(lastResult.failMessage);
				this.screen.getFont().draw(pose, lastResult.failMessage, this.screen.getGuiLeft() + (this.screen.getXSize() / 2) - (messageWidth / 2), this.screen.getGuiTop() + 80, 0xFF0000);
			}
		}
		
	}

	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY) {
		
		//Render the currently referenced trade's tooltips (no stock or other misc stuff, just the item tooltips & original name)
		ItemTradeButton.tryRenderTooltip(pose, this.screen, this.screen.getReferencedTrade(), this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 10, mouseX, mouseY);
		
		if(this.screen.changeInTrades()) //Render true trade's tooltips
			ItemTradeButton.tryRenderTooltip(pose, this.screen, this.screen.getReferencedTrade(), this.screen.getGuiLeft() + this.screen.getXSize() - ItemTradeButton.WIDTH - 10, this.screen.getGuiTop() + 10, mouseX, mouseY);
		
	}

	@Override
	public void tick() { }

	@Override
	public void onClose() { }
	
	private void onInteractionSelect(int newTypeIndex) {
		InteractionType newType = InteractionType.fromIndex(newTypeIndex);
		this.screen.getMenu().blockEntity.setInteractionType(newType);
		LightmansCurrencyPacketHandler.instance.sendToServer(new MessageSetInteractionType(this.screen.getMenu().blockEntity.getBlockPos(), newType));
	}

}
