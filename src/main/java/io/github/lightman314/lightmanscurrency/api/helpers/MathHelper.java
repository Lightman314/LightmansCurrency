package io.github.lightman314.lightmanscurrency.api.helpers;

import org.joml.Vector3f;
import org.joml.Vector3fc;

public final class MathHelper {

    private MathHelper() {}

    public static Vector3fc vectorMult(Vector3fc vector, float mult) { return new Vector3f(vector.x() * mult,vector.y() * mult,vector.z() * mult); }

}