package poomonkeys.common;

import javax.media.opengl.GL2;

public class Tank extends Drawable
{
	static final float TURRENT_LENGTH = 2f;
	static final float WIDTH = 4f; 
	static final float HEIGHT = 4f; 

	float baseGeometry[];
	
	public Drawable turret = new Drawable();
	float turretLength = 5;
	
	public Tank()
	{
		this.registerDrawable(turret);
	}
	
	public void buildGeometry(float viewWidth, float viewHeight)
	{
		width = WIDTH;
		height = HEIGHT;
		float[] baseGeometry = {
		            // X, Y
					0, 0, 0,
					width, 0f,0,
					width/2, height,0
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
		turret.p.x = width/2;
		turret.p.y = height;
	}
	
	public void init(float viewWidth, float viewHeight)
	{
		p.x = (float) (Math.random()*viewWidth);
		p.y = (float) (Math.random()*viewHeight);
		super.init(viewWidth, viewHeight);
	}
	
}
