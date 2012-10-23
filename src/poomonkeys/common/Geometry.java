package poomonkeys.common;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Don't use directly. Subclasses should always be singleton.
 */
public class Geometry
{
		
	public FloatBuffer vertexBuffer  = null;
	public float[] vertices = null;
	
	public int vertexBufferID = 0;
	
	public int drawMode;
	
	public boolean hasChanged = true;
	public boolean needsCompile = false;

	protected float width = 0;
	protected float height = 0;
	
	public int num_instances;
	public int geometryID = -1;
	
	protected static Geometry instance = null;
	
	public static Geometry getInstance()
	{
		if(instance == null)
		{
			instance = new Geometry();
		}
		
		return instance;
	}
	
	// subclasses are expected to override this method and put something in vertices 
	public void buildGeometry(float viewWidth, float viewHeight) {}
	
	public void finalizeGeometry()
	{
		hasChanged = false;
		
		if(vertices == null) return;
        
        // initialize vertex Buffer (# of coordinate values * 4 bytes per float)  
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * Float.SIZE);
		vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(vertices);
        vertexBuffer.rewind();
        
        needsCompile = true;
	}

	public int getNumPoints() 
	{
		return vertices.length/3;
	}
}
