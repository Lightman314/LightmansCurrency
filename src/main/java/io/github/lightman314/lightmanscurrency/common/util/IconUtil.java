package io.github.lightman314.lightmanscurrency.common.util;

import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class IconUtil {

    /**
     * Texture file used for miscellaneous icons, most of which are refferred to in the IconData constants below.
     */
    public static final ResourceLocation ICON_TEXTURE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "textures/gui/icons.png");

    public static final IconData ICON_TRADER = IconData.of(ModItems.TRADING_CORE);
    public static final IconData ICON_TRADER_ALT = IconData.of(ICON_TEXTURE, 80, 0);
    public static final IconData ICON_STORAGE = IconData.of(Items.CHEST);

    public static final IconData ICON_COLLECT_COINS = IconData.of(ICON_TEXTURE, 0, 0);
    public static final IconData ICON_STORE_COINS = IconData.of(ICON_TEXTURE, 16, 0);
    public static final IconData ICON_TRADE_RULES = IconData.of(Items.BOOK);
    public static final IconData ICON_SETTINGS = IconData.of(ICON_TEXTURE, 32, 0);

    public static final IconData ICON_BACK = IconData.of(ICON_TEXTURE, 0, 16);
    public static final IconData ICON_LEFT = IconData.of(ICON_TEXTURE, 16, 16);
    public static final IconData ICON_RIGHT = IconData.of(ICON_TEXTURE, 32, 16);
    public static final IconData ICON_UP = IconData.of(ICON_TEXTURE, 112, 16);
    public static final IconData ICON_DOWN = IconData.of(ICON_TEXTURE, 128, 16);

    public static final IconData ICON_SHOW_LOGGER = IconData.of(Items.WRITABLE_BOOK);

    public static Function<IconButton,IconData> ICON_CREATIVE_TOGGLE(@Nonnull Supplier<Boolean> isCreative)
    {
        return (b) -> {
            boolean c = b.isHovered() != isCreative.get();
            return c ? ICON_CREATIVE : ICON_CREATIVE_OFF;
        };
    }
    public static final IconData ICON_CREATIVE = IconData.of(ICON_TEXTURE, 48, 16);
    public static final IconData ICON_CREATIVE_OFF = IconData.of(ICON_TEXTURE, 64, 16);

    public static final IconData ICON_PERSISTENT_DATA = IconData.of(ICON_TEXTURE, 80, 16);

    public static Supplier<IconData> ICON_INTERFACE_ACTIVE(Supplier<Boolean> isActive) {
        return () -> isActive.get() ? ICON_INTERFACE_ON : ICON_INTERFACE_OFF;
    }
    private static final IconData ICON_INTERFACE_ON = IconData.of(Items.REDSTONE_TORCH);
    private static final IconData ICON_INTERFACE_OFF = IconData.of(Items.TORCH);

    public static final IconData ICON_PLUS = IconData.of(ICON_TEXTURE, 0, 32);
    public static final IconData ICON_X = IconData.of(ICON_TEXTURE, 16, 32);

    public static final IconData ICON_WHITELIST = IconData.of(ICON_TEXTURE, 32, 32);
    public static final IconData ICON_BLACKLIST = IconData.of(ICON_TEXTURE, 48, 32);
    public static final IconData ICON_COUNT = IconData.of(ICON_TEXTURE, 64, 32);
    public static final IconData ICON_COUNT_PLAYER = IconData.of(ICON_TEXTURE, 80, 32);
    public static final IconData ICON_TIMED_SALE = IconData.of(Items.CLOCK);
    public static final IconData ICON_DISCOUNT_LIST = IconData.of(ICON_TEXTURE, 96, 32);
    public static final IconData ICON_FREE_SAMPLE = IconData.of(ICON_TEXTURE, 112, 32);
    public static final IconData ICON_PRICE_FLUCTUATION = IconData.of(ICON_TEXTURE, 128, 32);
    public static final IconData ICON_DEMAND_PRICING = IconData.of(ICON_TEXTURE, 144, 32);

    public static final IconData ICON_TRADELIST = IconData.of(ICON_TEXTURE, 48, 0);

    public static final IconData ICON_MODE_DISABLED = IconData.of(Items.BARRIER);
    public static final IconData ICON_MODE_REDSTONE_OFF = IconData.of(ICON_TEXTURE, 64, 0);
    public static final IconData ICON_MODE_REDSTONE = IconData.of(Items.REDSTONE_TORCH);
    public static final IconData ICON_MODE_ALWAYS_ON = IconData.of(Items.REDSTONE_BLOCK);

    public static IconData GetIcon(TraderInterfaceBlockEntity.ActiveMode mode) {
        return switch (mode) {
            case DISABLED -> ICON_MODE_DISABLED;
            case REDSTONE_OFF -> ICON_MODE_REDSTONE_OFF;
            case REDSTONE_ONLY -> ICON_MODE_REDSTONE;
            case ALWAYS_ON -> ICON_MODE_ALWAYS_ON;
        };
    }

    public static final IconData ICON_CHECKMARK = IconData.of(ICON_TEXTURE, 0, 48);

    public static final ItemStack ITEM_ALEX_HEAD;

    public static final IconData ICON_ALEX_HEAD;

    public static final IconData ICON_TAXES = IconData.of(ICON_TEXTURE, 96, 0);

    public static final IconData ICON_ONLINEMODE_TRUE = IconData.of(Items.PLAYER_HEAD);
    public static final IconData ICON_ONLINEMODE_FALSE = ICON_CHECKMARK;

    static {
        ItemStack alexHead = new ItemStack(Items.PLAYER_HEAD);
        PropertyMap map = new PropertyMap();
        map.put("textures",new Property("textures","eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjNiMDk4OTY3MzQwZGFhYzUyOTI5M2MyNGUwNDkxMDUwOWIyMDhlN2I5NDU2M2MzZWYzMWRlYzdiMzc1MCJ9fX0="));
        ResolvableProfile profile = new ResolvableProfile(Optional.empty(),Optional.of(UUIDUtil.uuidFromIntArray(new int[] {-731408145, -304985227, -1778597514, 158507129 })), map);
        alexHead.set(DataComponents.PROFILE, profile);
        ITEM_ALEX_HEAD = alexHead;
        ICON_ALEX_HEAD = IconData.of(alexHead);
    }

}
