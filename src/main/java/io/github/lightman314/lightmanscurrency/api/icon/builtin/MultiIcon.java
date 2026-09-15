package io.github.lightman314.lightmanscurrency.api.icon.builtin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.IconType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public class MultiIcon extends IconData {

    private static final int LIMIT = 256;

    private static final MapCodec<MultiIcon> MAP_CODEC = IconData.CODEC.listOf(1,LIMIT)
            .fieldOf("icons").xmap(MultiIcon::of,mi -> mi.icons);
    private static final StreamCodec<RegistryFriendlyByteBuf,MultiIcon> STREAM_CODEC = IconData.STREAM_CODEC
            .apply(ByteBufCodecs.list(LIMIT)).map(MultiIcon::of,mi -> mi.icons);

    public static final IconType<MultiIcon> TYPE = new IconType<>(MAP_CODEC,STREAM_CODEC);

    public final ImmutableList<IconData> icons;
    private MultiIcon(List<IconData> icons) {
        this.icons = ImmutableList.copyOf(icons);
        assertValidList(this.icons);
    }

    @Override
    public IconType<?> getType() { return TYPE; }

    private static void assertValidList(List<IconData> icons) {
        if(icons.size() > LIMIT)
            throw new IllegalArgumentException("Cannot make a MultiIcon with more than " + LIMIT + " children!");
        for(IconData icon : icons)
        {
            if(icons instanceof MultiIcon)
                throw new IllegalArgumentException("Cannot nest another MultiIcon within a multi-icon");
        }
    }

    public static MultiIcon of(IconData... icons) { return of(ImmutableList.copyOf(icons)); }
    public static MultiIcon of(List<IconData> icons) {
        //Unpack any child multi-icons
        List<IconData> result = new ArrayList<>();
        for(IconData icon : icons)
        {
            if(result.size() >= LIMIT)
                break;
            if(icon instanceof MultiIcon mi)
            {
                for(IconData i : mi.icons)
                    safeAdd(result,i);
            }
            else
                safeAdd(result,icon);
        }
        return new MultiIcon(result);
    }

    private static void safeAdd(List<IconData> list,IconData icon) {
        if(list.size() >= LIMIT)
            return;
        list.add(icon);
    }

}
