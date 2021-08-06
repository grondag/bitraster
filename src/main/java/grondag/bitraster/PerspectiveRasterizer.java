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

import static grondag.bitraster.Constants.BOUNDS_IN;
import static grondag.bitraster.Constants.BOUNDS_OUTSIDE_OR_TOO_SMALL;
import static grondag.bitraster.Constants.EVENT_POSITION_MASK;
import static grondag.bitraster.Constants.HALF_PRECISE_HEIGHT;
import static grondag.bitraster.Constants.HALF_PRECISE_WIDTH;
import static grondag.bitraster.Constants.IDX_AX0;
import static grondag.bitraster.Constants.IDX_AX1;
import static grondag.bitraster.Constants.IDX_AY0;
import static grondag.bitraster.Constants.IDX_AY1;
import static grondag.bitraster.Constants.IDX_BX0;
import static grondag.bitraster.Constants.IDX_BX1;
import static grondag.bitraster.Constants.IDX_BY0;
import static grondag.bitraster.Constants.IDX_BY1;
import static grondag.bitraster.Constants.IDX_CX0;
import static grondag.bitraster.Constants.IDX_CX1;
import static grondag.bitraster.Constants.IDX_CY0;
import static grondag.bitraster.Constants.IDX_CY1;
import static grondag.bitraster.Constants.IDX_DX0;
import static grondag.bitraster.Constants.IDX_DX1;
import static grondag.bitraster.Constants.IDX_DY0;
import static grondag.bitraster.Constants.IDX_DY1;
import static grondag.bitraster.Constants.IDX_MAX_TILE_ORIGIN_X;
import static grondag.bitraster.Constants.IDX_MAX_TILE_ORIGIN_Y;
import static grondag.bitraster.Constants.IDX_MIN_TILE_ORIGIN_X;
import static grondag.bitraster.Constants.IDX_POS0;
import static grondag.bitraster.Constants.IDX_POS1;
import static grondag.bitraster.Constants.IDX_POS2;
import static grondag.bitraster.Constants.IDX_POS3;
import static grondag.bitraster.Constants.IDX_TILE_INDEX;
import static grondag.bitraster.Constants.IDX_TILE_ORIGIN_X;
import static grondag.bitraster.Constants.IDX_TILE_ORIGIN_Y;
import static grondag.bitraster.Constants.IDX_VERTEX_DATA;
import static grondag.bitraster.Constants.PRECISE_HEIGHT;
import static grondag.bitraster.Constants.PRECISE_HEIGHT_CLAMP;
import static grondag.bitraster.Constants.PRECISE_WIDTH;
import static grondag.bitraster.Constants.PRECISE_WIDTH_CLAMP;
import static grondag.bitraster.Constants.PRECISION_BITS;
import static grondag.bitraster.Constants.PV_PX;
import static grondag.bitraster.Constants.PV_PY;
import static grondag.bitraster.Constants.PV_W;
import static grondag.bitraster.Constants.PV_X;
import static grondag.bitraster.Constants.PV_Y;
import static grondag.bitraster.Constants.PV_Z;
import static grondag.bitraster.Constants.SCANT_PRECISE_PIXEL_CENTER;
import static grondag.bitraster.Constants.TILE_AXIS_MASK;
import static grondag.bitraster.Constants.TILE_AXIS_SHIFT;
import static grondag.bitraster.Indexer.tileIndex;

public final class PerspectiveRasterizer extends AbstractRasterizer {
	/** Holds results of {@link #clipNear(int, int)}. */
	protected int clipX, clipY;

