package poomonkeys.common;

public class Util 
{

	public static Float[] boxFloats(float[] primary)
	{
		Float[] r = new Float[primary.length];
		for(int i = 0; i < r.length; i++) {
			r[i] = primary[i];
		}
		
		return r;
	}
	
	public static float[] unboxFloats(Float[] ob)
	{
		float[] r = new float[ob.length];
		for(int i = 0; i < r.length; i++) {
			r[i] = ob[i];
		}
		
		return r;
	}
}
