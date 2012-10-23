package poomonkeys.common;

import java.util.ArrayList;

public interface GameEngine 
{

	public static final int STATE_CHOOSE_ANGLE = 0;
	public static final int STATE_FIRING_SHOT = 1;
	public static final int STATE_ENEMY_FIRING_SHOT = 2;
	public static final int STATE_TESTING = 3;
	
	public static final Object terrainLock = new Object();
	
	public Terrain getTerrain();

}
