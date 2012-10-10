package poomonkeys.common;

import javax.media.opengl.GL2;

public class DirtGeometry extends Geometry
{
	protected static DirtGeometry instance = null;
	
	public static DirtGeometry getInstance()
	{
		if(instance == null)
		{
			instance = new DirtGeometry();
		}
		
		return instance;
	}
	
	public void buildGeometry(float viewWidth, float viewHeight)
	{
		vertices = new float[4 * 3];
		vertices[0] = -.5f;
		vertices[1] = -.5f;
		vertices[2] = 0;
		vertices[3] = -.5f;
		vertices[4] = .5f;
		vertices[5] = 0;
		vertices[6] = .5f;
		vertices[7] = .5f;
		vertices[8] = 0;
		vertices[9] = .5f;
		vertices[10] = -.5f;
		vertices[11] = 0;
		
		drawMode = GL2.GL_LINE_LOOP;
	}
}
