package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.common.menus.providers.TerminalMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CPacketOpenNetworkTerminal extends ClientToServerPacket {

	private static final Type<CPacketOpenNetworkTerminal> TYPE = new Type<>(VersionUtil.lcResource("c_open_terminal"));
	public static final Handler<CPacketOpenNetworkTerminal> HANDLER = new H();

	private final MenuValidator validator;

	public CPacketOpenNetworkTerminal() { this(null); }
	public CPacketOpenNetworkTerminal(@Nullable MenuValidator validator) { super(TYPE); this.validator = validator; }

	private static void encode(FriendlyByteBuf buffer, CPacketOpenNetworkTerminal message) {
        buffer.writeBoolean(message.validator != null);
        if(message.validator != null)
            message.validator.encode(buffer);
    }
	private static CPacketOpenNetworkTerminal decode(FriendlyByteBuf buffer) {
        MenuValidator validator = null;
        if(buffer.readBoolean())
            validator = MenuValidator.decode(buffer);
        return new CPacketOpenNetworkTerminal(validator);
    }

	private static class H extends Handler<CPacketOpenNetworkTerminal>
	{
		protected H() { super(TYPE, easyCodec(CPacketOpenNetworkTerminal::encode,CPacketOpenNetworkTerminal::decode)); }
		@Override
		protected void handle(CPacketOpenNetworkTerminal message, IPayloadContext context, Player player) {
			MenuValidator validator = message.validator;
			if(validator == null && player.containerMenu instanceof IValidatedMenu menu)
				validator = menu.getValidator();
            if(validator != null && !validator.isNull())
			    TerminalMenuProvider.OpenMenu(player, validator);
		}
	}

}
