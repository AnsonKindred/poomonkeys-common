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
		vertices = new float[12];
		vertices[0]=0; vertices[1]=0; vertices[2]=0;
		vertices[3]=0; vertices[4]=HEIGHT; vertices[5]=0;
		vertices[6]=WIDTH; vertices[7]=0; vertices[8]=0;
		vertices[9]=WIDTH; vertices[10]=HEIGHT; vertices[11]=0;
		this.drawMode = GL2.GL_TRIANGLE_STRIP;
		this.p[0] = 0;
		this.p[1] = 0;
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
