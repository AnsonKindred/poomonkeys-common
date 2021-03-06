package poomonkeys.common;

import java.nio.FloatBuffer;
import java.util.Stack;

public class Matrix
{
    public static float[] model_view_projection;
    
    private static Stack<float[]> matrixStack = new Stack<float[]>();

    public static final float[] identity_matrix = {
	        1.0f, 0.0f, 0.0f, 0.0f,
	        0.0f, 1.0f, 0.0f, 0.0f,
	        0.0f, 0.0f, 1.0f, 0.0f,
	        0.0f, 0.0f, 0.0f, 1.0f
	    };
    
    public static void loadIdentity()
    {
    	model_view_projection = identity_matrix;
    }

	public static void translate(float x, float y, float z) 
	{
		float[] t = { 
				1.0f, 0.0f, 0.0f, 0.0f, 
				0.0f, 1.0f, 0.0f, 0.0f, 
				0.0f, 0.0f, 1.0f, 0.0f, 
				x   , y   , z   , 1.0f 
			};
		
		model_view_projection = multiply(model_view_projection, t);
	}

	public static void rotate(float a, float x, float y, float z) 
	{
		float s, c;
		
		s = (float) Math.sin(Math.toRadians(a));
		c = (float) Math.cos(Math.toRadians(a));
		
		float[] r = { 
				x * x * (1.0f - c) + c, y * x * (1.0f - c) + z * s,
				x * z * (1.0f - c) - y * s, 0.0f, x * y * (1.0f - c) - z * s,
				y * y * (1.0f - c) + c, y * z * (1.0f - c) + x * s, 0.0f,
				x * z * (1.0f - c) + y * s, y * z * (1.0f - c) - x * s,
				z * z * (1.0f - c) + c, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f 
			};
		
		model_view_projection = multiply(model_view_projection, r);
	}
	
	/*
	 * Introducing projection matrix helper functions
	 * 
	 * OpenGL ES 2 vertex projection transformations gets applied inside the
	 * vertex shader, all you have to do are to calculate and supply a
	 * projection matrix.
	 * 
	 * Its recommended to use the com/jogamp/opengl/util/PMVMatrix.java import
	 * com.jogamp.opengl.util.PMVMatrix; To simplify all your projection model
	 * view matrix creation needs.
	 * 
	 * These helpers here are based on PMVMatrix code and common linear algebra
	 * for matrix multiplication, translate and rotations.
	 */
	public static void glMultMatrixf(FloatBuffer a, FloatBuffer b, FloatBuffer d) 
	{
		final int aP = a.position();
		final int bP = b.position();
		final int dP = d.position();
		for (int i = 0; i < 4; i++) 
		{
			final float ai0 = a.get(aP + i + 0 * 4), ai1 = a.get(aP + i + 1 * 4), ai2 = a.get(aP + i + 2 * 4), ai3 = a.get(aP + i + 3 * 4);
			d.put(dP + i + 0 * 4, ai0 * b.get(bP + 0 + 0 * 4) + ai1 * b.get(bP + 1 + 0 * 4) + ai2 * b.get(bP + 2 + 0 * 4) + ai3 * b.get(bP + 3 + 0 * 4));
			d.put(dP + i + 1 * 4, ai0 * b.get(bP + 0 + 1 * 4) + ai1 * b.get(bP + 1 + 1 * 4) + ai2 * b.get(bP + 2 + 1 * 4) + ai3 * b.get(bP + 3 + 1 * 4));
			d.put(dP + i + 2 * 4, ai0 * b.get(bP + 0 + 2 * 4) + ai1 * b.get(bP + 1 + 2 * 4) + ai2 * b.get(bP + 2 + 2 * 4) + ai3 * b.get(bP + 3 + 2 * 4));
			d.put(dP + i + 3 * 4, ai0 * b.get(bP + 0 + 3 * 4) + ai1 * b.get(bP + 1 + 3 * 4) + ai2 * b.get(bP + 2 + 3 * 4) + ai3 * b.get(bP + 3 + 3 * 4));
		}
	}

	public static float[] multiply(float[] a, float[] b) 
	{
		float[] tmp = new float[16];
		
		glMultMatrixf(FloatBuffer.wrap(a), FloatBuffer.wrap(b), FloatBuffer.wrap(tmp));
		
		return tmp;
	}
	
	public static void pushMatrix()
	{
		matrixStack.push(model_view_projection);
	}
	
	public static void popMatrix()
	{
		model_view_projection = matrixStack.pop();
	}
}
