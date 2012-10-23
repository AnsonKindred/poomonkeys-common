package poomonkeys.common;

import java.util.ArrayList;

import javax.media.opengl.GL2;

public interface Renderer 
{
	public static final int MAX_MOVABLES = 100000;
	
	public static final Object movableLock = new Object();
	
	abstract void _render(GL2 gl,int drawMode, Drawable drawable);
	abstract long getTimeSinceLastDraw();
	
	public void registerDrawable(Drawable d);
	
	public boolean removeMovable(int g, int i);
	public void addMovable(float x, float y, Geometry geom);
	public ArrayList<Movable[]> getMovables();

	public int getGeometryID(Geometry geom);
	public Geometry getGeometry(int id);
	
	public float getViewWidth();
	public float getViewHeight();
}
