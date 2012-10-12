package poomonkeys.common;

import javax.media.opengl.GL2;

public class Tank extends Drawable {
	static final float TURRENT_LENGTH = 1f;
	static final float WIDTH = 4f;
	static final float HEIGHT = 4f;

	float baseGeometry[];

	public Drawable turret = new Drawable();
	float turretLength = 1;

	public Tank() {
		width = WIDTH;
		height = HEIGHT;
		m = 3;
		this.registerDrawable(turret);
	}

	public void buildGeometry(float viewWidth, float viewHeight) {
		float[] baseGeometry = {
				// X, Y
				-width / 2, -height / 2, 0, width / 2, -height / 2f, 0, 0,
				height / 2, 0 };

		this.drawMode = GL2.GL_LINE_LOOP;
		super.vertices = baseGeometry;

		turretLength = TURRENT_LENGTH;
		float turretGeometry[] = { 0, 0, 0, 0, turretLength, 0 };
		turret.drawMode = GL2.GL_LINES;
		turret.vertices = turretGeometry;
		turret.p[0] = 0;
		turret.p[1] = height / 2;
	}

	public void intersectTerrain(Terrain t, float[] intersect) {
		// .5*m*v2 = F*d
		// d = v0t + 0.5at2
		removeFromPhysicsEngine = true;
		this.p[0] += this.v[0] * intersect[2];
		this.p[1] += this.v[1] * intersect[2];
		float velocityVector = (float) Math.sqrt((this.v[0] * this.v[0])
				+ this.v[1] * this.v[1]);
		float force = velocityVector * m;

		if (force > 0) {
			float xDistance = (.5f * m * (this.v[0] * this.v[0]));
			float yDistance = (.5f * m * (this.v[1] * this.v[1]));
			if (this.v[0] < 0) {
				xDistance = -xDistance;
			}
			if (this.v[1] < 0) {
				yDistance = -yDistance;
			}
			this.p[0] += xDistance;
			this.p[1] += yDistance;
			t.explodeRectangle(this.p[0], this.p[1], width / 2);
		}

		this.v[0] = 0;
		this.v[1] = 0;

		// if (t.points[(int) (intersect[0] / t.segmentWidth)] > t.points[(int)
		// (intersect[0] / t.segmentWidth) + 1])
		// {
		// this.p[0] = intersect[0] + t.segmentWidth;
		// this.p[1] = t.points[(int) (intersect[0] / t.segmentWidth) + 1];
		// }
	}

	public void underTerrain() {
		this.v[0] = 0;
		this.v[1] = 0;
	}
}
