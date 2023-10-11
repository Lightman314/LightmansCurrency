package io.github.lightman314.lightmanscurrency.client.model;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;


public class ModelWallet<T extends LivingEntity> extends HumanoidModel<T> {
	
	public final ModelPart wallet;
	
	public ModelWallet(ModelPart part)
	{
		super(part);
		this.wallet = part.getChild("wallet");
	}
	
	@Nonnull
	@Override
	protected Iterable<ModelPart> headParts() { return ImmutableList.of(); }
	
	@Nonnull
	@Override
	protected Iterable<ModelPart> bodyParts()
	{
		return ImmutableList.of(this.wallet);
	}
	
	@Override
	public void setupAnim(@Nonnull T t, float v, float v1, float v2, float v3, float v4)
	{
		
	}

}
