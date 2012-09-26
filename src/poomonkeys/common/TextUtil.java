package poomonkeys.common;

public class TextUtil 
{
	public static int generateTextTexture(String text)
	{
		return generateTextTexture(text, 32, 0, 0);
	}
	
	public static int generateTextTexture(String text, float textSize, float x_pad, float y_pad)
	{
		/*// Create an empty, mutable bitmap
		Bitmap bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_4444);
		// get a canvas to paint over the bitmap
		Canvas canvas = new Canvas(bitmap);
		bitmap.eraseColor(Color.TRANSPARENT);

		// Draw the text
		Paint textPaint = new Paint();
		textPaint.setTextSize(textSize);
		textPaint.setAntiAlias(true);
		textPaint.setARGB(0xff, 0x00, 0x00, 0x00);
		//textPaint.setTextAlign(Paint.Align.CENTER);
		// draw the text centered
		canvas.drawText(text, x_pad, 128-y_pad, textPaint);

		//Generate one texture pointer...
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		//...and bind it to our array
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

		//Create Nearest Filtered Texture
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

		//Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

		//Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

		//Clean up
		bitmap.recycle();
		
		return textures[0];*/
		return 0;
	}
}
