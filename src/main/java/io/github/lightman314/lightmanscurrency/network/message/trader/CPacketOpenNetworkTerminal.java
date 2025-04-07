package io.github.lightman314.lightmanscurrency.network.message.trader;

import io.github.lightman314.lightmanscurrency.common.menus.providers.TerminalMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.validation.IValidatedMenu;
import io.github.lightman314.lightmanscurrency.common.menus.validation.MenuValidator;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.SimpleValidator;
import io.github.lightman314.lightmanscurrency.network.packet.ClientToServerPacket;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public class CPacketOpenNetworkTerminal extends ClientToServerPacket {

	private static final Type<CPacketOpenNetworkTerminal> TYPE = new Type<>(VersionUtil.lcResource("c_open_terminal"));
	public static final Handler<CPacketOpenNetworkTerminal> HANDLER = new H();

	private final boolean ignoreExistingValidation;

	public CPacketOpenNetworkTerminal() { this(false); }
	public CPacketOpenNetworkTerminal(boolean ignoreExistingValidation) { super(TYPE); this.ignoreExistingValidation = ignoreExistingValidation; }

	private static void encode(@Nonnull FriendlyByteBuf buffer, @Nonnull CPacketOpenNetworkTerminal message) { buffer.writeBoolean(message.ignoreExistingValidation); }
	private static CPacketOpenNetworkTerminal decode(@Nonnull FriendlyByteBuf buffer) { return new CPacketOpenNetworkTerminal(buffer.readBoolean()); }

	private static class H extends Handler<CPacketOpenNetworkTerminal>
	{
		protected H() { super(TYPE, easyCodec(CPacketOpenNetworkTerminal::encode,CPacketOpenNetworkTerminal::decode)); }
		@Override
		protected void handle(@Nonnull CPacketOpenNetworkTerminal message, @Nonnull IPayloadContext context, @Nonnull Player player) {
			MenuValidator validator = SimpleValidator.NULL;
			if(!message.ignoreExistingValidation && player.containerMenu instanceof IValidatedMenu menu)
				validator = menu.getValidator();
			TerminalMenuProvider.OpenMenu(player, validator);
		}
	}

}
