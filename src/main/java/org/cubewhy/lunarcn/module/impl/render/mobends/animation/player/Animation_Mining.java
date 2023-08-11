package org.cubewhy.lunarcn.module.impl.render.mobends.animation.player;


import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.cubewhy.lunarcn.module.impl.render.mobends.animation.Animation;
import org.cubewhy.lunarcn.module.impl.render.mobends.client.model.ModelRendererBends;
import org.cubewhy.lunarcn.module.impl.render.mobends.client.model.entity.ModelBendsPlayer;
import org.cubewhy.lunarcn.module.impl.render.mobends.data.PlayerData;
import org.cubewhy.lunarcn.module.impl.render.mobends.data.EntityData;

//import me.yuxiangll.jigsaw.client.Utils.render.mobends.animation.Animation;
//import me.yuxiangll.jigsaw.client.Utils.render.mobends.client.model.ModelRendererBends;
//import me.yuxiangll.jigsaw.client.Utils.render.mobends.client.model.entity.ModelBendsPlayer;
//import me.yuxiangll.jigsaw.client.Utils.render.mobends.data.Data_Player;
//import me.yuxiangll.jigsaw.client.Utils.render.mobends.data.EntityData;
public class Animation_Mining extends Animation {
	
	public String getName(){
		return "mining";
	}

	@Override
	public void animate(EntityLivingBase argEntity, ModelBase argModel, EntityData argData) {
		ModelBendsPlayer model = (ModelBendsPlayer) argModel;
		PlayerData data = (PlayerData) argData;
		EntityPlayer player = (EntityPlayer) argEntity;
		
		((ModelRendererBends)model.bipedRightLeg).rotation.setSmoothZ(10,0.3f);
		((ModelRendererBends)model.bipedLeftLeg).rotation.setSmoothZ(-10,0.3f);
		model.renderOffset.setSmoothY(-1.5f,0.3f);
		
		if (player.isSwingInProgress) {
			float speed = 1.8f;
			
			float progress = (player.ticksExisted*speed/20.0f)%1;
			float progress2 = ((player.ticksExisted-2)*speed/20.0f)%1;
			
			float armSwing = (MathHelper.cos(progress*(float)Math.PI*2)+1)/2*-60-30+model.headRotationX*0.5f-30.0f;
			
			
			float armYRot = 30.0f+MathHelper.cos((armSwing-90)/180.0f*3.14f)*-5.0f;
			
			((ModelRendererBends)model.bipedRightArm).rotation.setSmoothX(armSwing,0.7f);
			((ModelRendererBends)model.bipedRightArm).rotation.setSmoothY(-armYRot,0.7f);
			model.renderItemRotation.setSmoothZ(-30.0f,0.3f);
			
			((ModelRendererBends)model.bipedBody).rotation.setSmoothY(MathHelper.sin(progress2*(float)Math.PI*2)*-20);
			((ModelRendererBends)model.bipedHead).rotation.setSmoothX(model.headRotationX-model.bipedBody.rotateAngleX);
			((ModelRendererBends)model.bipedHead).rotation.setSmoothY(model.headRotationY-model.bipedBody.rotateAngleY);
		}
	}
}
