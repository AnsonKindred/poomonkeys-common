package poomonkeys.common;

import javax.media.opengl.GL2;

public class PowerBar extends Drawable
{
	static final float INNER_PADDING = 1f; 
	static final float LEFT = 86f;
	static final float HEIGHT = .8f; // ratio of screen height
	static final float WIDTH = 10f;
	
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
		width   = WIDTH;
		padding = INNER_PADDING;
		
		baseGeometry[0]=0;     baseGeometry[1]=0;       baseGeometry[2]=0;
		baseGeometry[3]=width; baseGeometry[4]=0;       baseGeometry[5]=0;
		baseGeometry[6]=width; baseGeometry[7]=height;  baseGeometry[8]=0;
		baseGeometry[9]=0;     baseGeometry[10]=height; baseGeometry[11]=0;

		this.p[0] = LEFT;
		this.p[1] = viewHeight/2 - height/2;
		
		super.vertices = baseGeometry;
		super.drawMode = GL2.GL_LINE_LOOP;
		
		inner.height = height - padding;
		inner.width = width - padding;
		
		_compileInnerBar();
		
		inner.p[0] = padding/2.f;
		inner.p[1] = padding/2.f;
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
		
		inner.vertices = innerGeometry;
		inner.drawMode = GL2.GL_TRIANGLE_STRIP;
	}
	
	public void touch(float x, float y)
	{
		// distance from base of power bar
		percentFull = ((y-this.p[1]) / inner.height);
		percentFull = Math.max(Math.min(percentFull, 1), 0);
		_compileInnerBar();
		inner.finalizeGeometry();
	}
}
