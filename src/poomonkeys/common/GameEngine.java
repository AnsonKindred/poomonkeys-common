package poomonkeys.common;

public interface GameEngine 
{

	public static final int STATE_CHOOSE_ANGLE = 0;
	public static final int STATE_FIRING_SHOT = 1;
	public static final int STATE_ENEMY_FIRING_SHOT = 2;
	public static final int STATE_TESTING = 3;
	
	public static final int MAX_MOVABLES = 100000;
	public static final int ITEMS_PER_MOVABLE = 7;
	public static final int X = 0;
	public static final int Y = 1;
	public static final int VX = 2;
	public static final int VY = 3;
	public static final int M = 4;
	public static final int VOLUME = 5;
	public static final int GEOM = 6;
	
	public static final Object movableLock = new Object();
	
	public boolean removeMovable(int i);
	public void addMovable(float x, float y, Geometry geom);
	public float[] getMovables();
	public Terrain getTerrain();
	public int getNumMovables();
	public int getGeometryID(Geometry geom);
	public Geometry getGeometry(int id);

}
