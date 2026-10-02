package io.github.lightman314.lightmanscurrency.client.features.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

public final class BlockStateRenderState implements PictureInPictureRenderState {

    public static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

    public final BlockModelRenderState state;
    public final int x;
    public final int y;
    public final Quaternionfc rotation;
    public final float scale;
    private final int intScale;
    public BlockStateRenderState(BlockState state, int x, int y,Quaternionfc rotation) { this(state,x,y,rotation,16f); }
    public BlockStateRenderState(BlockState state, int x, int y,Quaternionfc rotation,float scale) {
        BlockModelRenderState modelState = new BlockModelRenderState();
        Minecraft.getInstance().getBlockModelResolver().update(modelState,state, DisplayRenderer.BLOCK_DISPLAY_CONTEXT);
        this(modelState,x,y,rotation,scale);
    }
    public BlockStateRenderState(BlockModelRenderState state, int x, int y,Quaternionfc rotation) { this(state,x,y,rotation,16f); }
    public BlockStateRenderState(BlockModelRenderState state, int x, int y,Quaternionfc rotation,float scale) {
        this.state = state;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.scale = scale;
        this.intScale = Mth.ceil(this.scale);
    }

    @Override
    public int x0() { return this.x; }
    @Override
    public int x1() { return this.x  + this.intScale; }
    @Override
    public int y0() { return this.y; }
    @Override
    public int y1() { return this.y + this.intScale; }
    @Override
    public float scale() { return this.scale; }
    @Override
    @Nullable
    public ScreenRectangle scissorArea() { return null; }
    @Override
    @Nullable
    public ScreenRectangle bounds() { return PictureInPictureRenderState.getBounds(this.x,this.y,this.x + this.intScale,this.y + this.intScale,null); }

}
