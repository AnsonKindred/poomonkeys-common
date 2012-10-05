package poomonkeys.common;

import javax.media.opengl.GL2;

public class Tank extends Drawable
{
	static final float TURRENT_LENGTH = 2f;
	static final float WIDTH = 4f; 
	static final float HEIGHT = 4f; 

	float baseGeometry[];
	
	public Drawable turret = new Drawable();
	float turretLength = 1;
	
	public Tank()
	{
		width = WIDTH;
		height = HEIGHT;
		m=2;
		this.registerDrawable(turret);
	}
	
	public void buildGeometry(float viewWidth, float viewHeight)
	{
		float[] baseGeometry = {
		            // X, Y
					-width/2, -height/2, 0,
					width/2, -height/2f,0,
					0, height/2,0
		        };

		this.drawMode = GL2.GL_LINE_LOOP;
		super.baseGeometry = baseGeometry;
		
		turretLength = TURRENT_LENGTH;
		float turretGeometry[] = {
				0, 0, 0,
				0, turretLength, 0
			};
		turret.drawMode = GL2.GL_LINES;
		turret.baseGeometry = turretGeometry;
		turret.x = 0;
		turret.y = height/2;
	}
	
	public void intersectTerrain(Terrain t, float x, float y)
	{
		removeFromPhysicsEngine = true;
		this.v.x = 0;
		this.v.y = 0;
	}
}
