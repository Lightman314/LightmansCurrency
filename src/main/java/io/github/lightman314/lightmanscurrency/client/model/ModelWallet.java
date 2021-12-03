package io.github.lightman314.lightmanscurrency.client.model;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class ModelWallet<T extends LivingEntity> extends BipedModel<T> {
	
	public final ModelRenderer wallet;
	
	public ModelWallet()
	{
		super(0.0f);
		this.textureWidth = 32;
		this.textureHeight = 16;
		this.wallet = new ModelRenderer(this, 0, 0);
		this.wallet.setRotationPoint(0f, 0f, 0f);
		this.wallet.addBox(4F, 11.5F, -2.0F, 2, 4, 4, 0.0F);
	}
	
	@Override
	protected Iterable<ModelRenderer> getHeadParts()
	{
		return ImmutableList.of();
	}
	
	@Override
	protected Iterable<ModelRenderer> getBodyParts()
	{
		return ImmutableList.of(this.wallet);
	}
	
	public void setupAngles(BipedModel<T> model)
	{
		this.wallet.copyModelAngles(model.bipedBody);
	}

}