	@Override void setupVertex(final int baseIndex, final int x, final int y, final int z) {
		final int[] data = this.data;
		final Matrix4L mvpMatrix = this.mvpMatrix;

		final float tx = mvpMatrix.transformVec4X(x, y, z) * Matrix4L.FLOAT_CONVERSION;
		final float ty = mvpMatrix.transformVec4Y(x, y, z) * Matrix4L.FLOAT_CONVERSION;
		final float w = mvpMatrix.transformVec4W(x, y, z) * Matrix4L.FLOAT_CONVERSION;

		data[baseIndex + PV_X + IDX_VERTEX_DATA] = Float.floatToRawIntBits(tx);
		data[baseIndex + PV_Y + IDX_VERTEX_DATA] = Float.floatToRawIntBits(ty);
		data[baseIndex + PV_Z + IDX_VERTEX_DATA] = Float.floatToRawIntBits(mvpMatrix.transformVec4Z(x, y, z) * Matrix4L.FLOAT_CONVERSION);
		data[baseIndex + PV_W + IDX_VERTEX_DATA] = Float.floatToRawIntBits(w);

		if (w != 0) {
			final float iw = 1f / w;
			final int px = Math.round(tx * iw * HALF_PRECISE_WIDTH) + HALF_PRECISE_WIDTH;
			final int py = Math.round(ty * iw * HALF_PRECISE_HEIGHT) + HALF_PRECISE_HEIGHT;

			data[baseIndex + PV_PX + IDX_VERTEX_DATA] = px;
			data[baseIndex + PV_PY + IDX_VERTEX_DATA] = py;
		}
	}

	private void clipNear(int internal, int external) {
		final int[] data = this.data;

		final float intX = Float.intBitsToFloat(data[internal + PV_X + IDX_VERTEX_DATA]);
		final float intY = Float.intBitsToFloat(data[internal + PV_Y + IDX_VERTEX_DATA]);
		final float intZ = Float.intBitsToFloat(data[internal + PV_Z + IDX_VERTEX_DATA]);
		final float intW = Float.intBitsToFloat(data[internal + PV_W + IDX_VERTEX_DATA]);

		final float extX = Float.intBitsToFloat(data[external + PV_X + IDX_VERTEX_DATA]);
		final float extY = Float.intBitsToFloat(data[external + PV_Y + IDX_VERTEX_DATA]);
		final float extZ = Float.intBitsToFloat(data[external + PV_Z + IDX_VERTEX_DATA]);
		final float extW = Float.intBitsToFloat(data[external + PV_W + IDX_VERTEX_DATA]);

		// intersection point is the projection plane, at which point Z == 1
		// and w will be 0 but projection division isn't needed, so force output to W = 1
		// see https://www.cs.usfca.edu/~cruse/math202s11/homocoords.pdf

		final float wt = intZ / -(extZ - intZ);

		// note again that projection division isn't needed
		final float x = (intX + (extX - intX) * wt);
		final float y = (intY + (extY - intY) * wt);
		final float w = (intW + (extW - intW) * wt);
		final float iw = 1f / w;

		clipX = Math.round(iw * x * HALF_PRECISE_WIDTH) + HALF_PRECISE_WIDTH;
		clipY = Math.round(iw * y * HALF_PRECISE_HEIGHT) + HALF_PRECISE_HEIGHT;
	}

	@Override
	int prepareBounds(int v0, int v1, int v2, int v3) {
		// puts bits in lexical order
		final int split = needsNearClip(v3) | (needsNearClip(v2) << 1) | (needsNearClip(v1) << 2) | (needsNearClip(v0) << 3);

		switch (split) {
			case 0b0000:
				return prepareBounds0000(v0, v1, v2, v3);

			case 0b0001:
				return prepareBounds0001(v0, v1, v2, v3);

			case 0b0010:
				return prepareBounds0001(v3, v0, v1, v2);

			case 0b0100:
				return prepareBounds0001(v2, v3, v0, v1);

			case 0b1000:
				return prepareBounds0001(v1, v2, v3, v0);

			case 0b0011:
				return prepareBounds0011(v0, v1, v2, v3);

			case 0b1001:
				return prepareBounds0011(v1, v2, v3, v0);

			case 0b1100:
				return prepareBounds0011(v2, v3, v0, v1);

			case 0b0110:
				return prepareBounds0011(v3, v0, v1, v2);

			case 0b0111:
				return prepareBounds0111(v0, v1, v2, v3);

			case 0b1011:
				return prepareBounds0111(v1, v2, v3, v0);

			case 0b1101:
				return prepareBounds0111(v2, v3, v0, v1);

			case 0b1110:
				return prepareBounds0111(v3, v0, v1, v2);

			case 0b1111:
				return BOUNDS_OUTSIDE_OR_TOO_SMALL;

			default:
				assert false : "Occlusion edge case";
				// NOOP
		}

		return BOUNDS_OUTSIDE_OR_TOO_SMALL;
	}

