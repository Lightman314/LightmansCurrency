package io.github.lightman314.lightmanscurrency.api.world.menu;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.helpers.debug.DebugHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.ItemHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.IRegistryAccess;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class FancyMenu extends AbstractContainerMenu implements ISidedContext, IRegistryAccess {

    private final Player player;
    public final Player getPlayer() { return this.player; }

    @Override
    public final boolean isClient() { return this.player.level().isClientSide(); }
    @Override
    public final HolderLookup.Provider registryAccess() { return this.player.registryAccess(); }

    protected FancyMenu(@Nullable MenuType<?> menuType, int containerId, Player player) {
        super(menuType, containerId);
        this.player = player;
    }

    protected final void debugSlotCount() {
        LightmansCurrency.LogDebug(this.getClass().getSimpleName() + " on the " + DebugHelper.sideName(this) + " has " + this.slots.size() + " slots!");
    }

    //Assembles a simple container and calls the Container version of the clearContainer method
    public final void clearContainer(List<ItemStack> items)
    {
        Container c = new SimpleContainer(items.size());
        for(int i = 0; i < items.size(); ++i)
            c.setItem(i,items.get(i));
        this.clearContainer(c);
    }
    //Calls the player-sensitive version of the clearContainer method using the locally stored player
    public final void clearContainer(Container container) { this.clearContainer(this.player,container); }
    //Assembles a list of container contents and then calls the List version of the clearContainer method
    public final void clearContainer(ResourceHandler<ItemResource> handler) {
        Transaction transaction = Transaction.openRoot();
        List<ItemStack> drops = new ArrayList<>();
        for(int i = 0; i < handler.size(); ++i)
        {
            ItemResource r = handler.getResource(i);
            if(r.isEmpty())
                continue;
            boolean loop = true;
            //Loop through the extraction process in case the handler has more than Integer.MAX_VALUE items
            while(loop)
            {
                int extracted = handler.extract(i,r,handler.getAmountAsInt(i),transaction);
                if(extracted > 0)
                    drops.addAll(ItemHelper.splitStack(r.toStack(extracted)));
                else
                    loop = false;
            }
        }
        transaction.commit();
        this.clearContainer(drops);
    }

    public final boolean moveStackTo(ItemStack stack,int startSlot,int endSlot,boolean backwards) { return this.moveItemStackTo(stack,startSlot,endSlot,backwards); }

    public abstract static class Validated extends FancyMenu implements IValidatedMenu
    {

        private final MenuValidator mainValidator;
        private final List<MenuValidator> validators = new ArrayList<>();

        protected Validated(@Nullable MenuType<?> menuType, int containerId, Player player, MenuValidator validator) {
            super(menuType, containerId, player);
            this.mainValidator = validator;
            this.validators.add(this.mainValidator);
        }

        @Override
        public MenuValidator getValidator() { return this.mainValidator; }

        @Override
        public void addValidator(MenuValidator validator) {
            if(!this.validators.contains(validator))
                this.validators.add(validator);
        }

        @Override
        public boolean stillValid(Player player) {
            for(MenuValidator v : new ArrayList<>(this.validators))
            {
                if(!v.stillValid(player))
                    return false;
            }
            return true;
        }
    }

}
