package poomonkeys.common;

import javax.media.opengl.GL2;

public class PowerBar extends Drawable
{
	static final float INNER_PADDING = .01f; // ratio of screen height
	static final float LEFT = .355f; // ratio of screen width
	static final float HEIGHT = .8f; // ratio of screen height
	static final float WIDTH = .1f; // ratio of screen width
	
	Drawable inner = new Drawable();
	
	float percentFull;
	float padding;
	
	float[] baseGeometry = new float[12];
	float[] innerGeometry = new float[12];
	
	public PowerBar()
	{
		percentFull = .5f;
		this.registerDrawable(inner);
	}
	
	public void buildGeometry(float viewWidth, float viewHeight)
	{
		height  = viewHeight*HEIGHT;
		width   = viewWidth*WIDTH;
		padding = viewWidth*INNER_PADDING;
		
		baseGeometry[0]=0;     baseGeometry[1]=0;       baseGeometry[2]=0;
		baseGeometry[3]=width; baseGeometry[4]=0;       baseGeometry[5]=0;
		baseGeometry[6]=width; baseGeometry[7]=height;  baseGeometry[8]=0;
		baseGeometry[9]=0;     baseGeometry[10]=height; baseGeometry[11]=0;

		this.x = viewWidth*LEFT;
		this.y = -height/2f;
		
		super.baseGeometry = baseGeometry;
		super.drawMode = GL2.GL_LINE_LOOP;
		
		inner.height = height - padding;
		inner.width = width - padding;
		
		_compileInnerBar();
		
		inner.x = padding/2.f;
		inner.y = padding/2.f;
	}
	
	private void _compileInnerBar()
	{
		innerGeometry[0]=0;	
		innerGeometry[1]=0; 
		innerGeometry[2]=0;
		
		innerGeometry[3]=0; 
		innerGeometry[4]=inner.height*percentFull; 
		innerGeometry[5]=0;
		
		innerGeometry[6]=inner.width; 
		innerGeometry[7]=0; 
		innerGeometry[8]=0;
		
		innerGeometry[9]=inner.width; 
		innerGeometry[10]=inner.height*percentFull; 
		innerGeometry[11]=0;
		
		inner.baseGeometry = innerGeometry;
		inner.drawMode = GL2.GL_TRIANGLE_STRIP;
	}
	
	public void touch(float x, float y)
	{
		// distance from base of power bar
		percentFull = ((y + inner.height/2) / inner.height);
		percentFull = Math.max(Math.min(percentFull, 1), 0);
		_compileInnerBar();
		inner.finalizeGeometry();
	}
}