	private int prepareBounds0000(int v0, int v1, int v2, int v3) {
		final int[] data = this.data;
		int ax0, ay0, ax1, ay1;
		int bx0, by0, bx1, by1;
		int cx0, cy0, cx1, cy1;
		int dx0, dy0, dx1, dy1;
		int minY = 0, maxY = 0, minX = 0, maxX = 0;

		ax0 = data[v0 + PV_PX + IDX_VERTEX_DATA];
		ay0 = data[v0 + PV_PY + IDX_VERTEX_DATA];
		bx0 = data[v1 + PV_PX + IDX_VERTEX_DATA];
		by0 = data[v1 + PV_PY + IDX_VERTEX_DATA];
		cx0 = data[v2 + PV_PX + IDX_VERTEX_DATA];
		cy0 = data[v2 + PV_PY + IDX_VERTEX_DATA];
		dx0 = data[v3 + PV_PX + IDX_VERTEX_DATA];
		dy0 = data[v3 + PV_PY + IDX_VERTEX_DATA];

		ax1 = bx0;
		ay1 = by0;
		bx1 = cx0;
		by1 = cy0;
		cx1 = dx0;
		cy1 = dy0;
		dx1 = ax0;
		dy1 = ay0;

		minX = ax0;
		maxX = ax0;

		if (bx0 < minX) {
			minX = bx0;
		} else if (bx0 > maxX) {
			maxX = bx0;
		}

		if (cx0 < minX) {
			minX = cx0;
		} else if (cx0 > maxX) {
			maxX = cx0;
		}

		if (dx0 < minX) {
			minX = dx0;
		} else if (dx0 > maxX) {
			maxX = dx0;
		}

		minY = ay0;
		maxY = ay0;

		if (by0 < minY) {
			minY = by0;
		} else if (by0 > maxY) {
			maxY = by0;
		}

		if (cy0 < minY) {
			minY = cy0;
		} else if (cy0 > maxY) {
			maxY = cy0;
		}

		if (dy0 < minY) {
			minY = dy0;
		} else if (dy0 > maxY) {
			maxY = dy0;
		}

		if (((maxY - 1) | (maxX - 1) | (PRECISE_HEIGHT - 1 - minY) | (PRECISE_WIDTH - 1 - minX)) < 0) {
			// NOOP?  TODO: why is this here?
		}

		if (maxY <= 0 || minY >= PRECISE_HEIGHT) {
			return BOUNDS_OUTSIDE_OR_TOO_SMALL;
		}

		if (maxX <= 0 || minX >= PRECISE_WIDTH) {
			return BOUNDS_OUTSIDE_OR_TOO_SMALL;
		}

		if (minX < 0) {
			minX = 0;
		}

		if (maxX >= PRECISE_WIDTH_CLAMP) {
			maxX = PRECISE_WIDTH_CLAMP;

			if (minX > PRECISE_WIDTH_CLAMP) {
				minX = PRECISE_WIDTH_CLAMP;
			}
		}

		if (minY < 0) {
			minY = 0;
		}

		if (maxY >= PRECISE_HEIGHT_CLAMP) {
			maxY = PRECISE_HEIGHT_CLAMP;

			if (minY > PRECISE_HEIGHT_CLAMP) {
				minY = PRECISE_HEIGHT_CLAMP;
			}
		}

		final int minPixelX = ((minX + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS);
		final int minPixelY = ((minY + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS);
		final int maxPixelX = ((maxX + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS);
		final int maxPixelY = ((maxY + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS);

		data[IDX_MIN_TILE_ORIGIN_X] = minPixelX & TILE_AXIS_MASK;
		data[IDX_MAX_TILE_ORIGIN_X] = maxPixelX & TILE_AXIS_MASK;
		data[IDX_MAX_TILE_ORIGIN_Y] = maxPixelY & TILE_AXIS_MASK;

		data[IDX_TILE_ORIGIN_X] = minPixelX & TILE_AXIS_MASK;
		data[IDX_TILE_ORIGIN_Y] = minPixelY & TILE_AXIS_MASK;
		data[IDX_TILE_INDEX] = tileIndex(minPixelX >> TILE_AXIS_SHIFT, minPixelY >> TILE_AXIS_SHIFT);

		final int position0 = edgePosition(ax0, ay0, ax1, ay1);
		final int position1 = edgePosition(bx0, by0, bx1, by1);
		final int position2 = edgePosition(cx0, cy0, cx1, cy1);
		final int position3 = edgePosition(dx0, dy0, dx1, dy1);

		this.minPixelX = minPixelX;
		this.minPixelY = minPixelY;
		this.maxPixelX = maxPixelX;
		this.maxPixelY = maxPixelY;
		data[IDX_AX0] = ax0;
		data[IDX_AY0] = ay0;
		data[IDX_AX1] = ax1;
		data[IDX_AY1] = ay1;
		data[IDX_BX0] = bx0;
		data[IDX_BY0] = by0;
		data[IDX_BX1] = bx1;
		data[IDX_BY1] = by1;
		data[IDX_CX0] = cx0;
		data[IDX_CY0] = cy0;
		data[IDX_CX1] = cx1;
		data[IDX_CY1] = cy1;
		data[IDX_DX0] = dx0;
		data[IDX_DY0] = dy0;
		data[IDX_DX1] = dx1;
		data[IDX_DY1] = dy1;
		data[IDX_POS0] = position0;
		data[IDX_POS1] = position1;
		data[IDX_POS2] = position2;
		data[IDX_POS3] = position3;

		final int eventKey = (position0 - 1) & EVENT_POSITION_MASK
				| (((position1 - 1) & EVENT_POSITION_MASK) << 2)
				| (((position2 - 1) & EVENT_POSITION_MASK) << 4)
				| (((position3 - 1) & EVENT_POSITION_MASK) << 6);

		EVENT_FILLERS[eventKey].apply();

		return BOUNDS_IN;
	}

	private int prepareBounds0001(int v0, int v1, int v2, int ext3) {
		final int[] data = this.data;
		int ax0, ay0, ax1, ay1;
		int bx0, by0, bx1, by1;
		int cx0, cy0, cx1, cy1;
		int dx0, dy0, dx1, dy1;
		int minY = 0, maxY = 0, minX = 0, maxX = 0;

		ax0 = data[v0 + PV_PX + IDX_VERTEX_DATA];
		ay0 = data[v0 + PV_PY + IDX_VERTEX_DATA];
		ax1 = data[v1 + PV_PX + IDX_VERTEX_DATA];
		ay1 = data[v1 + PV_PY + IDX_VERTEX_DATA];

		bx0 = ax1;
		by0 = ay1;
		bx1 = data[v2 + PV_PX + IDX_VERTEX_DATA];
		by1 = data[v2 + PV_PY + IDX_VERTEX_DATA];

		cx0 = bx1;
		cy0 = by1;
		clipNear(v2, ext3);
		cx1 = clipX;
		cy1 = clipY;

		clipNear(v0, ext3);
		dx0 = clipX;
		dy0 = clipY;
		dx1 = ax0;
		dy1 = ay0;

		minX = ax0;
		maxX = ax0;

		// ax1 = bx0 and dx1 = ax0,  so no need to test those
		if (bx0 < minX) {
			minX = bx0;
		} else if (bx0 > maxX) {
			maxX = bx0;
		}

		if (cx0 < minX) {
			minX = cx0;
		} else if (cx0 > maxX) {
			maxX = cx0;
		}

		if (cx1 < minX) {
			minX = cx1;
		} else if (cx1 > maxX) {
			maxX = cx1;
		}

		if (dx0 < minX) {
			minX = dx0;
		} else if (dx0 > maxX) {
			maxX = dx0;
		}

		minY = ay0;
		maxY = ay0;

		if (by0 < minY) {
			minY = by0;
		} else if (by0 > maxY) {
			maxY = by0;
		}

		if (cy0 < minY) {
			minY = cy0;
		} else if (cy0 > maxY) {
			maxY = cy0;
		}

		if (cy1 < minY) {
			minY = cy1;
		} else if (cy1 > maxY) {
			maxY = cy1;
		}

		if (dy0 < minY) {
			minY = dy0;
		} else if (dy0 > maxY) {
			maxY = dy0;
		}

		if (maxY <= 0 || minY >= PRECISE_HEIGHT) {
			return BOUNDS_OUTSIDE_OR_TOO_SMALL;
		}

		if (maxX <= 0 || minX >= PRECISE_WIDTH) {
			return BOUNDS_OUTSIDE_OR_TOO_SMALL;
		}

		if (minX < 0) {
			minX = 0;
		}

		if (maxX >= PRECISE_WIDTH_CLAMP) {
			maxX = PRECISE_WIDTH_CLAMP;

			if (minX > PRECISE_WIDTH_CLAMP) {
				minX = PRECISE_WIDTH_CLAMP;
			}
		}

		if (minY < 0) {
			minY = 0;
		}

		if (maxY >= PRECISE_HEIGHT_CLAMP) {
			maxY = PRECISE_HEIGHT_CLAMP;

			if (minY > PRECISE_HEIGHT_CLAMP) {
				minY = PRECISE_HEIGHT_CLAMP;
			}
		}

		final int minPixelX = ((minX + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS);
		final int minPixelY = ((minY + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS);
		final int maxPixelX = ((maxX + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS);
		final int maxPixelY = ((maxY + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS);

		data[IDX_MIN_TILE_ORIGIN_X] = minPixelX & TILE_AXIS_MASK;
		data[IDX_MAX_TILE_ORIGIN_X] = maxPixelX & TILE_AXIS_MASK;
		data[IDX_MAX_TILE_ORIGIN_Y] = maxPixelY & TILE_AXIS_MASK;

		data[IDX_TILE_ORIGIN_X] = minPixelX & TILE_AXIS_MASK;
		data[IDX_TILE_ORIGIN_Y] = minPixelY & TILE_AXIS_MASK;
		data[IDX_TILE_INDEX] = tileIndex(minPixelX >> TILE_AXIS_SHIFT, minPixelY >> TILE_AXIS_SHIFT);

		final int position0 = edgePosition(ax0, ay0, ax1, ay1);
		final int position1 = edgePosition(bx0, by0, bx1, by1);
		final int position2 = edgePosition(cx0, cy0, cx1, cy1);
		final int position3 = edgePosition(dx0, dy0, dx1, dy1);

		this.minPixelX = minPixelX;
		this.minPixelY = minPixelY;
		this.maxPixelX = maxPixelX;
		this.maxPixelY = maxPixelY;
		data[IDX_AX0] = ax0;
		data[IDX_AY0] = ay0;
		data[IDX_AX1] = ax1;
		data[IDX_AY1] = ay1;
		data[IDX_BX0] = bx0;
		data[IDX_BY0] = by0;
		data[IDX_BX1] = bx1;
		data[IDX_BY1] = by1;
		data[IDX_CX0] = cx0;
		data[IDX_CY0] = cy0;
		data[IDX_CX1] = cx1;
		data[IDX_CY1] = cy1;
		data[IDX_DX0] = dx0;
		data[IDX_DY0] = dy0;
		data[IDX_DX1] = dx1;
		data[IDX_DY1] = dy1;
		data[IDX_POS0] = position0;
		data[IDX_POS1] = position1;
		data[IDX_POS2] = position2;
		data[IDX_POS3] = position3;

		final int eventKey = (position0 - 1) & EVENT_POSITION_MASK
				| (((position1 - 1) & EVENT_POSITION_MASK) << 2)
				| (((position2 - 1) & EVENT_POSITION_MASK) << 4)
				| (((position3 - 1) & EVENT_POSITION_MASK) << 6);

		EVENT_FILLERS[eventKey].apply();

		return BOUNDS_IN;
	}

	private int prepareBounds0011(int v0, int v1, int ext2, int ext3) {
		final int[] data = this.data;
		int ax0, ay0, ax1, ay1;
		int bx0, by0, bx1, by1;
		int cx0, cy0, cx1, cy1;
		int dx0, dy0, dx1, dy1;

		int minY = 0, maxY = 0, minX = 0, maxX = 0;

		ax0 = data[v0 + PV_PX + IDX_VERTEX_DATA];
		ay0 = data[v0 + PV_PY + IDX_VERTEX_DATA];
		ax1 = data[v1 + PV_PX + IDX_VERTEX_DATA];
		ay1 = data[v1 + PV_PY + IDX_VERTEX_DATA];

		bx0 = ax1;
		by0 = ay1;
		clipNear(v1, ext2);
		bx1 = clipX;
		by1 = clipY;

		// force line c to be a single, existing point - entire line is clipped and should not influence anything
		cx0 = ax0;
		cy0 = ay0;
		cx1 = ax0;
		cy1 = ay0;

		clipNear(v0, ext3);
		dx0 = clipX;
		dy0 = clipY;
		dx1 = ax0;
		dy1 = ay0;

		minX = ax0;
		maxX = ax0;

		if (bx0 < minX) {
			minX = bx0;
		} else if (bx0 > maxX) {
			maxX = bx0;
		}

		if (bx1 < minX) {
			minX = bx1;
		} else if (bx1 > maxX) {
			maxX = bx1;
		}

		if (dx0 < minX) {
			minX = dx0;
		} else if (dx0 > maxX) {
			maxX = dx0;
		}

		minY = ay0;
		maxY = ay0;

		if (by0 < minY) {
			minY = by0;
		} else if (by0 > maxY) {
			maxY = by0;
		}

		if (by1 < minY) {
			minY = by1;
		} else if (by1 > maxY) {
			maxY = by1;
		}

		if (dy0 < minY) {
			minY = dy0;
		} else if (dy0 > maxY) {
			maxY = dy0;
		}

		if (maxY <= 0 || minY >= PRECISE_HEIGHT) {
			return BOUNDS_OUTSIDE_OR_TOO_SMALL;
		}

		if (maxX <= 0 || minX >= PRECISE_WIDTH) {
			return BOUNDS_OUTSIDE_OR_TOO_SMALL;
		}

		if (minX < 0) {
			minX = 0;
		}

		if (maxX >= PRECISE_WIDTH_CLAMP) {
			maxX = PRECISE_WIDTH_CLAMP;

			if (minX > PRECISE_WIDTH_CLAMP) {
				minX = PRECISE_WIDTH_CLAMP;
			}
		}

		if (minY < 0) {
			minY = 0;
		}

		if (maxY >= PRECISE_HEIGHT_CLAMP) {
			maxY = PRECISE_HEIGHT_CLAMP;

			if (minY > PRECISE_HEIGHT_CLAMP) {
				minY = PRECISE_HEIGHT_CLAMP;
			}
		}

		final int minPixelX = ((minX + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS);
		final int minPixelY = ((minY + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS);
		final int maxPixelX = ((maxX + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS);
		final int maxPixelY = ((maxY + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS);

		data[IDX_MIN_TILE_ORIGIN_X] = minPixelX & TILE_AXIS_MASK;
		data[IDX_MAX_TILE_ORIGIN_X] = maxPixelX & TILE_AXIS_MASK;
		data[IDX_MAX_TILE_ORIGIN_Y] = maxPixelY & TILE_AXIS_MASK;

		data[IDX_TILE_ORIGIN_X] = minPixelX & TILE_AXIS_MASK;
		data[IDX_TILE_ORIGIN_Y] = minPixelY & TILE_AXIS_MASK;
		data[IDX_TILE_INDEX] = tileIndex(minPixelX >> TILE_AXIS_SHIFT, minPixelY >> TILE_AXIS_SHIFT);

		final int position0 = edgePosition(ax0, ay0, ax1, ay1);
		final int position1 = edgePosition(bx0, by0, bx1, by1);
		final int position2 = edgePosition(cx0, cy0, cx1, cy1);
		final int position3 = edgePosition(dx0, dy0, dx1, dy1);

		this.minPixelX = minPixelX;
		this.minPixelY = minPixelY;
		this.maxPixelX = maxPixelX;
		this.maxPixelY = maxPixelY;
		data[IDX_AX0] = ax0;
		data[IDX_AY0] = ay0;
		data[IDX_AX1] = ax1;
		data[IDX_AY1] = ay1;
		data[IDX_BX0] = bx0;
		data[IDX_BY0] = by0;
		data[IDX_BX1] = bx1;
		data[IDX_BY1] = by1;
		data[IDX_CX0] = cx0;
		data[IDX_CY0] = cy0;
		data[IDX_CX1] = cx1;
		data[IDX_CY1] = cy1;
		data[IDX_DX0] = dx0;
		data[IDX_DY0] = dy0;
		data[IDX_DX1] = dx1;
		data[IDX_DY1] = dy1;
		data[IDX_POS0] = position0;
		data[IDX_POS1] = position1;
		data[IDX_POS2] = position2;
		data[IDX_POS3] = position3;

		final int eventKey = (position0 - 1) & EVENT_POSITION_MASK
				| (((position1 - 1) & EVENT_POSITION_MASK) << 2)
				| (((position2 - 1) & EVENT_POSITION_MASK) << 4)
				| (((position3 - 1) & EVENT_POSITION_MASK) << 6);

		EVENT_FILLERS[eventKey].apply();

		return BOUNDS_IN;
	}

	private int prepareBounds0111(int v0, int ext1, int ext2, int ext3) {
		final int[] data = this.data;
		int ax0, ay0, ax1, ay1;
		int bx0, by0, bx1, by1;
		int cx0, cy0, cx1, cy1;
		int dx0, dy0, dx1, dy1;
		int minY = 0, maxY = 0, minX = 0, maxX = 0;

		ax0 = data[v0 + PV_PX + IDX_VERTEX_DATA];
		ay0 = data[v0 + PV_PY + IDX_VERTEX_DATA];
		clipNear(v0, ext1);
		ax1 = clipX;
		ay1 = clipY;

		// force lines b & c to be a single, existing point - entire line is clipped and should not influence anything
		bx0 = ax0;
		by0 = ay0;
		bx1 = ax0;
		by1 = ay0;
		cx0 = ax0;
		cy0 = ay0;
		cx1 = ax0;
		cy1 = ay0;

		clipNear(v0, ext3);
		dx0 = clipX;
		dy0 = clipY;
		dx1 = ax0;
		dy1 = ay0;

		minX = ax0;
		maxX = ax0;

		if (ax1 < minX) {
			minX = ax1;
		} else if (ax1 > maxX) {
			maxX = ax1;
		}

		if (dx0 < minX) {
			minX = dx0;
		} else if (dx0 > maxX) {
			maxX = dx0;
		}

		minY = ay0;
		maxY = ay0;

		if (ay1 < minY) {
			minY = ay1;
		} else if (ay1 > maxY) {
			maxY = ay1;
		}

		if (dy0 < minY) {
			minY = dy0;
		} else if (dy0 > maxY) {
			maxY = dy0;
		}

		if (maxY <= 0 || minY >= PRECISE_HEIGHT) {
			return BOUNDS_OUTSIDE_OR_TOO_SMALL;
		}

		if (maxX <= 0 || minX >= PRECISE_WIDTH) {
			return BOUNDS_OUTSIDE_OR_TOO_SMALL;
		}

		if (minX < 0) {
			minX = 0;
		}

		if (maxX >= PRECISE_WIDTH_CLAMP) {
			maxX = PRECISE_WIDTH_CLAMP;

			if (minX > PRECISE_WIDTH_CLAMP) {
				minX = PRECISE_WIDTH_CLAMP;
			}
		}

		if (minY < 0) {
			minY = 0;
		}

		if (maxY >= PRECISE_HEIGHT_CLAMP) {
			maxY = PRECISE_HEIGHT_CLAMP;

			if (minY > PRECISE_HEIGHT_CLAMP) {
				minY = PRECISE_HEIGHT_CLAMP;
			}
		}

		final int minPixelX = ((minX + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS);
		final int minPixelY = ((minY + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS);
		final int maxPixelX = ((maxX + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS);
		final int maxPixelY = ((maxY + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS);

		data[IDX_MIN_TILE_ORIGIN_X] = minPixelX & TILE_AXIS_MASK;
		data[IDX_MAX_TILE_ORIGIN_X] = maxPixelX & TILE_AXIS_MASK;
		data[IDX_MAX_TILE_ORIGIN_Y] = maxPixelY & TILE_AXIS_MASK;

		data[IDX_TILE_ORIGIN_X] = minPixelX & TILE_AXIS_MASK;
		data[IDX_TILE_ORIGIN_Y] = minPixelY & TILE_AXIS_MASK;
		data[IDX_TILE_INDEX] = tileIndex(minPixelX >> TILE_AXIS_SHIFT, minPixelY >> TILE_AXIS_SHIFT);

		final int position0 = edgePosition(ax0, ay0, ax1, ay1);
		final int position1 = edgePosition(bx0, by0, bx1, by1);
		final int position2 = edgePosition(cx0, cy0, cx1, cy1);
		final int position3 = edgePosition(dx0, dy0, dx1, dy1);

		this.minPixelX = minPixelX;
		this.minPixelY = minPixelY;
		this.maxPixelX = maxPixelX;
		this.maxPixelY = maxPixelY;
		data[IDX_AX0] = ax0;
		data[IDX_AY0] = ay0;
		data[IDX_AX1] = ax1;
		data[IDX_AY1] = ay1;
		data[IDX_BX0] = bx0;
		data[IDX_BY0] = by0;
		data[IDX_BX1] = bx1;
		data[IDX_BY1] = by1;
		data[IDX_CX0] = cx0;
		data[IDX_CY0] = cy0;
		data[IDX_CX1] = cx1;
		data[IDX_CY1] = cy1;
		data[IDX_DX0] = dx0;
		data[IDX_DY0] = dy0;
		data[IDX_DX1] = dx1;
		data[IDX_DY1] = dy1;
		data[IDX_POS0] = position0;
		data[IDX_POS1] = position1;
		data[IDX_POS2] = position2;
		data[IDX_POS3] = position3;

		final int eventKey = (position0 - 1) & EVENT_POSITION_MASK
				| (((position1 - 1) & EVENT_POSITION_MASK) << 2)
				| (((position2 - 1) & EVENT_POSITION_MASK) << 4)
				| (((position3 - 1) & EVENT_POSITION_MASK) << 6);

		EVENT_FILLERS[eventKey].apply();

		return BOUNDS_IN;
	}
}
