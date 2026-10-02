package io.github.lightman314.lightmanscurrency.api.trader.client.nodes.interfaces;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollingWidgetBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.storage.builtin.settings.simple.SettingLabel;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionAccess;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.BiConsumer;

public interface IInputSettingAddon {

    void addAdditionalInputSettings(INodeAccess trader, IPermissionAccess perms, BiConsumer<TraderNodeType<?>, FancyPacketMap> sender, InputAddonsBuider builder);

    final class InputAddonsBuider {

        private final List<ScrollingWidgetBuilder> list;

        public InputAddonsBuider(List<ScrollingWidgetBuilder> list) { this.list = list; }

        public void addSimpleSetting(ScrollingWidgetBuilder builder) { this.list.add(builder); }

        public void addSimpleSettingLabel(TextEntry label) { this.addSimpleSettingLabel(label.get()); }
        public void addSimpleSettingLabel(Component label) { this.addSimpleSettingLabel(label,0xFF404040); }
        public void addSimpleSettingLabel(Component label,int textColor) { this.addSimpleSetting(new SettingLabel(label,textColor)); }

    }

}
