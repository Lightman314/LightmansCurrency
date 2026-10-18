package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.builtin;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.WidgetScrollingArea;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.trader.client.permissions.ClientPermission;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.AlliesNode;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.PermissionValue;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AllyPermissionsTab extends SettingsSubTab.ForNode<AlliesNode> {

    public static final int SPACING = 5;

    public AllyPermissionsTab(SettingsClientTab parent) { super(parent,AlliesNode.TYPE); }

    private WidgetScrollingArea holder;

    @Override
    public IconData getIcon() { return ItemIcon.of(Items.BOOKSHELF); }
    @Override
    public Component getName() { return AlliesNode.VALUE_ALLY_PERMS.get(); }

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {
        int width = area.width - 40;
        this.holder = this.addChild(WidgetScrollingArea.builder()
                .atPos(area.pos.offset(20,18))
                .ofSize(width,120)
                .withOldWidget(this.holder)
                .build());
        AlliesNode node = this.getNode();
        if(node == null)
            return;
        MutableInt yPos = new MutableInt(0);
        node.getPermissionData().forEach((perm,value) -> {
            ClientPermission clientPerm = ClientPermission.REGISTRY.getValue(perm.getType());
            int height = clientPerm.addWidget(this.valueGetter(perm),this.permissionChangeRequest(perm),perm,this.holder,width,yPos.intValue());
            yPos.add(height + SPACING);
        });
    }

    private Supplier<PermissionValue<?>> valueGetter(Permission<?> permission){
        return () -> {
            AlliesNode node = this.getNode();
            PermissionValue<?> value = node == null ? null : node.getPermissionData().get(permission);
            if(value == null)
                return permission.createNew();
            return value;
        };
    }

    private <T> Consumer<Object> permissionChangeRequest(Permission<T> permission) {
        return newValue -> {
            PermissionValue<?> value = permission.createNew();
            if(permission.isValueType(newValue)) {
                value.setUnsafe(newValue);
                this.sendSettingRequest(FancyPacketMap.map().set("changePermission",LCFancyPacketTypes.PERMISSION_VALUE,value));
            }
        };
    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {

    }

}