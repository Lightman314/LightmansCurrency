package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.common.menus.providers.TerminalMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class CPacketOpenNetworkTerminal extends ClientToServerPacket {

	public static final Handler<CPacketOpenNetworkTerminal> HANDLER = new H();

	private final MenuValidator validator;

	public CPacketOpenNetworkTerminal() { this(null); }
	public CPacketOpenNetworkTerminal(@Nullable MenuValidator validator) { this.validator = validator; }

	public void encode(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.validator != null);
        if(this.validator != null)
            this.validator.encode(buffer);
    }

	private static class H extends Handler<CPacketOpenNetworkTerminal>
	{
		@Override
		public CPacketOpenNetworkTerminal decode(FriendlyByteBuf buffer) {
            MenuValidator validator = null;
            if(buffer.readBoolean())
                validator = MenuValidator.decode(buffer);
            return new CPacketOpenNetworkTerminal(validator);
        }

		@Override
		protected void handle(CPacketOpenNetworkTerminal message, Player player) {
            MenuValidator validator = message.validator;
            if(validator == null && player.containerMenu instanceof IValidatedMenu menu)
                validator = menu.getValidator();
            if(validator != null && !validator.isNull())
                TerminalMenuProvider.OpenMenu(player, validator);
		}
	}

}
