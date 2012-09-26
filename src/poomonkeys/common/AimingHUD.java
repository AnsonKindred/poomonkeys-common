package poomonkeys.common;

import javax.media.opengl.GL2;

public class AimingHUD extends Drawable
{
	
	PowerBar powerBar;
	public AnglePicker anglePicker;
	public FireButton startButton;
	Drawable divider;
	
	static final float DIVIDER = .52f; // ratio of width
	static final float ANGLE_PICKER_LEFT_OFFSET = .03f; // ratio of width
	
	static AimingHUD instance = null;
	
	// apparently mutable statics are passe...so singleton it is
	public static AimingHUD getInstance()
	{
		if(instance == null) instance = new AimingHUD();
		return instance;
	}
	
	public AimingHUD()
	{
		anglePicker = new AnglePicker();
		powerBar    = new PowerBar();
		divider     = new Drawable();
		startButton = new FireButton();
		
		this.registerDrawable(anglePicker);
		this.registerDrawable(powerBar);
		this.registerDrawable(startButton);
		this.registerDrawable(divider);
	}
	
	public void buildGeometry(float viewHeight, float viewWidth)
	{	
		anglePicker.circle.x = -ANGLE_PICKER_LEFT_OFFSET*viewWidth;
		anglePicker.line.x = -ANGLE_PICKER_LEFT_OFFSET*viewWidth;
		
		float[] line = {
				0, -viewHeight/2, 0,
				0, viewHeight/2, 0
			};
		divider.baseGeometry = line; 
		divider.x = DIVIDER*viewWidth;
		divider.drawMode = GL2.GL_LINES;
	}
	
	public void delete()
	{
		AimingHUD.instance = null;
	}
	
	public void touch(float x, float y, float viewWidth, float viewHeight)
	{
		if(x < -viewWidth/2+startButton.width && y < -viewHeight/2+startButton.height) {
			startButton.depress();
		}
		else if(x < divider.x) {
			anglePicker.touch(x, y);
		}
		else {
			powerBar.touch(x, y);
		}
	}
	
	public void click(float x, float y, float viewWidth, float viewHeight)
	{
		if(x < -viewWidth/2+startButton.width && y < -viewHeight/2+startButton.height) {
			startButton.click();
		}
	}

	public float getAngle() 
	{
		return anglePicker.line.getRotation();
	}

	public float getPower() 
	{
		return powerBar.percentFull;
	}
	
}
