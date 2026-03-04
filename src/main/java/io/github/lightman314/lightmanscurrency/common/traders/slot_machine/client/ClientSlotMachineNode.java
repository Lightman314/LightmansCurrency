package io.github.lightman314.lightmanscurrency.common.traders.slot_machine.client;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.client.BooleanPermission;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.client.PermissionOption;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.ClientTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.client.input.IInputClientNode;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.nodes.SlotMachineNode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

public class ClientSlotMachineNode extends ClientTraderNode<SlotMachineNode> implements IInputClientNode {

    public ClientSlotMachineNode(SlotMachineNode node) { super(node); }

    @Override
    public void addPermissionOptions(TraderData trader, Consumer<PermissionOption> builder) {
        builder.accept(BooleanPermission.of(Permissions.EDIT_TRADES));
    }

    @Override
    public IconData getSettingsTabIcon(TraderData trader) { return ItemIcon.ofItem(Items.HOPPER); }
    @Override
    public Component getSettingsTabTooltip(TraderData trader) { return LCText.TOOLTIP_TRADER_SETTINGS_INPUT_ITEM.get(); }
}
