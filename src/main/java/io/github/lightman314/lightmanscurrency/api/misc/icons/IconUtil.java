package io.github.lightman314.lightmanscurrency.api.misc.icons;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class IconUtil {

    public static final IconData ICON_TRADER = ItemIcon.ofItem(ModItems.TRADING_CORE);
    public static final IconData ICON_TRADER_ALT = IconIcon.ofIcon(VersionUtil.lcResource("trader_alt"));
    public static final IconData ICON_STORAGE = ItemIcon.ofItem(Items.CHEST);

    public static final IconData ICON_COLLECT_COINS = IconIcon.ofIcon(VersionUtil.lcResource("collect_coins"));
    public static final IconData ICON_STORE_COINS = IconIcon.ofIcon(VersionUtil.lcResource("store_coins"));
    public static final IconData ICON_TRADE_RULES = ItemIcon.ofItem(Items.BOOK);
    public static final IconData ICON_SETTINGS = IconIcon.ofIcon(VersionUtil.lcResource("settings"));

    public static final IconData ICON_BACK = IconIcon.ofIcon(VersionUtil.lcResource("arrow_back"));
    public static final IconData ICON_LEFT = IconIcon.ofIcon(VersionUtil.lcResource("arrow_left"));
    public static final IconData ICON_RIGHT = IconIcon.ofIcon(VersionUtil.lcResource("arrow_right"));
    public static final IconData ICON_UP = IconIcon.ofIcon(VersionUtil.lcResource("arrow_up"));
    public static final IconData ICON_DOWN = IconIcon.ofIcon(VersionUtil.lcResource("arrow_down"));
    public static final IconData ICON_PLUS = IconIcon.ofIcon(VersionUtil.lcResource("sign_plus"));
    public static final IconData ICON_MINUS = IconIcon.ofIcon(VersionUtil.lcResource("sign_minus"));
    public static final IconData ICON_X = IconIcon.ofIcon(VersionUtil.lcResource("sign_x"));
    public static final IconData ICON_CHECKMARK = IconIcon.ofIcon(VersionUtil.lcResource("checkmark"));

    public static final IconData ICON_SHOW_LOGGER = ItemIcon.ofItem(Items.WRITABLE_BOOK);

    public static Function<IconButton,IconData> ICON_CREATIVE_TOGGLE(Supplier<Boolean> isCreative)
    {
        return (b) -> {
            boolean c = b.isHoveredOrFocused() != isCreative.get();
            return c ? ICON_CREATIVE : ICON_CREATIVE_OFF;
        };
    }
    public static final IconData ICON_CREATIVE = IconIcon.ofIcon(VersionUtil.lcResource("creative"));
    public static final IconData ICON_CREATIVE_OFF = MultiIcon.ofMultiple(ICON_CREATIVE,ICON_X);

    public static final IconData ICON_PERSISTENT_DATA = IconIcon.ofIcon(VersionUtil.lcResource("persistent"));

    public static Supplier<IconData> ICON_INTERFACE_ACTIVE(Supplier<Boolean> isActive) {
        return () -> isActive.get() ? ICON_INTERFACE_ON : ICON_INTERFACE_OFF;
    }
    private static final IconData ICON_INTERFACE_ON = ItemIcon.ofItem(Items.REDSTONE_TORCH);
    private static final IconData ICON_INTERFACE_OFF = ItemIcon.ofItem(Items.TORCH);

    public static final IconData ICON_COUNT = IconIcon.ofIcon(VersionUtil.lcResource("counting"));
    public static final IconData ICON_WHITELIST = MultiIcon.ofMultiple(ICON_COUNT,ICON_CHECKMARK);
    public static final IconData ICON_BLACKLIST = MultiIcon.ofMultiple(ICON_COUNT,ICON_X);
    public static final IconData ICON_COUNT_PLAYER = MultiIcon.ofMultiple(ICON_COUNT,IconIcon.ofIcon(VersionUtil.lcResource("player_head")));
    public static final IconData ICON_TIMED_SALE = ItemIcon.ofItem(Items.CLOCK);
    public static final IconData ICON_DISCOUNT_LIST = MultiIcon.ofMultiple(ICON_COUNT,ItemIcon.ofItem(ModItems.COIN_GOLD));
    public static final IconData ICON_FREE_SAMPLE = IconIcon.ofIcon(VersionUtil.lcResource("free_sample"));
    public static final IconData ICON_PRICE_FLUCTUATION = IconIcon.ofIcon(VersionUtil.lcResource("price_fluctuation"));
    public static final IconData ICON_DEMAND_PRICING = IconIcon.ofIcon(VersionUtil.lcResource("demand_pricing"));
    public static final IconData ICON_DAILY_TRADE = IconIcon.ofIcon(VersionUtil.lcResource("daily_trades"));

    public static final IconData ICON_TRADELIST = IconIcon.ofIcon(VersionUtil.lcResource("trade_list"));

    public static final IconData ICON_MODE_DISABLED = ItemIcon.ofItem(Items.BARRIER);
    public static final IconData ICON_MODE_REDSTONE_OFF = IconIcon.ofIcon(VersionUtil.lcResource("redstone_off"));
    public static final IconData ICON_MODE_REDSTONE = ItemIcon.ofItem(Items.REDSTONE_TORCH);
    public static final IconData ICON_MODE_ALWAYS_ON = ItemIcon.ofItem(Items.REDSTONE_BLOCK);

    public static IconData GetIcon(TraderInterfaceBlockEntity.ActiveMode mode) {
        return switch (mode) {
            case DISABLED -> ICON_MODE_DISABLED;
            case REDSTONE_OFF -> ICON_MODE_REDSTONE_OFF;
            case REDSTONE_ONLY -> ICON_MODE_REDSTONE;
            case ALWAYS_ON -> ICON_MODE_ALWAYS_ON;
        };
    }

    public static final ItemStack ITEM_ALEX_HEAD;

    public static final IconData ICON_ALEX_HEAD;

    public static final IconData ICON_TAXES = IconIcon.ofIcon(VersionUtil.lcResource("bank"));

    public static final IconData ICON_ONLINEMODE_TRUE = ItemIcon.ofItem(Items.PLAYER_HEAD);
    public static final IconData ICON_ONLINEMODE_FALSE = ICON_CHECKMARK;

    static {
        ItemStack alexHead = new ItemStack(Items.PLAYER_HEAD);
        PropertyMap map = new PropertyMap();
        map.put("textures",new Property("textures","eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjNiMDk4OTY3MzQwZGFhYzUyOTI5M2MyNGUwNDkxMDUwOWIyMDhlN2I5NDU2M2MzZWYzMWRlYzdiMzc1MCJ9fX0="));
        ResolvableProfile profile = new ResolvableProfile(Optional.empty(),Optional.of(UUIDUtil.uuidFromIntArray(new int[] {-731408145, -304985227, -1778597514, 158507129 })), map);
        alexHead.set(DataComponents.PROFILE, profile);
        ITEM_ALEX_HEAD = alexHead;
        ICON_ALEX_HEAD = ItemIcon.ofItem(alexHead);
    }

}
