package io.github.lightman314.lightmanscurrency.common.notifications.types;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.deprecated.RemovedNotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

public class DeprecatedNotificationTypes {

    public static final NotificationType<ChangeSettingNotification> ADD_REMOVE_TRADE = new AddRemoveTradeType();

    private static class AddRemoveTradeType extends RemovedNotificationType<ChangeSettingNotification>
    {
        @Override
        protected ChangeSettingNotification createNew() {
            return ChangeSettingNotification.simple(PlayerReference.NULL,LCText.DATA_ENTRY_TRADER_TRADE_COUNT.get(),0);
        }
        @Override
        protected ChangeSettingNotification loadAndConvert(CompoundTag tag, HolderLookup.Provider lookup) {
            tag = tag.copy();
            //"Player" field is the same so no change needed
            int newCount = tag.getInt("NewCount");
            //Write the "Setting" field directly
            tag.putString("Setting", Component.Serializer.toJson(LCText.DATA_ENTRY_TRADER_TRADE_COUNT.get(),lookup));
            //Turn the "NewCount" field into a "NewValue" field
            tag.putString("NewValue",Component.Serializer.toJson(EasyText.literal(String.valueOf(newCount)),lookup));
            return ChangeSettingNotification.SIMPLE_TYPE.loadOldData(tag,lookup);
        }

    }


}
