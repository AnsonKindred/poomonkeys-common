package poomonkeys.common;

import javax.media.opengl.GL2;

public class Tank extends Drawable
{
	static final float TURRENT_LENGTH = .045f; // ratio of screen height
	static final float WIDTH = .05f; // ratio of screen height
	static final float HEIGHT = .07f; // ratio of screen height

	float baseGeometry[];
	
	public Drawable turret = new Drawable();
	float turretLength = 5;
	
	float xRatio, yRatio;
	
	public Tank()
	{
		this.registerDrawable(turret);
	}
	
	public void buildGeometry(float viewWidth, float viewHeight)
	{
		width = WIDTH*viewWidth;
		height = HEIGHT*viewHeight;
		float[] baseGeometry = {
		            // X, Y
					0, 0, 0,
					width, 0f,0,
					width/2, height,0
		        };

		this.drawMode = GL2.GL_LINE_LOOP;
		super.baseGeometry = baseGeometry;
		
		x = viewWidth*xRatio;
		y = viewHeight*yRatio;
		
		turretLength = TURRENT_LENGTH*viewHeight;
		float turretGeometry[] = {
				0, 0, 0,
				0, turretLength, 0
			};
		turret.drawMode = GL2.GL_LINES;
		turret.baseGeometry = turretGeometry;
		turret.x = width/2;
		turret.y = height;
	}
	
	public void init(float viewWidth, float viewHeight)
	{
		x = (float) (Math.random()*viewWidth)-viewWidth/2;
		y = (float) (Math.random()*viewHeight)-viewHeight/2;
		xRatio = x/viewWidth;
		yRatio = y/viewHeight;
		super.init(viewWidth, viewHeight);
	}
	
}
