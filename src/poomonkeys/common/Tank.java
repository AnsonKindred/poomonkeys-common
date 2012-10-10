package poomonkeys.common;

import javax.media.opengl.GL2;

public class Tank extends Drawable
{
	static final float TURRENT_LENGTH = 1f;
	static final float WIDTH = 2f; 
	static final float HEIGHT = 2f; 

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
		super.vertices = baseGeometry;
		
		turretLength = TURRENT_LENGTH;
		float turretGeometry[] = {
				0, 0, 0,
				0, turretLength, 0
			};
		turret.drawMode = GL2.GL_LINES;
		turret.vertices = turretGeometry;
		turret.p[0] = 0;
		turret.p[1] = height/2;
	}
	
	public void intersectTerrain(Terrain t, float[] intersect)
	{
		removeFromPhysicsEngine = true;
		this.p[0] += this.v[0]*intersect[2];
		this.p[1] += this.v[1]*intersect[2];
		this.v[0] = 0;
		this.v[1] = 0;
	}
	
	public void underTerrain()
	{
		this.v[0] = 0;
		this.v[1] = 0;
	}
}
