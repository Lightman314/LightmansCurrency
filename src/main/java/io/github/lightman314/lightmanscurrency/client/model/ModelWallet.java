package io.github.lightman314.lightmanscurrency.client.model;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;


public class ModelWallet<T extends LivingEntity> extends BipedModel<T> {
	
	public final ModelRenderer wallet;
	
	public ModelWallet()
	{
		super(0.0f);
		this.texWidth = 32;
		this.texHeight = 16;
		this.wallet = new ModelRenderer(this, 0, 0);
		this.wallet.setPos(0f, 0f, 0f);
		this.wallet.addBox(4F, 11.5F, -2.0F, 2, 4, 4, 0.0F);
	}
	
	@Override
	protected Iterable<ModelRenderer> headParts()
	{
		return ImmutableList.of();
	}
	
	@Override
	protected Iterable<ModelRenderer> bodyParts()
	{
		return ImmutableList.of(this.wallet);
	}
	
	@Override
	public void setupAnim(@Nonnull T t, float v, float v1, float v2, float v3, float v4)
	{
		
	}

}
