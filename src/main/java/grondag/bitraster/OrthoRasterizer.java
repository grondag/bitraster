/*
 *  Copyright 2019, 2020 grondag
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.  You may obtain a copy
 *  of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package grondag.bitraster;

import static grondag.bitraster.Constants.HALF_PRECISE_HEIGHT;
import static grondag.bitraster.Constants.HALF_PRECISE_WIDTH;
import static grondag.bitraster.Constants.PV_PX;
import static grondag.bitraster.Constants.PV_PY;
import static grondag.bitraster.Constants.PV_X;
import static grondag.bitraster.Constants.PV_Y;

public final class OrthoRasterizer extends AbstractRasterizer {
	@Override
	int prepareBounds(int v0, int v1, int v2, int v3) {
		return prepareBoundsNoClip(v0, v1, v2, v3);
	}

	@Override void setupVertex(final int baseIndex, final int x, final int y, final int z) {
		final int[] data = vertexData;
		final Matrix4L mvpMatrix = this.mvpMatrix;

		final float tx = mvpMatrix.transformVec4X(x, y, z) * Matrix4L.FLOAT_CONVERSION;
		final float ty = mvpMatrix.transformVec4Y(x, y, z) * Matrix4L.FLOAT_CONVERSION;

		data[baseIndex + PV_X] = Float.floatToRawIntBits(tx);
		data[baseIndex + PV_Y] = Float.floatToRawIntBits(ty);

		final int px = Math.round(tx * HALF_PRECISE_WIDTH) + HALF_PRECISE_WIDTH;
		final int py = Math.round(ty* HALF_PRECISE_HEIGHT) + HALF_PRECISE_HEIGHT;

		data[baseIndex + PV_PX] = px;
		data[baseIndex + PV_PY] = py;
	}
}
