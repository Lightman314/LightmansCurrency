package io.github.lightman314.lightmanscurrency.common.menus.variant;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.menus.LazyMessageMenu;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class VariantSelectMenu extends LazyMessageMenu {

    public VariantSelectMenu(MenuType<?> type,int id, Inventory inventory) { super(type, id, inventory); }

    public final void SetVariant(@Nullable ResourceLocation variant)
    {
        if(this.isClient())
        {
            if(variant == null)
                this.SendMessageToServer(this.builder().setFlag("ClearVariant"));
            else
                this.SendMessageToServer(this.builder().setResourceLocation("SetVariant",variant));
        }
        else
        {
            if(variant != null && LCConfig.SERVER.variantBlacklist.matches(variant) && !this.player.isCreative())
            {
                LightmansCurrency.LogWarning(this.player.getName().getString() + " just tried to assign a blacklisted Model Variant (" + variant + ")!");
                return;
            }
            this.changeVariant(variant);
        }
    }

    protected abstract void changeVariant(@Nullable ResourceLocation newVariant);

    @Nullable
    public abstract ResourceLocation getSelectedVariant();

    @Override
    public final void HandleMessage(LazyPacketData message) {
        if(message.contains("SetVariant"))
            this.SetVariant(message.getResourceLocation("SetVariant"));
        if(message.contains("ClearVariant"))
            this.SetVariant(null);
    }

}