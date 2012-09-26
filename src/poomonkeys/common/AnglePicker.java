package poomonkeys.common;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

public class AnglePicker extends Drawable
{
	Drawable circle = new Drawable();
	public Drawable line = new Drawable();
	
	static final float LINE_LENGTH = 1.15f; // ratio of radius
	static final float RADIUS = .8f; // ratio of height
	static final int CIRCLE_RES = 50;
	
	private float radius;
	private float lineLength; // the actual line length calculated from radius*LINE_LENGTH
	
	public AnglePicker()
	{
		this.registerDrawable(circle);
		this.registerDrawable(line);
	}
	
	public void buildGeometry(float viewWidth, float viewHeight)
	{
		radius = Math.min(viewHeight/2, AimingHUD.getInstance().divider.x - this.circle.x)*RADIUS;
		lineLength = LINE_LENGTH*radius;
		
		float[] circle_geometry = new float[CIRCLE_RES*3];
		for(int i=0; i < CIRCLE_RES*3; i+=3) {
			int c_i = i/3;
			circle_geometry[i] = ((float) Math.sin(((1.f*c_i)/CIRCLE_RES)*2*Math.PI))*radius;
			circle_geometry[i+1] = (float) Math.cos(((1.f*c_i)/CIRCLE_RES)*2*Math.PI)*radius;
			circle_geometry[i+2] = 0;
		}		
		circle.baseGeometry = circle_geometry;
		circle.drawMode = GL2.GL_LINE_LOOP;

		float[] line_geometry = {0, 0, 0, 0, lineLength, 0};
		line.baseGeometry = line_geometry;
		line.drawMode = GL2.GL_LINES;
	}
	
	
	public void touch(float x, float y)
	{
		line.setRotation(x, y);
	}
}
