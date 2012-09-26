package poomonkeys.common;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;

public class Drawable
{
	public ArrayList<Drawable> drawables = new ArrayList<Drawable>();
	public ArrayList<EventListener> listenerList = new ArrayList<EventListener>();
	
	public float x = 0, y = 0;
	
	public float rotation = 0;
	public float slope    = 0;
	public float width    = 0;
	public float height   = 0;
	
	public FloatBuffer geometryBuffer = null;
	public FloatBuffer textureBuffer  = null;
	public FloatBuffer colorBuffer    = null;
	
	public float[] color = {.3f, .4f, .5f, 1};
	
	public float[] vertexColors = null;
	public float[] textureCoords = null;
	public float[] baseGeometry = null;
	public int texture_id = 0;	
	
	public int drawMode;
	
	public boolean didInit = false;
	
	public void registerDrawable(Drawable d)
    {
    	drawables.add(d);
    }
	
	public void buildGeometry(float viewWidth, float viewHeight)
	{
		
	}
	
	public void finalizeGeometry()
	{
		if(baseGeometry == null) return;
        vertexColors = new float[baseGeometry.length/3 * 4];
        for(int c = 0; c < baseGeometry.length/3; c++)
        {
        	int i = c * 4;
        	vertexColors[i] = color[0];
        	vertexColors[i+1] = color[1];
        	vertexColors[i+2] = color[2];
        	vertexColors[i+3] = color[3];
        }
        
        textureCoords = new float[baseGeometry.length/3 * 2];
        float factor = height;
        if(width > height) factor = width;
        for(int c = 0; c < baseGeometry.length/3; c++)
        {
        	textureCoords[c*2] = baseGeometry[c*3]/factor;
        	textureCoords[c*2+1] = 1-baseGeometry[c*3+1]/factor;
        }
        
        // initialize color Buffer  
        ByteBuffer vbb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                vertexColors.length * 4
            ).order(ByteOrder.nativeOrder());
        colorBuffer = vbb.asFloatBuffer();
        colorBuffer.put(vertexColors);    // add the coordinates to the FloatBuffer
        colorBuffer.position(0);          // set the buffer to read the first coordinate
        
        // initialize vertex Buffer  
        vbb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                baseGeometry.length * 4
            ).order(ByteOrder.nativeOrder());
        geometryBuffer = vbb.asFloatBuffer();
        geometryBuffer.put(baseGeometry);    // add the coordinates to the FloatBuffer
        geometryBuffer.position(0);          // set the buffer to read the first coordinate
        
        // initialize texture Buffer  
        vbb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                textureCoords.length * 4
            ).order(ByteOrder.nativeOrder());
        textureBuffer = vbb.asFloatBuffer();
        textureBuffer.put(textureCoords);    // add the coordinates to the FloatBuffer
        textureBuffer.position(0);          // set the buffer to read the first coordinate
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
		for(int i=0; i < drawables.size(); i++) {
			if(!drawables.get(i).didInit) {
				drawables.get(i).init(viewWidth, viewHeight);
			}
		}
		didInit = true;
	}
	
	public void update(Renderer renderer) {}
	
	public void dispose()
	{
		for(int i=0; i < drawables.size(); i++) drawables.get(i).dispose();
	}
	
	public void reshape(int x, int y, int width, int height) 
	{
		buildGeometry(width, height);
		finalizeGeometry();
		for(int i=0; i < drawables.size(); i++) drawables.get(i).reshape(x, y, width, height);
	}

	public FloatBuffer getGeometry() 
	{
		return geometryBuffer;	
	}

	public int getNumPoints() 
	{
		return baseGeometry.length/3;
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
}
