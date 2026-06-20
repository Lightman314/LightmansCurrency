package io.github.lightman314.lightmanscurrency.api.world.menu;

import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.network.message.data.BPacketMenuMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class MessageMenu extends FancyMenu {

    private Consumer<FancyPacketMap> clientListener = m -> {};
    protected MessageMenu(@Nullable MenuType<?> menuType, int containerId,Player player) {
        super(menuType,containerId,player);
    }

    public final void addClientListener(Consumer<FancyPacketMap> listener) { this.clientListener = listener; }

    public final void sendToClient(FancyPacketMap message)
    {
        if(this.isClient())
            return;
        new BPacketMenuMessage(this.containerId,message).sendTo(this.getPlayer());
    }
    public final void sendToServer(FancyPacketMap message)
    {
        if(this.isServer())
            return;
        new BPacketMenuMessage(this.containerId,message).sendToServer();
    }
    public final void send(FancyPacketMap message)
    {
        if(this.isClient())
            this.sendToServer(message);
        else
            this.sendToClient(message);
    }

    public final void receiveMessage(FancyPacketMap message)
    {
        this.handleMessage(message);
        this.clientListener.accept(message);
    }
    protected abstract void handleMessage(FancyPacketMap message);

    public abstract static class Validated extends MessageMenu implements IValidatedMenu
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