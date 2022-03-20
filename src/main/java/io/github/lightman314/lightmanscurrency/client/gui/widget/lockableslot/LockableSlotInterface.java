package io.github.lightman314.lightmanscurrency.client.gui.widget.lockableslot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.menus.slots.LockableSlot;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.misc.MessageLockableSlotInteraction;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class LockableSlotInterface {

	List<Pair<Integer,LockableSlotButton>> slotButtons = new ArrayList<>();
	
	public static final String DEFAULT_KEY = "LockableSlots";
	
	private final AbstractContainerMenu menu;
	private final ILockableSlotInteractableMenu interactableMenu;
	private final String messageKey;
	
	public LockableSlotInterface(AbstractContainerScreen<?> screen, ILockableSlotInteractableMenu interactableMenu, Consumer<LockableSlotButton> addButton) { this(screen, interactableMenu, DEFAULT_KEY, addButton); }
	
	public LockableSlotInterface(AbstractContainerScreen<?> screen, ILockableSlotInteractableMenu interactableMenu, String messageKey, Consumer<LockableSlotButton> addButton) {
		this.messageKey = messageKey;
		this.menu = screen.getMenu();
		this.interactableMenu = interactableMenu;
		for(Slot slot : screen.getMenu().slots)
		{
			if(slot instanceof LockableSlot) {
				LockableSlot s = (LockableSlot)slot;
				int slotIndex = s.getContainerSlot();
				LockableSlotButton button = new LockableSlotButton(screen, s, this::OnPress);
				addButton.accept(button);
				this.slotButtons.add(Pair.of(slotIndex, button));
			}
		}
	}
	
	public void renderTooltips(Screen screen, PoseStack pose, int mouseX, int mouseY) {
		for(Pair<Integer,LockableSlotButton> buttonPair : this.slotButtons)
		{
			if(buttonPair.getSecond().isMouseOver(mouseX, mouseY))
				buttonPair.getSecond().renderTooltip(screen, pose, mouseX, mouseY);
		}
	}
	
	private void OnPress(Button button) {
		if(button instanceof LockableSlotButton)
		{
			int containerIndex = this.getSlotIndex((LockableSlotButton)button);
			if(containerIndex < 0)
				return;
			//Handle the interaction client side
			this.interactableMenu.OnLockableSlotInteraction(this.messageKey, containerIndex, this.menu.getCarried());
			//Send interaction message to server
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageLockableSlotInteraction(this.messageKey, containerIndex, this.menu.getCarried()));
		}
	}
	
	private int getSlotIndex(LockableSlotButton button) {
		for(Pair<Integer,LockableSlotButton> buttonPair : this.slotButtons)
		{
			if(buttonPair.getSecond() == button)
				return buttonPair.getFirst();
		}
		return -1;
	}
	
	public interface ILockableSlotInteractableMenu {
		public void OnLockableSlotInteraction(String key, int index, ItemStack carriedStack);
	}
	
}
