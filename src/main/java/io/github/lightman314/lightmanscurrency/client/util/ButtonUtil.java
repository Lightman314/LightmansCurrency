package io.github.lightman314.lightmanscurrency.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.builtin.MoneyStorage;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.capability.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.BankNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.MoneyStorageNode;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ButtonUtil {

	public static IconButton finishCollectCoinButton(IconButton.Builder builder, Player player, Supplier<TraderData> traderSource)
	{
		return builder
				.icon(IconUtil.ICON_COLLECT_COINS)
				.addon(EasyAddonHelper.tooltips(() -> {
					TraderData trader = traderSource.get();
					if(trader != null)
                    {
                        BankNode bankNode = trader.getNode(BankNode.TYPE);
                        MoneyStorageNode moneyNode = trader.getNode(MoneyStorageNode.TYPE);
                        if(moneyNode == null)
                            return new ArrayList<>();
                        if(bankNode == null || !bankNode.hasBankAccount() && !moneyNode.getStorage().isEmpty())
                        {
                            List<Component> result = new ArrayList<>();
                            result.add(LCText.TOOLTIP_TRADER_COLLECT_COINS.get());
                            IMoneyHolder storage = trader.getStoredMoney();
                            for(MoneyValue value : storage.getStoredMoney().allValues())
                                result.add(value.getText());
                            return result;
                        }
                    }
					return new ArrayList<>();
				}))
				.addon(EasyAddonHelper.visibleCheck(() -> {
					TraderData trader = traderSource.get();
					if(trader == null)
						return false;
                    if(trader.getNode(MoneyStorageNode.TYPE) == null)
                        return false;
                    if(trader.shouldStoreMoney())
                        return false;
					return trader.hasPermission(player, Permissions.COLLECT_COINS) && !BankNode.isLinkedToBankAccount(trader);
				}))
				.addon(EasyAddonHelper.activeCheck(() -> {
					TraderData trader = traderSource.get();
					if(trader == null)
						return false;
                    MoneyStorageNode node = trader.getNode(MoneyStorageNode.TYPE);
					return node != null && !node.getStorage().isEmpty();
				}))
				.build();
	}
	
	public static IconButton finishCollectCoinButton(IconButton.Builder builder, Supplier<MoneyStorage> moneyStorageSource)
	{
		return builder
				.icon(IconUtil.ICON_COLLECT_COINS)
				.addon(EasyAddonHelper.tooltips(() -> {
					MoneyStorage storage = moneyStorageSource.get();
					if(storage != null && !storage.isEmpty())
					{
						List<Component> result = new ArrayList<>();
						result.add(LCText.TOOLTIP_TRADER_COLLECT_COINS.get());
						for(MoneyValue value : storage.allValues())
							result.add(value.getText());
						return result;
					}
					return new ArrayList<>();
				}))
				.build();
	}
}
