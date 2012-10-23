package poomonkeys.common;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;
import java.util.LinkedList;

public class Drawable
{
	public ArrayList<Drawable> drawables = new ArrayList<Drawable>();
	public ArrayList<EventListener> listenerList = new ArrayList<EventListener>();
	
	public float[] p = new float[2];
	public float[] v = new float[2];
	public float[] a = new float[2];
	public float m = 1;
	
	public float rotation = 0;
	public Point2D scale = new Point2D(1, 1);
	public float slope    = 0;
	
	float width, height;
	
	public Geometry geometry = null;
	public FloatBuffer vertexBuffer  = null;
	public float[] vertices = null;
	public int drawMode;
	
	public boolean didInit = false;
	public boolean removeFromGLEngine = false;
	public boolean removeFromPhysicsEngine = false;
	public boolean isUnderTerrain;
	public boolean isTouchingTerrain = true;
	public boolean needsPositionUpdated = true;
	
	public void registerDrawable(Drawable d)
    {
    	drawables.add(d);
    }
	
	public float getRotation()
	{
		return rotation;
	}
	
	public void setRotation(float x, float y)
	{
		float theta = (float) Math.atan(y/x);
		rotation = (float) Math.toDegrees(theta)-90;
		slope = y/x;
		if(x < 0) rotation += 180;
	}
	
	public void setRotation(float deg)
	{
		rotation = deg;
		slope = (float) Math.tan(Math.toRadians(deg+90));
	}

	public float getSlope()
	{
		return slope;
	}

	public void init(float viewWidth, float viewHeight) 
	{
		buildGeometry(viewWidth, viewHeight);
		finalizeGeometry();
		if(geometry != null && geometry.hasChanged)
		{
			geometry.buildGeometry(viewWidth, viewHeight);
			//geometry.finalizeGeometry();
		}
		for(int i=0; i < drawables.size(); i++) {
			if(!drawables.get(i).didInit) {
				drawables.get(i).init(viewWidth, viewHeight);
			}
		}
		didInit = true;
	}
	
	public void dispose()
	{
		for(int i=0; i < drawables.size(); i++) drawables.get(i).dispose();
	}
	
	public void reshape(float width, float height) 
	{
		if(geometry != null)
		{
			geometry.buildGeometry(width, height);
			//geometry.finalizeGeometry();
		}
	}

	public Geometry getGeometry() 
	{
		return geometry;	
	}
	
    public void addGLClickListener(GLClickListener listener)
    {
        listenerList.add(listener);
    }

    public void removeGLClickListener(GLClickListener listener) 
    {
        listenerList.remove(listener);
    }

    void fireGLClick(GLClickEvent evt) 
    {
    	Iterator<EventListener> itr = listenerList.iterator();
    	while (itr.hasNext()) 
    	{
    		// This is sure to fail at some point
    		GLClickListener listener = (GLClickListener) itr.next();
            listener.glClicked(evt);
        }
    }

	public void intersectTerrain(Terrain t, float[] intersect) {}

	public void underTerrain(Terrain t) 
	{
		isUnderTerrain = true;
	}

	public void aboveTerrain() 
	{
		isUnderTerrain = false;
	}
	
	public void setHeight(float h)
	{
		height = h;
	}

	public void setWidth(float w)
	{
		width = w;
	}

	public float getHeight()
	{
		return height;
	}

	public float getWidth()
	{
		return width;
	}
	
	public void buildGeometry(float viewWidth, float viewHeight) {}
	
	public void finalizeGeometry()
	{
		if(vertices == null) return;
        
        // initialize vertex Buffer (# of coordinate values * 4 bytes per float)  
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * Float.SIZE);
		vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        // add the coordinates to the FloatBuffer
        vertexBuffer.put(vertices);
        vertexBuffer.rewind();
	}
	
	public int getNumPoints() 
	{
		return vertices.length/3;
	}
}
