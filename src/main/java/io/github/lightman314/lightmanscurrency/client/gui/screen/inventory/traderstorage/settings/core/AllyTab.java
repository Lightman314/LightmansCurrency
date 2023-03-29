package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

import javax.annotation.Nonnull;
import java.util.List;

public class AllyTab extends SettingsSubTab {

    public AllyTab(@Nonnull TraderSettingsClientTab parent) { super(parent); }

    EditBox nameInput;
    Button buttonAddAlly;
    Button buttonRemoveAlly;

    ScrollTextDisplay display;

    @Override
    @Nonnull
    public IconData getIcon() { return IconData.of(Items.PLAYER_HEAD); }

    @Override
    public MutableComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.settings.ally"); }

    @Override
    public boolean canOpen() { return this.menu.hasPermission(Permissions.ADD_REMOVE_ALLIES); }

    @Override
    public void onOpen() {

        this.nameInput = this.addWidget(new EditBox(this.font, this.screen.getGuiLeft() + 20, this.screen.getGuiTop() + 10, 160, 20, EasyText.empty()));
        this.nameInput.setMaxLength(16);

        this.buttonAddAlly = this.addWidget(Button.builder(EasyText.translatable("gui.button.lightmanscurrency.allies.add"), this::AddAlly).pos(this.screen.getGuiLeft() + 20, this.screen.getGuiTop() + 35).size(74, 20).build());
        this.buttonRemoveAlly = this.addWidget(Button.builder(EasyText.translatable("gui.button.lightmanscurrency.allies.remove"), this::RemoveAlly).pos(this.screen.getGuiLeft() + this.screen.getXSize() - 93, this.screen.getGuiTop() + 35).size(74, 20).build());

        this.display = this.addWidget(new ScrollTextDisplay(this.screen.getGuiLeft() + 5, this.screen.getGuiTop() + 60, this.screen.getXSize() - 10, 75, this.font, this::getAllyList));
        this.display.setColumnCount(2);

    }

    @Override
    public void onClose() {

    }

    @Override
    public void renderBG(@Nonnull PoseStack pose, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public void renderTooltips(@Nonnull PoseStack pose, int mouseX, int mouseY) {

    }

    private List<Component> getAllyList()
    {
        List<Component> list = Lists.newArrayList();
        TraderData trader = this.menu.getTrader();
        if(trader != null)
            trader.getAllies().forEach(ally -> list.add(ally.getNameComponent(true)));
        return list;
    }

    @Override
    public void tick() {
        this.nameInput.tick();
        this.buttonAddAlly.active = this.buttonRemoveAlly.active = !this.nameInput.getValue().isEmpty();
    }

    private void AddAlly(Button button)
    {
        String allyName = this.nameInput.getValue();
        CompoundTag message = new CompoundTag();
        message.putString("AddAlly", allyName);
        this.sendNetworkMessage(message);
        this.nameInput.setValue("");
    }

    private void RemoveAlly(Button button)
    {
        String allyName = this.nameInput.getValue();
        CompoundTag message = new CompoundTag();
        message.putString("RemoveAlly", allyName);
        this.sendNetworkMessage(message);
        this.nameInput.setValue("");
    }

}
