package poomonkeys.common;

import java.util.ArrayList;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

public class Shot extends Drawable
{

	static final float MAX_START_VELOCITY = .03f; // ratio of screen height
	
	static final float POINT_RADIUS = .02f; // ratio of screen height
	
	ArrayList<Point2D> path;
	int weaponType;
	float pointRadius;
	float maxStartVelocity;

	public float[] baseGeometry = new float[12];
	
	private Player player;
	
	public Shot()
	{
		this.weaponType = 0;
	}
	
	public Shot(Player player, int weaponType, float x, float y, float vx, float vy, float viewWidth, float viewHeight)
	{
		this.weaponType = weaponType;
		this.player = player;
		drawMode = GL2.GL_TRIANGLE_STRIP;
		
		pointRadius      = POINT_RADIUS * viewHeight;
		maxStartVelocity = MAX_START_VELOCITY * viewHeight;
		
		p[0] = x;
		p[1] = y;
		
		v[0] = vx;
		v[1] = vy;
	}
	
	public Shot(Player player, int weaponType, float power, float viewWidth, float viewHeight)
	{
		this.weaponType = weaponType;
		this.player = player;
		this.drawMode = GL2.GL_TRIANGLE_STRIP;
		
		pointRadius      = POINT_RADIUS * viewHeight;
		maxStartVelocity = MAX_START_VELOCITY * viewHeight;
		
		// starting point of shot, set to the top of the tank
		p[0] = player.tank.p[0]+player.tank.width/2;
		p[1] = player.tank.p[1]+player.tank.height;
		
		// Get a normalized velocity vector from the turret angle
		int flip = player.tank.turret.getRotation() < 0 ? 1 : -1;
		v[0] = flip;
		v[1] = flip*player.tank.turret.getSlope();
		VectorUtil.normalize2D(v);
		
		// Adjust the starting position so that it is at the end of the turret
		p[0] += v[0]*player.tank.turretLength;
		p[1] += v[1]*player.tank.turretLength;
		
		// Magnify the velocity vector by power
		VectorUtil.mult2D(v, power*maxStartVelocity);
	}
	
	public void buildGeometry(float viewWidth, float viewHeight)
	{
		baseGeometry[0]=-pointRadius/2; baseGeometry[1]=-pointRadius/2; baseGeometry[2]=0;
		baseGeometry[3]=-pointRadius/2; baseGeometry[4]=pointRadius/2; baseGeometry[5]=0;
		baseGeometry[6]=pointRadius/2; baseGeometry[7]=-pointRadius/2;  baseGeometry[8]=0;
		baseGeometry[9]=pointRadius/2; baseGeometry[10]=pointRadius/2; baseGeometry[11]=0;
		super.vertices = baseGeometry;
	}
	
}
