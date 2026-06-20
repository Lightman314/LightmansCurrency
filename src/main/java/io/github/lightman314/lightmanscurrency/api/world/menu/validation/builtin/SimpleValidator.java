package io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin;

import com.google.common.base.Predicates;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.MenuValidator;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

public class SimpleValidator implements MenuValidator {

    public static final StreamCodec<ByteBuf,SimpleValidator> STREAM_CODEC = StreamHelper.uncheckedUnit(new SimpleValidator(Predicates.alwaysTrue()));

    private final Predicate<Player> test;
    private final Runnable onFail;
    public SimpleValidator(Predicate<Player> test) { this(test,() -> {}); }
    public SimpleValidator(Predicate<Player> test,Runnable onFail) { this.test = test; this.onFail = onFail; }

    @Override
    public StreamCodec<ByteBuf,SimpleValidator> getType() { return STREAM_CODEC; }

    @Override
    public boolean stillValid(Player player) {
        boolean result = this.test.test(player);
        if(!result)
            this.onFail.run();
        return result;
    }

}