package poomonkeys.common;

import javax.media.opengl.GL2;

public class AimingHUD extends Drawable
{
	
	PowerBar powerBar;
	public AnglePicker anglePicker;
	public FireButton startButton;
	Drawable divider;
	
	static final float DIVIDER = 82f;
	static final float ANGLE_PICKER_LEFT_OFFSET = 0f;
	
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
	
	public void buildGeometry(float viewWidth, float viewHeight)
	{	
		anglePicker.circle.p.x = viewWidth/2-ANGLE_PICKER_LEFT_OFFSET;
		anglePicker.line.p.x = viewWidth/2-ANGLE_PICKER_LEFT_OFFSET;
		anglePicker.circle.p.y = viewHeight/2-ANGLE_PICKER_LEFT_OFFSET;
		anglePicker.line.p.y = viewHeight/2-ANGLE_PICKER_LEFT_OFFSET;
		
		float[] line = {
				0, 0, 0,
				0, viewHeight, 0
			};
		divider.baseGeometry = line; 
		divider.p.x = DIVIDER;
		divider.drawMode = GL2.GL_LINES;
	}
	
	public void delete()
	{
		AimingHUD.instance = null;
	}
	
	public void touch(float x, float y, float viewWidth, float viewHeight)
	{
		if(x < startButton.width && y < startButton.height) {
			startButton.depress();
		}
		else if(x < divider.p.x) {
			anglePicker.touch(x, y);
		}
		else {
			powerBar.touch(x, y);
		}
	}
	
	public void click(float x, float y, float viewWidth, float viewHeight)
	{
		if(x < startButton.width && y < startButton.height) {
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
