package poomonkeys.common;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;

public class FireButton extends Drawable 
{
	static final float WIDTH = 15f;
	static final float HEIGHT = 8f;
	
	//private Drawable text = new Drawable();
	
	public void buildGeometry(float viewWidth, float viewHeight)
	{
		baseGeometry = new float[12];
		baseGeometry[0]=0; baseGeometry[1]=0; baseGeometry[2]=0;
		baseGeometry[3]=0; baseGeometry[4]=HEIGHT; baseGeometry[5]=0;
		baseGeometry[6]=WIDTH; baseGeometry[7]=0; baseGeometry[8]=0;
		baseGeometry[9]=WIDTH; baseGeometry[10]=HEIGHT; baseGeometry[11]=0;
		this.drawMode = GL2.GL_TRIANGLE_STRIP;
		this.x = 0;
		this.y = 0;
		this.width = WIDTH;
		this.height = HEIGHT;
		
		/*text.baseGeometry = this.baseGeometry;
		text.drawMode = GL2.GL_TRIANGLE_STRIP;
		text.texture_id = TextUtil.generateTextTexture("Fire", 42, 29, 19);
		text.width = this.width = WIDTH*viewWidth;
		text.height = this.height = HEIGHT*viewHeight;
		text.x = this.x = -viewWidth/2;
		text.y = this.y = -viewHeight/2;*/
	}

	public void click() 
	{
		this.fireGLClick(new GLClickEvent(this));
	}
	
	public void depress()
	{
		
	}
}
