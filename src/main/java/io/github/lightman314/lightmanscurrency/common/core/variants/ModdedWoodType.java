package io.github.lightman314.lightmanscurrency.common.core.variants;

import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nonnull;

public class ModdedWoodType extends WoodType
{
    private final String mod;
    public ModdedWoodType(@Nonnull String name, @Nonnull String mod) { super(name); this.mod = mod; }
    public ModdedWoodType(@Nonnull String name, @Nonnull MapColor mapColor, @Nonnull String mod) { super(name, mapColor); this.mod = mod; }
    @Nonnull
    @Override
    public String getModID() { return this.mod; }
    @Override
    public boolean isValid() { return ModList.get().isLoaded(this.mod); }
}
