package io.github.lightman314.lightmanscurrency.client.model;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;


public class ModelWallet<T extends LivingEntity> extends HumanoidModel<T> {
	
	public final ModelPart wallet;
	
	public ModelWallet(ModelPart part)
	{
		super(part);
		this.wallet = part.getChild("wallet");
	}
	
	public static LayerDefinition createLayer()
	{
		CubeDeformation cube = CubeDeformation.NONE;
		MeshDefinition mesh = HumanoidModel.createMesh(cube, 0.0f);
		PartDefinition part = mesh.getRoot();
		part.addOrReplaceChild("wallet", CubeListBuilder.create().texOffs(0, 0).addBox(4f, 11.5f, -2f, 2f, 4f, 4f, cube),
				PartPose.offsetAndRotation(0f, 0f, 0f, 0f, 0f, 0f));
		return LayerDefinition.create(mesh, 32, 16);
	}
	
	@Override
	@Nonnull
	protected Iterable<ModelPart> headParts()
	{
		return ImmutableList.of();
	}
	
	@Override
	@Nonnull
	protected Iterable<ModelPart> bodyParts()
	{
		return ImmutableList.of(this.wallet);
	}

	@Override
	public void setupAnim(@Nonnull T t, float v, float v1, float v2, float v3, float v4)
	{
		
	}
	
}
