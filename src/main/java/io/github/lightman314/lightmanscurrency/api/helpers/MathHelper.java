package io.github.lightman314.lightmanscurrency.api.helpers;

import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class MathHelper {

    private MathHelper() {}

    public static final Vector3fc XP = new Vector3f(1,0,0);
    public static final Vector3fc YP = new Vector3f(0,1,0);
    public static final Vector3fc ZP = new Vector3f(0,0,1);

    public static Vec3 toDoubleVec(Vector3fc vector) { return new Vec3(vector.x(),vector.y(),vector.z()); }

    public static Vector3fc vectorMult(Vector3fc vector, float mult) { return new Vector3f(vector.x() * mult,vector.y() * mult,vector.z() * mult); }

    public static Vector3fc vectorAdd(Vector3fc... vectors) {
        float x = 0;
        float y = 0;
        float z = 0;
        for(Vector3fc vec : vectors) {
            x += vec.x();
            y += vec.y();
            z += vec.z();
        }
        return new Vector3f(x,y,z);
    }

    public static int divideAndRoundUp(int a,int b) {
        int result = a/b;
        if(a % b > 0)
            result++;
        return result;
    }

}