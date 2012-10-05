package poomonkeys.common;

import javax.media.opengl.GL2;

public class Dirt extends Drawable {
	public float volume;
	float rVolume;
	int TRAILLENGTH = 15;

	public void buildGeometry(float viewWidth, float viewHeight) {
		baseGeometry = new float[4 * 3];
		baseGeometry[0] = -width / 2;
		baseGeometry[1] = -height / 2;
		baseGeometry[2] = 0;
		baseGeometry[3] = -width / 2;
		baseGeometry[4] = height / 2;
		baseGeometry[5] = 0;
		baseGeometry[6] = width / 2;
		baseGeometry[7] = height / 2;
		baseGeometry[8] = 0;
		baseGeometry[9] = width / 2;
		baseGeometry[10] = -height / 2;
		baseGeometry[11] = 0;
		drawMode = GL2.GL_LINE_LOOP;
	}

	public void intersectTerrain(Terrain t, float x, float y) {
		this.x = x;
		this.y = y;
		rVolume = volume;
		this.removeFromGLEngine = true;
		this.removeFromPhysicsEngine = true;

		int index = Math.round((x) / t.segmentWidth);

		index = Math.max(0, index);
		index = Math.min(t.points.length - 1, index);

		for (int j = index; j <= index; j++) {
			addDirt(t, index);
		}
	}

	public void addDirt(Terrain t, int index) {
		if (index < 1 || index > t.NUM_POINTS - 2) {
			return;
		}
		if (t.points[index - 1] + t.offsets[index - 1] < t.points[index]
				+ t.offsets[index]
				&& t.points[index - 1] + t.offsets[index - 1] < t.points[index + 1]
						+ t.offsets[index + 1]) {
			t.offsets[index - 1] += rVolume / TRAILLENGTH;
			volume -= rVolume / TRAILLENGTH;
			if (volume < 0) {
				return;
			}
			addDirt(t, index - 1);
			return;
		}
		if (t.points[index + 1] + t.offsets[index + 1] < t.points[index]
				+ t.offsets[index]
				&& t.points[index + 1] + t.offsets[index + 1] < t.points[index - 1]
						+ t.offsets[index - 1]) {
			t.offsets[index + 1] += rVolume / TRAILLENGTH;
			volume -= rVolume / TRAILLENGTH;
			if (volume < 0) {
				return;
			}
			addDirt(t, index + 1);
			return;
		}
		if (t.points[index] + t.offsets[index] <= t.points[index + 1]
				+ t.offsets[index + 1]
				&& t.points[index] + t.offsets[index] <= t.points[index - 1]
						+ t.offsets[index - 1]) {
			t.offsets[index] += volume / TRAILLENGTH;
			volume -= rVolume / TRAILLENGTH;
			if (volume < 0) {
				return;
			}
			addDirt(t, index);
			return;
		}
	}
}
