package poomonkeys.common;

import java.util.ArrayList;

import javax.media.opengl.GL2;

public interface Renderer 
{
	public static final int MAX_INSTANCES = 100000;
	
	public static final Object instanceLock = new Object();
	public static final Object drawableLock = new Object();
	
	abstract void _render(GL2 gl,int drawMode, Drawable drawable);
	abstract long getTimeSinceLastDraw();
	
	public void registerDrawable(Drawable d);
	
	public boolean removeInstanceGeometry(int g, int i);
	public void addGeometryInstance(float x, float y, Geometry geom);
	public ArrayList<Movable[]> getMovables();

	public int getGeometryID(Geometry geom);
	public Geometry getGeometry(int id);
	
	public float getViewWidth();
	public float getViewHeight();
}
