package io.github.lightman314.lightmanscurrency.client.features.trader.item.block_entity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;

public class ArmorDisplayTraderRenderState extends BlockEntityRenderState {

    public float rotation = 0f;

    public ArmorStandRenderState armorState = new ArmorStandRenderState();

}