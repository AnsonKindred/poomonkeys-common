package poomonkeys.common;

import java.util.ArrayList;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

public class Shot extends Drawable
{

	static final float MAX_START_VELOCITY = .003f; // ratio of screen height
	
	static final float POINT_RADIUS = .02f; // ratio of screen height
	
	ArrayList<Point2D> path;
	int weaponType;
	float pointRadius;
	float maxStartVelocity;
	Point2D v = new Point2D();
	Point2D a = new Point2D();

	public float[] baseGeometry = new float[12];
	
	private Player player;
	
	public Shot()
	{
		this.weaponType = 0;
	}
	
	public Shot(Player player, int weaponType, float x, float y, float vx, float vy, float ax, float ay, float viewWidth, float viewHeight)
	{
		this.weaponType = weaponType;
		this.player = player;
		this.drawMode = GL2.GL_TRIANGLE_STRIP;
		
		pointRadius      = POINT_RADIUS * viewHeight;
		maxStartVelocity = MAX_START_VELOCITY * viewHeight;
		
		this.x = x;
		this.y = y;
		
		v.x = vx;
		v.y = vy;
		a.x = ax;
		a.y = ay;
	}
	
	public Shot(Player player, int weaponType, float power, float ax, float ay, float viewWidth, float viewHeight)
	{
		this.weaponType = weaponType;
		this.player = player;
		this.drawMode = GL2.GL_TRIANGLE_STRIP;
		
		pointRadius      = POINT_RADIUS * viewHeight;
		maxStartVelocity = MAX_START_VELOCITY * viewHeight;
		
		a.x = ax;
		a.y = ay;
		
		// starting point of shot, set to the top of the tank
		x = player.tank.x+player.tank.width/2;
		y = player.tank.y+player.tank.height;
		
		// Get a normalized velocity vector from the turret angle
		int flip = player.tank.turret.getRotation() < 0 ? 1 : -1;
		v.x = flip;
		v.y = flip*player.tank.turret.getSlope();
		VectorUtil.normalize2D(v);
		
		// Adjust the starting position so that it is at the end of the turret
		x += v.x*player.tank.turretLength;
		y += v.y*player.tank.turretLength;
		
		// Magnify the velocity vector by power
		VectorUtil.mult2D(v, power*maxStartVelocity);
	}
	
	public void buildGeometry(float viewWidth, float viewHeight)
	{
		baseGeometry[0]=-pointRadius/2; baseGeometry[1]=-pointRadius/2; baseGeometry[2]=0;
		baseGeometry[3]=-pointRadius/2; baseGeometry[4]=pointRadius/2; baseGeometry[5]=0;
		baseGeometry[6]=pointRadius/2; baseGeometry[7]=-pointRadius/2;  baseGeometry[8]=0;
		baseGeometry[9]=pointRadius/2; baseGeometry[10]=pointRadius/2; baseGeometry[11]=0;
		super.baseGeometry = baseGeometry;
	}
	
	public void update(Renderer renderer)
	{	
		long dTime = renderer.getTimeSinceLastDraw();
		
		x += v.x*dTime;
		y += v.y*dTime;
		v.x += a.x*dTime;
		v.y += a.y*dTime;
	}
	
}
