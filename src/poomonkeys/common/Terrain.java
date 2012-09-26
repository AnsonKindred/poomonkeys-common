package poomonkeys.common;

import javax.media.opengl.GL2;

public class Terrain extends Drawable
{

	int NUM_POINTS = 64;
	float yValues[] = new float[NUM_POINTS];

	public Terrain() 
	{
	}
	
	public void buildGeometry(float viewWidth, float viewHeight)
	{
		baseGeometry = new float[NUM_POINTS*3];
		float ratio = viewWidth/(NUM_POINTS-1);
		for(int i = 0; i < NUM_POINTS; i++)
		{
			baseGeometry[i*3] = i*ratio;
			baseGeometry[i*3+1] = yValues[i]*viewHeight;
			baseGeometry[i*3+2] = 0;
		}
		drawMode = GL2.GL_LINE_STRIP;
		x = -viewWidth/2;
		y = -viewHeight/2;
	}
}