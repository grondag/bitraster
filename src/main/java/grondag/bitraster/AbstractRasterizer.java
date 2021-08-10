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
import static grondag.bitraster.Constants.EDGE_BOTTOM;
import static grondag.bitraster.Constants.EDGE_POINT;
import static grondag.bitraster.Constants.EDGE_TOP;
import static grondag.bitraster.Constants.EVENT_0123_FFFF;
import static grondag.bitraster.Constants.EVENT_0123_FFFL;
import static grondag.bitraster.Constants.EVENT_0123_FFFR;
import static grondag.bitraster.Constants.EVENT_0123_FFLF;
import static grondag.bitraster.Constants.EVENT_0123_FFLL;
import static grondag.bitraster.Constants.EVENT_0123_FFLR;
import static grondag.bitraster.Constants.EVENT_0123_FFRF;
import static grondag.bitraster.Constants.EVENT_0123_FFRL;
import static grondag.bitraster.Constants.EVENT_0123_FFRR;
import static grondag.bitraster.Constants.EVENT_0123_FLFF;
import static grondag.bitraster.Constants.EVENT_0123_FLFL;
import static grondag.bitraster.Constants.EVENT_0123_FLFR;
import static grondag.bitraster.Constants.EVENT_0123_FLLF;
import static grondag.bitraster.Constants.EVENT_0123_FLLL;
import static grondag.bitraster.Constants.EVENT_0123_FLLR;
import static grondag.bitraster.Constants.EVENT_0123_FLRF;
import static grondag.bitraster.Constants.EVENT_0123_FLRL;
import static grondag.bitraster.Constants.EVENT_0123_FLRR;
import static grondag.bitraster.Constants.EVENT_0123_FRFF;
import static grondag.bitraster.Constants.EVENT_0123_FRFL;
import static grondag.bitraster.Constants.EVENT_0123_FRFR;
import static grondag.bitraster.Constants.EVENT_0123_FRLF;
import static grondag.bitraster.Constants.EVENT_0123_FRLL;
import static grondag.bitraster.Constants.EVENT_0123_FRLR;
import static grondag.bitraster.Constants.EVENT_0123_FRRF;
import static grondag.bitraster.Constants.EVENT_0123_FRRL;
import static grondag.bitraster.Constants.EVENT_0123_FRRR;
import static grondag.bitraster.Constants.EVENT_0123_LFFF;
import static grondag.bitraster.Constants.EVENT_0123_LFFL;
import static grondag.bitraster.Constants.EVENT_0123_LFFR;
import static grondag.bitraster.Constants.EVENT_0123_LFLF;
import static grondag.bitraster.Constants.EVENT_0123_LFLL;
import static grondag.bitraster.Constants.EVENT_0123_LFLR;
import static grondag.bitraster.Constants.EVENT_0123_LFRF;
import static grondag.bitraster.Constants.EVENT_0123_LFRL;
import static grondag.bitraster.Constants.EVENT_0123_LFRR;
import static grondag.bitraster.Constants.EVENT_0123_LLFF;
import static grondag.bitraster.Constants.EVENT_0123_LLFL;
import static grondag.bitraster.Constants.EVENT_0123_LLFR;
import static grondag.bitraster.Constants.EVENT_0123_LLLF;
import static grondag.bitraster.Constants.EVENT_0123_LLLL;
import static grondag.bitraster.Constants.EVENT_0123_LLLR;
import static grondag.bitraster.Constants.EVENT_0123_LLRF;
import static grondag.bitraster.Constants.EVENT_0123_LLRL;
import static grondag.bitraster.Constants.EVENT_0123_LLRR;
import static grondag.bitraster.Constants.EVENT_0123_LRFF;
import static grondag.bitraster.Constants.EVENT_0123_LRFL;
import static grondag.bitraster.Constants.EVENT_0123_LRFR;
import static grondag.bitraster.Constants.EVENT_0123_LRLF;
import static grondag.bitraster.Constants.EVENT_0123_LRLL;
import static grondag.bitraster.Constants.EVENT_0123_LRLR;
import static grondag.bitraster.Constants.EVENT_0123_LRRF;
import static grondag.bitraster.Constants.EVENT_0123_LRRL;
import static grondag.bitraster.Constants.EVENT_0123_LRRR;
import static grondag.bitraster.Constants.EVENT_0123_RFFF;
import static grondag.bitraster.Constants.EVENT_0123_RFFL;
import static grondag.bitraster.Constants.EVENT_0123_RFFR;
import static grondag.bitraster.Constants.EVENT_0123_RFLF;
import static grondag.bitraster.Constants.EVENT_0123_RFLL;
import static grondag.bitraster.Constants.EVENT_0123_RFLR;
import static grondag.bitraster.Constants.EVENT_0123_RFRF;
import static grondag.bitraster.Constants.EVENT_0123_RFRL;
import static grondag.bitraster.Constants.EVENT_0123_RFRR;
import static grondag.bitraster.Constants.EVENT_0123_RLFF;
import static grondag.bitraster.Constants.EVENT_0123_RLFL;
import static grondag.bitraster.Constants.EVENT_0123_RLFR;
import static grondag.bitraster.Constants.EVENT_0123_RLLF;
import static grondag.bitraster.Constants.EVENT_0123_RLLL;
import static grondag.bitraster.Constants.EVENT_0123_RLLR;
import static grondag.bitraster.Constants.EVENT_0123_RLRF;
import static grondag.bitraster.Constants.EVENT_0123_RLRL;
import static grondag.bitraster.Constants.EVENT_0123_RLRR;
import static grondag.bitraster.Constants.EVENT_0123_RRFF;
import static grondag.bitraster.Constants.EVENT_0123_RRFL;
import static grondag.bitraster.Constants.EVENT_0123_RRFR;
import static grondag.bitraster.Constants.EVENT_0123_RRLF;
import static grondag.bitraster.Constants.EVENT_0123_RRLL;
import static grondag.bitraster.Constants.EVENT_0123_RRLR;
import static grondag.bitraster.Constants.EVENT_0123_RRRF;
import static grondag.bitraster.Constants.EVENT_0123_RRRL;
import static grondag.bitraster.Constants.EVENT_0123_RRRR;
import static grondag.bitraster.Constants.EVENT_DATA_LENGTH;
import static grondag.bitraster.Constants.EVENT_POSITION_MASK;
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
import static grondag.bitraster.Constants.MAX_PIXEL_Y;
import static grondag.bitraster.Constants.PIXEL_HEIGHT;
import static grondag.bitraster.Constants.PIXEL_WIDTH;
import static grondag.bitraster.Constants.PRECISE_HEIGHT;
import static grondag.bitraster.Constants.PRECISE_HEIGHT_CLAMP;
import static grondag.bitraster.Constants.PRECISE_WIDTH;
import static grondag.bitraster.Constants.PRECISE_WIDTH_CLAMP;
import static grondag.bitraster.Constants.PRECISION_BITS;
import static grondag.bitraster.Constants.PV_PX;
import static grondag.bitraster.Constants.PV_PY;
import static grondag.bitraster.Constants.PV_W;
import static grondag.bitraster.Constants.PV_Z;
import static grondag.bitraster.Constants.SCANT_PRECISE_PIXEL_CENTER;
import static grondag.bitraster.Constants.TILE_AXIS_MASK;
import static grondag.bitraster.Constants.TILE_AXIS_SHIFT;
import static grondag.bitraster.Constants.TILE_COUNT;
import static grondag.bitraster.Constants.TILE_HEIGHT_MASK;
import static grondag.bitraster.Constants.TILE_WIDTH;
import static grondag.bitraster.Constants.TILE_WIDTH_BITS;
import static grondag.bitraster.Constants.TILE_WIDTH_MASK;
import static grondag.bitraster.Constants.VERTEX_DATA_LENGTH;
import static grondag.bitraster.Indexer.tileIndexFromPixelXY;

// Some elements are adapted from content found at
// https://fgiesen.wordpress.com/2013/02/17/optimizing-sw-occlusion-culling-index/
// by Fabian “ryg” Giesen. That content is in the public domain.
public abstract class AbstractRasterizer {
	final Matrix4L mvpMatrix = new Matrix4L();
	final int[] vertexData = new int[VERTEX_DATA_LENGTH];
	final int[] eventData = new int[EVENT_DATA_LENGTH];
	final long[] tiles = new long[TILE_COUNT];
	final EventFiller[] EVENT_FILLERS = new EventFiller[0x1000];

	/** Bounds of current triangle - pixel coordinates. */
	protected int minPixelX, minPixelY, maxPixelX, maxPixelY;

	/** Classifies each edge, holding results of {@link #edgePosition(int, int, int, int)}. */
	protected int pos0, pos1, pos2, pos3;

	protected int saveTileIndex;
	/** Control iteration in populateEvents_ methods. */
	protected int eventY0, eventLimit;

	{
		EVENT_FILLERS[EVENT_0123_RRRR] = () -> {
			populateLeftEvents();
			populateRightEvents4(IDX_AX0, IDX_BX0, IDX_CX0, IDX_DX0);
		};
		EVENT_FILLERS[EVENT_0123_LRRR] = () -> {
			populateLeftEvents(IDX_AX0);
			populateRightEvents3(IDX_BX0, IDX_CX0, IDX_DX0);
		};
		EVENT_FILLERS[EVENT_0123_FRRR] = () -> {
			populateLeftEvents();
			populateRightEvents3(IDX_BX0, IDX_CX0, IDX_DX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
		};
		EVENT_FILLERS[EVENT_0123_RLRR] = () -> {
			populateLeftEvents(IDX_BX0);
			populateRightEvents3(IDX_AX0, IDX_CX0, IDX_DX0);
		};
		EVENT_FILLERS[EVENT_0123_LLRR] = () -> {
			populateLeftEvents2(IDX_AX0, IDX_BX0);
			populateRightEvents2(IDX_CX0, IDX_DX0);
		};
		EVENT_FILLERS[EVENT_0123_FLRR] = () -> {
			populateLeftEvents(IDX_BX0);
			populateRightEvents2(IDX_CX0, IDX_DX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
		};
		EVENT_FILLERS[EVENT_0123_RFRR] = () -> {
			populateLeftEvents();
			populateRightEvents3(IDX_AX0, IDX_CX0, IDX_DX0);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
		};
		EVENT_FILLERS[EVENT_0123_LFRR] = () -> {
			populateLeftEvents(IDX_AX0);
			populateRightEvents2(IDX_CX0, IDX_DX0);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
		};
		EVENT_FILLERS[EVENT_0123_FFRR] = () -> {
			populateLeftEvents();
			populateRightEvents2(IDX_CX0, IDX_DX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
		};

		EVENT_FILLERS[EVENT_0123_RRLR] = () -> {
			populateLeftEvents(IDX_CX0);
			populateRightEvents3(IDX_AX0, IDX_BX0, IDX_DX0);
		};
		EVENT_FILLERS[EVENT_0123_LRLR] = () -> {
			populateLeftEvents2(IDX_AX0, IDX_CX0);
			populateRightEvents2(IDX_BX0, IDX_DX0);
		};
		EVENT_FILLERS[EVENT_0123_FRLR] = () -> {
			populateLeftEvents(IDX_CX0);
			populateRightEvents2(IDX_BX0, IDX_DX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
		};
		EVENT_FILLERS[EVENT_0123_RLLR] = () -> {
			populateLeftEvents2(IDX_BX0, IDX_CX0);
			populateRightEvents2(IDX_AX0, IDX_DX0);
		};
		EVENT_FILLERS[EVENT_0123_LLLR] = () -> {
			populateLeftEvents3(IDX_AX0, IDX_BX0, IDX_CX0);
			populateRightEvents(IDX_DX0);
		};
		EVENT_FILLERS[EVENT_0123_FLLR] = () -> {
			populateLeftEvents2(IDX_BX0, IDX_CX0);
			populateRightEvents(IDX_DX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
		};
		EVENT_FILLERS[EVENT_0123_RFLR] = () -> {
			populateLeftEvents(IDX_CX0);
			populateRightEvents2(IDX_AX0, IDX_DX0);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
		};
		EVENT_FILLERS[EVENT_0123_LFLR] = () -> {
			populateLeftEvents2(IDX_AX0, IDX_CX0);
			populateRightEvents(IDX_DX0);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
		};
		EVENT_FILLERS[EVENT_0123_FFLR] = () -> {
			populateLeftEvents(IDX_CX0);
			populateRightEvents(IDX_DX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
		};

		EVENT_FILLERS[EVENT_0123_LRFR] = () -> {
			populateLeftEvents(IDX_AX0);
			populateRightEvents2(IDX_BX0, IDX_DX0);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
		};
		EVENT_FILLERS[EVENT_0123_RRFR] = () -> {
			populateLeftEvents();
			populateRightEvents3(IDX_AX0, IDX_BX0, IDX_DX0);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
		};
		EVENT_FILLERS[EVENT_0123_FRFR] = () -> {
			populateLeftEvents();
			populateRightEvents2(IDX_BX0, IDX_DX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
		};
		EVENT_FILLERS[EVENT_0123_RLFR] = () -> {
			populateLeftEvents(IDX_BX0);
			populateRightEvents2(IDX_AX0, IDX_DX0);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
		};
		EVENT_FILLERS[EVENT_0123_LLFR] = () -> {
			populateLeftEvents2(IDX_AX0, IDX_BX0);
			populateRightEvents(IDX_DX0);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
		};
		EVENT_FILLERS[EVENT_0123_FLFR] = () -> {
			populateLeftEvents(IDX_BX0);
			populateRightEvents(IDX_DX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
		};
		EVENT_FILLERS[EVENT_0123_RFFR] = () -> {
			populateLeftEvents();
			populateRightEvents2(IDX_AX0, IDX_DX0);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
		};
		EVENT_FILLERS[EVENT_0123_LFFR] = () -> {
			populateLeftEvents(IDX_AX0);
			populateRightEvents(IDX_DX0);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
		};
		EVENT_FILLERS[EVENT_0123_FFFR] = () -> {
			populateLeftEvents();
			populateRightEvents(IDX_DX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
		};

		EVENT_FILLERS[EVENT_0123_RRRL] = () -> {
			populateLeftEvents(IDX_DX0);
			populateRightEvents3(IDX_AX0, IDX_BX0, IDX_CX0);
		};
		EVENT_FILLERS[EVENT_0123_LRRL] = () -> {
			populateLeftEvents2(IDX_AX0, IDX_DX0);
			populateRightEvents2(IDX_BX0, IDX_CX0);
		};
		EVENT_FILLERS[EVENT_0123_FRRL] = () -> {
			populateLeftEvents(IDX_DX0);
			populateRightEvents2(IDX_BX0, IDX_CX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
		};
		EVENT_FILLERS[EVENT_0123_RLRL] = () -> {
			populateLeftEvents2(IDX_BX0, IDX_DX0);
			populateRightEvents2(IDX_AX0, IDX_CX0);
		};
		EVENT_FILLERS[EVENT_0123_LLRL] = () -> {
			populateLeftEvents3(IDX_AX0, IDX_BX0, IDX_DX0);
			populateRightEvents(IDX_CX0);
		};
		EVENT_FILLERS[EVENT_0123_FLRL] = () -> {
			populateLeftEvents2(IDX_BX0, IDX_DX0);
			populateRightEvents(IDX_CX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
		};
		EVENT_FILLERS[EVENT_0123_RFRL] = () -> {
			populateLeftEvents(IDX_DX0);
			populateRightEvents2(IDX_AX0, IDX_CX0);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
		};
		EVENT_FILLERS[EVENT_0123_LFRL] = () -> {
			populateLeftEvents2(IDX_AX0, IDX_DX0);
			populateRightEvents(IDX_CX0);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
		};
		EVENT_FILLERS[EVENT_0123_FFRL] = () -> {
			populateLeftEvents(IDX_DX0);
			populateRightEvents(IDX_CX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
		};
		EVENT_FILLERS[EVENT_0123_RRLL] = () -> {
			populateLeftEvents2(IDX_CX0, IDX_DX0);
			populateRightEvents2(IDX_AX0, IDX_BX0);
		};
		EVENT_FILLERS[EVENT_0123_LRLL] = () -> {
			populateLeftEvents3(IDX_AX0, IDX_CX0, IDX_DX0);
			populateRightEvents(IDX_BX0);
		};
		EVENT_FILLERS[EVENT_0123_FRLL] = () -> {
			populateLeftEvents2(IDX_CX0, IDX_DX0);
			populateRightEvents(IDX_BX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
		};
		EVENT_FILLERS[EVENT_0123_RLLL] = () -> {
			populateLeftEvents3(IDX_BX0, IDX_CX0, IDX_DX0);
			populateRightEvents(IDX_AX0);
		};
		EVENT_FILLERS[EVENT_0123_LLLL] = () -> {
			populateLeftEvents4(IDX_AX0, IDX_BX0, IDX_CX0, IDX_DX0);
			populateRightEvents();
		};
		EVENT_FILLERS[EVENT_0123_FLLL] = () -> {
			populateLeftEvents3(IDX_BX0, IDX_CX0, IDX_DX0);
			populateRightEvents();
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
		};
		EVENT_FILLERS[EVENT_0123_RFLL] = () -> {
			populateLeftEvents2(IDX_CX0, IDX_DX0);
			populateRightEvents(IDX_AX0);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
		};
		EVENT_FILLERS[EVENT_0123_LFLL] = () -> {
			populateLeftEvents3(IDX_AX0, IDX_CX0, IDX_DX0);
			populateRightEvents();
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
		};
		EVENT_FILLERS[EVENT_0123_FFLL] = () -> {
			populateLeftEvents2(IDX_CX0, IDX_DX0);
			populateRightEvents();
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
		};
		EVENT_FILLERS[EVENT_0123_LRFL] = () -> {
			populateLeftEvents2(IDX_AX0, IDX_DX0);
			populateRightEvents(IDX_BX0);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
		};
		EVENT_FILLERS[EVENT_0123_RRFL] = () -> {
			populateLeftEvents(IDX_DX0);
			populateRightEvents2(IDX_AX0, IDX_BX0);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
		};
		EVENT_FILLERS[EVENT_0123_FRFL] = () -> {
			populateLeftEvents(IDX_DX0);
			populateRightEvents(IDX_BX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
		};
		EVENT_FILLERS[EVENT_0123_RLFL] = () -> {
			populateLeftEvents2(IDX_BX0, IDX_DX0);
			populateRightEvents(IDX_AX0);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
		};
		EVENT_FILLERS[EVENT_0123_LLFL] = () -> {
			populateLeftEvents3(IDX_AX0, IDX_BX0, IDX_DX0);
			populateRightEvents();
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
		};
		EVENT_FILLERS[EVENT_0123_FLFL] = () -> {
			populateLeftEvents2(IDX_BX0, IDX_DX0);
			populateRightEvents();
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
		};
		EVENT_FILLERS[EVENT_0123_RFFL] = () -> {
			populateLeftEvents(IDX_DX0);
			populateRightEvents(IDX_AX0);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
		};
		EVENT_FILLERS[EVENT_0123_LFFL] = () -> {
			populateLeftEvents2(IDX_AX0, IDX_DX0);
			populateRightEvents();
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
		};
		EVENT_FILLERS[EVENT_0123_FFFL] = () -> {
			populateLeftEvents(IDX_DX0);
			populateRightEvents();
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
		};
		EVENT_FILLERS[EVENT_0123_RRRF] = () -> {
			populateLeftEvents();
			populateRightEvents3(IDX_AX0, IDX_BX0, IDX_CX0);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_LRRF] = () -> {
			populateLeftEvents(IDX_AX0);
			populateRightEvents2(IDX_BX0, IDX_CX0);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_FRRF] = () -> {
			populateLeftEvents();
			populateRightEvents2(IDX_BX0, IDX_CX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_RLRF] = () -> {
			populateLeftEvents(IDX_BX0);
			populateRightEvents2(IDX_AX0, IDX_CX0);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_LLRF] = () -> {
			populateLeftEvents2(IDX_AX0, IDX_BX0);
			populateRightEvents(IDX_CX0);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_FLRF] = () -> {
			populateLeftEvents(IDX_BX0);
			populateRightEvents(IDX_CX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_RFRF] = () -> {
			populateLeftEvents();
			populateRightEvents2(IDX_AX0, IDX_CX0);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_LFRF] = () -> {
			populateLeftEvents(IDX_AX0);
			populateRightEvents(IDX_CX0);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_FFRF] = () -> {
			populateLeftEvents();
			populateRightEvents(IDX_CX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_RRLF] = () -> {
			populateLeftEvents(IDX_CX0);
			populateRightEvents2(IDX_AX0, IDX_BX0);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_LRLF] = () -> {
			populateLeftEvents2(IDX_AX0, IDX_CX0);
			populateRightEvents(IDX_BX0);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_FRLF] = () -> {
			populateLeftEvents(IDX_CX0);
			populateRightEvents(IDX_BX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_RLLF] = () -> {
			populateLeftEvents2(IDX_BX0, IDX_CX0);
			populateRightEvents(IDX_AX0);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_LLLF] = () -> {
			populateLeftEvents3(IDX_AX0, IDX_BX0, IDX_CX0);
			populateRightEvents();
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_FLLF] = () -> {
			populateLeftEvents2(IDX_BX0, IDX_CX0);
			populateRightEvents();
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_RFLF] = () -> {
			populateLeftEvents(IDX_CX0);
			populateRightEvents(IDX_AX0);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_LFLF] = () -> {
			populateLeftEvents2(IDX_AX0, IDX_CX0);
			populateRightEvents();
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_FFLF] = () -> {
			populateLeftEvents(IDX_CX0);
			populateRightEvents();
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_LRFF] = () -> {
			populateLeftEvents(IDX_AX0);
			populateRightEvents(IDX_BX0);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_RRFF] = () -> {
			populateLeftEvents();
			populateRightEvents2(IDX_AX0, IDX_BX0);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_FRFF] = () -> {
			populateLeftEvents();
			populateRightEvents(IDX_BX0);
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_RLFF] = () -> {
			populateLeftEvents(IDX_BX0);
			populateRightEvents(IDX_AX0);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_LLFF] = () -> {
			populateLeftEvents2(IDX_AX0, IDX_BX0);
			populateRightEvents();
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_FLFF] = () -> {
			populateLeftEvents(IDX_BX0);
			populateRightEvents();
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_RFFF] = () -> {
			populateLeftEvents();
			populateRightEvents(IDX_AX0);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_LFFF] = () -> {
			populateLeftEvents(IDX_AX0);
			populateRightEvents();
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
		EVENT_FILLERS[EVENT_0123_FFFF] = () -> {
			// fill it
			populateLeftEvents();
			populateRightEvents();
			populateFlatEvents(pos0, vertexData[IDX_AY0]);
			populateFlatEvents(pos1, vertexData[IDX_BY0]);
			populateFlatEvents(pos2, vertexData[IDX_CY0]);
			populateFlatEvents(pos3, vertexData[IDX_DY0]);
		};
	}

	final void copyFrom(AbstractRasterizer source) {
		mvpMatrix.copyFrom(source.mvpMatrix);
		System.arraycopy(source.vertexData, 0, vertexData, 0, VERTEX_DATA_LENGTH);
		System.arraycopy(source.eventData, 0, eventData, 0, EVENT_DATA_LENGTH);
		System.arraycopy(source.tiles, 0, tiles, 0, TILE_COUNT);
	}

	final void drawQuad(int v0, int v1, int v2, int v3) {
		final int boundsResult = prepareBounds(v0, v1, v2, v3);

		if (boundsResult == BOUNDS_OUTSIDE_OR_TOO_SMALL) {
			return;
		}

		// Don't draw single points
		if (minPixelX == maxPixelX && minPixelY == maxPixelY) {
			return;
		}

		drawQuad();
	}

	final boolean isQuadPartiallyClear(int v0, int v1, int v2, int v3) {
		final int boundsResult = prepareBounds(v0, v1, v2, v3);

		if (boundsResult == BOUNDS_OUTSIDE_OR_TOO_SMALL) {
			return false;
		}

		final int px = minPixelX;
		final int py = minPixelY;

		if (px == maxPixelX && py == maxPixelY) {
			return px >= 0 && py >= 0 && px < PIXEL_WIDTH && py < PIXEL_HEIGHT && isPixelClear(px, py);
		} else {
			return isQuadPartiallyClear();
		}
	}

	final boolean isQuadPartiallyClear() {
		final int minTileX = minPixelX >> TILE_AXIS_SHIFT;
		final int maxTileX = maxPixelX >> TILE_AXIS_SHIFT;
		final int maxTileY = (maxPixelY >> TILE_AXIS_SHIFT) << TILE_WIDTH_BITS;

		int tileIndex = tileIndexFromPixelXY(minPixelX, minPixelY);
		int xLimit = maxTileX;
		int xInc = 1;

		while (true) {
			if (isQuadPartiallyClearInner(tileIndex)) {
				return true;
			}

			if ((tileIndex & TILE_WIDTH_MASK) == xLimit) {
				if ((tileIndex & TILE_HEIGHT_MASK) == maxTileY) {
					return false;
				} else {
					// move up
					tileIndex += TILE_WIDTH;
					// flip x order
					xLimit = xLimit == maxTileX ? minTileX : maxTileX;
					xInc = -xInc;
				}
			} else {
				// move left or right
				tileIndex += xInc;
			}
		}
	}

	final boolean isQuadPartiallyClearInner(int tileIndex) {
		final long word = tiles[tileIndex];

		// nothing to test if fully occluded
		if (word == -1L) {
			return false;
		}

		return (~word & computeTileCoverage(tileIndex)) != 0;
	}

	final boolean isQuadPartiallyOccluded(int v0, int v1, int v2, int v3) {
		final int boundsResult = prepareBounds(v0, v1, v2, v3);

		if (boundsResult == BOUNDS_OUTSIDE_OR_TOO_SMALL) {
			return false;
		}

		final int px = minPixelX;
		final int py = minPixelY;

		if (px == maxPixelX && py == maxPixelY) {
			return px >= 0 && py >= 0 && px < PIXEL_WIDTH && py < PIXEL_HEIGHT && isPixelSet(px, py);
		} else {
			return isQuadPartiallyOccluded();
		}
	}

	final boolean isQuadPartiallyOccluded() {
		final int minTileX = minPixelX >> TILE_AXIS_SHIFT;
		final int maxTileX = maxPixelX >> TILE_AXIS_SHIFT;
		final int maxTileY = (maxPixelY >> TILE_AXIS_SHIFT) << TILE_WIDTH_BITS;

		int tileIndex = tileIndexFromPixelXY(minPixelX, minPixelY);
		int xLimit = maxTileX;
		int xInc = 1;

		while (true) {
			if (isQuadPartiallyOccludedInner(tileIndex)) {
				return true;
			}

			if ((tileIndex & TILE_WIDTH_MASK) == xLimit) {
				if ((tileIndex & TILE_HEIGHT_MASK) == maxTileY) {
					return false;
				} else {
					// move up
					tileIndex += TILE_WIDTH;
					// flip x order
					xLimit = xLimit == maxTileX ? minTileX : maxTileX;
					xInc = -xInc;
				}
			} else {
				// move left or right
				tileIndex += xInc;
			}
		}
	}

	final boolean isQuadPartiallyOccludedInner(int tileIndex) {
		final long word = tiles[tileIndex];

		// nothing to test if fully clear
		if (word == 0) {
			return false;
		}

		return (word & computeTileCoverage(tileIndex)) != 0;
	}

	final void drawQuad() {
		final int minTileX = minPixelX >> TILE_AXIS_SHIFT;
		final int maxTileX = maxPixelX >> TILE_AXIS_SHIFT;
		final int maxTileY = (maxPixelY >> TILE_AXIS_SHIFT) << TILE_WIDTH_BITS;

		int tileIndex = tileIndexFromPixelXY(minPixelX, minPixelY);
		int xLimit = maxTileX;
		int xInc = 1;

		while (true) {
			drawQuadInner(tileIndex);

			if ((tileIndex & TILE_WIDTH_MASK) == xLimit) {
				if ((tileIndex & TILE_HEIGHT_MASK) == maxTileY) {
					return;
				} else {
					// move up
					tileIndex += TILE_WIDTH;
					// flip x order
					xLimit = xLimit == maxTileX ? minTileX : maxTileX;
					xInc = -xInc;
				}
			} else {
				// move left or right
				tileIndex += xInc;
			}
		}
	}

	final void drawQuadInner(int tileIndex) {
		long word = tiles[tileIndex];

		// nothing to do if fully occluded
		if (word != -1L) {
			word |= computeTileCoverage(tileIndex);
			tiles[tileIndex] = word;
		}
	}

	abstract int prepareBounds(int v0, int v1, int v2, int v3);

	private void populateFlatEvents(int position, int y0In) {
		final int[] eventData = this.eventData;

		if (position == EDGE_TOP) {
			final int py = ((y0In + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) + 1;

			if (py == MAX_PIXEL_Y) return;

			final int y1 = (maxPixelY & TILE_AXIS_MASK) + 7;
			final int start = py < 0 ? 0 : (py << 1);
			final int limit = y1 << 1;

			assert limit < EVENT_DATA_LENGTH;

			for (int y = start; y <= limit; ) {
				eventData[y++] = PIXEL_WIDTH;
				eventData[y++] = -1;
			}
		} else if (position == EDGE_BOTTOM) {
			final int py = (y0In >> PRECISION_BITS);

			if (py == 0) return;

			final int y0 = minPixelY & TILE_AXIS_MASK;
			final int start = y0 << 1;
			final int limit = py > MAX_PIXEL_Y ? (MAX_PIXEL_Y << 1) : (py << 1);

			assert limit < EVENT_DATA_LENGTH;

			for (int y = start; y < limit; ) {
				eventData[y++] = PIXEL_WIDTH;
				eventData[y++] = -1;
			}
		} else {
			assert position == EDGE_POINT;
		}
	}

	/**
	 * Puts left edge at screen boundary.
	 */
	private void populateLeftEvents() {
		final int[] eventData = this.eventData;
		final int y0 = eventY0;
		final int limit = eventLimit;

		for (int y = y0 << 1; y <= limit; y += 2) {
			eventData[y] = 0;
		}
	}

	private void populateLeftEvents(int a) {
		final int[] vertexData = this.vertexData;
		final int ax0 = vertexData[a];
		final int ay0 = vertexData[a + 1];
		final int ax1 = vertexData[a + 2];
		final int ay1 = vertexData[a + 3];

		final int[] eventData = this.eventData;
		final int y0 = eventY0;
		final int limit = eventLimit;
		final long dx = ax1 - ax0;

		if (dx == 0) {
			final int x = ((ax0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS);

			for (int y = (y0 << 1); y <= limit; y += 2) {
				eventData[y] = x;
			}
		} else {
			final int dy = ay1 - ay0;
			final long n = (dx << 16) / dy;
			final long nStep = n << PRECISION_BITS;
			long x = ((long) ax0 << 16) - n * ay0 + nStep * y0 + 0x100000L;

			for (int y = (y0 << 1); y <= limit; y += 2) {
				eventData[y] = (int) (x >> 20);
				x += nStep;
			}
		}
	}

	private void populateRightEvents() {
		final int[] eventData = this.eventData;
		final int y0 = eventY0;
		// difference from left: is high index in pairs
		final int limit = eventLimit + 1;

		// difference from left: is high index in pairs
		for (int y = (y0 << 1) + 1; y <= limit; y += 2) {
			eventData[y] = PIXEL_WIDTH;
		}
	}

	private void populateRightEvents(int a) {
		final int[] vertexData = this.vertexData;

		final int ax0 = vertexData[a];
		final int ay0 = vertexData[a + 1];
		final int ax1 = vertexData[a + 2];
		final int ay1 = vertexData[a + 3];

		final int[] eventData = this.eventData;
		final int y0 = eventY0;
		// difference from left: is high index in pairs
		final int limit = eventLimit + 1;
		final int dx = ax1 - ax0;


		if (dx == 0) {
			final int x = (ax0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS;

			// difference from left: is high index in pairs
			for (int y = (y0 << 1) + 1; y <= limit; y += 2) {
				eventData[y] = x;
			}
		} else {
			final int dy = ay1 - ay0;
			final long n = (((long) dx) << 16) / dy;
			final long nStep = n << PRECISION_BITS;
			// difference from left: rounding looses tie
			long x = ((long) ax0 << 16) - n * ay0 + nStep * y0 + 0x7FFFFL;

			// difference from left: is high index in pairs
			for (int y = (y0 << 1) + 1; y <= limit; y += 2) {
				eventData[y] = (int) (x >> 20);
				x += nStep;
			}
		}
	}

	private void populateLeftEvents2(int a, int b) {
		final int[] vertexData = this.vertexData;

		final int ax0 = vertexData[a];
		final int ay0 = vertexData[a + 1];
		final int ax1 = vertexData[a + 2];
		final int ay1 = vertexData[a + 3];

		final int bx0 = vertexData[b];
		final int by0 = vertexData[b + 1];
		final int bx1 = vertexData[b + 2];
		final int by1 = vertexData[b + 3];

		final int[] eventData = this.eventData;
		final int y0 = eventY0;
		final int limit = eventLimit;

		final long aStep;
		long ax;
		final long bStep;
		long bx;

		if (ax0 == ax1) {
			ax = ((ax0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) << 20;
			aStep = 0;
		} else {
			final int ady = ay1 - ay0;
			final int adx = ax1 - ax0;
			final long an = (((long) adx) << 16) / ady;
			aStep = an << PRECISION_BITS;
			ax = ((long) ax0 << 16) - an * ay0 + aStep * y0 + 0x100000L;
		}

		if (bx0 == bx1) {
			bx = ((bx0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) << 20;
			bStep = 0;
		} else {
			final int bdx = bx1 - bx0;
			final int bdy = by1 - by0;
			final long bn = (((long) bdx) << 16) / bdy;
			bStep = bn << PRECISION_BITS;
			bx = ((long) bx0 << 16) - bn * by0 + bStep * y0 + 0x100000L;
		}

		for (int y = (y0 << 1); y <= limit; y += 2) {
			final long x = ax > bx ? ax : bx;

			eventData[y] = (int) (x >> 20);

			ax += aStep;
			bx += bStep;
		}
	}

	private void populateLeftEvents3(int a, int b, int c) {
		final int[] vertexData = this.vertexData;

		final int ax0 = vertexData[a];
		final int ay0 = vertexData[a + 1];
		final int ax1 = vertexData[a + 2];
		final int ay1 = vertexData[a + 3];

		final int bx0 = vertexData[b];
		final int by0 = vertexData[b + 1];
		final int bx1 = vertexData[b + 2];
		final int by1 = vertexData[b + 3];

		final int cx0 = vertexData[c];
		final int cy0 = vertexData[c + 1];
		final int cx1 = vertexData[c + 2];
		final int cy1 = vertexData[c + 3];

		final int[] eventData = this.eventData;
		final int y0 = eventY0;
		final int limit = eventLimit;

		final long aStep;
		long ax;
		final long bStep;
		long bx;
		final long cStep;
		long cx;

		if (ax0 == ax1) {
			ax = ((ax0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) << 20;
			aStep = 0;
		} else {
			final int ady = ay1 - ay0;
			final int adx = ax1 - ax0;
			final long an = (((long) adx) << 16) / ady;
			aStep = an << PRECISION_BITS;
			ax = ((long) ax0 << 16) - an * ay0 + aStep * y0 + 0x100000L;
		}

		if (bx0 == bx1) {
			bx = ((bx0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) << 20;
			bStep = 0;
		} else {
			final int bdx = bx1 - bx0;
			final int bdy = by1 - by0;
			final long bn = (((long) bdx) << 16) / bdy;
			bStep = bn << PRECISION_BITS;
			bx = ((long) bx0 << 16) - bn * by0 + bStep * y0 + 0x100000L;
		}

		if (cx0 == cx1) {
			cx = ((cx0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) << 20;
			cStep = 0;
		} else {
			final int cdx = cx1 - cx0;
			final int cdy = cy1 - cy0;
			final long cn = (((long) cdx) << 16) / cdy;
			cStep = cn << PRECISION_BITS;
			cx = ((long) cx0 << 16) - cn * cy0 + cStep * y0 + 0x100000L;
		}

		for (int y = (y0 << 1); y <= limit; y += 2) {
			long x = ax > bx ? ax : bx;
			if (cx > x) x = cx;

			eventData[y] = (int) (x >> 20);

			ax += aStep;
			bx += bStep;
			cx += cStep;
		}
	}

	private void populateLeftEvents4(int a, int b, int c, int d) {
		final int[] vertexData = this.vertexData;

		final int ax0 = vertexData[a];
		final int ay0 = vertexData[a + 1];
		final int ax1 = vertexData[a + 2];
		final int ay1 = vertexData[a + 3];

		final int bx0 = vertexData[b];
		final int by0 = vertexData[b + 1];
		final int bx1 = vertexData[b + 2];
		final int by1 = vertexData[b + 3];

		final int cx0 = vertexData[c];
		final int cy0 = vertexData[c + 1];
		final int cx1 = vertexData[c + 2];
		final int cy1 = vertexData[c + 3];

		final int dx0 = vertexData[d];
		final int dy0 = vertexData[d + 1];
		final int dx1 = vertexData[d + 2];
		final int dy1 = vertexData[d + 3];

		final int[] eventData = this.eventData;
		final int y0 = eventY0;
		final int limit = eventLimit;

		final long aStep;
		long ax;
		final long bStep;
		long bx;
		final long cStep;
		long cx;
		final long dStep;
		long dx;

		if (ax0 == ax1) {
			ax = ((ax0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) << 20;
			aStep = 0;
		} else {
			final int ady = ay1 - ay0;
			final int adx = ax1 - ax0;
			final long an = (((long) adx) << 16) / ady;
			aStep = an << PRECISION_BITS;
			ax = ((long) ax0 << 16) - an * ay0 + aStep * y0 + 0x100000L;
		}

		if (bx0 == bx1) {
			bx = ((bx0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) << 20;
			bStep = 0;
		} else {
			final int bdx = bx1 - bx0;
			final int bdy = by1 - by0;
			final long bn = (((long) bdx) << 16) / bdy;
			bStep = bn << PRECISION_BITS;
			bx = ((long) bx0 << 16) - bn * by0 + bStep * y0 + 0x100000L;
		}

		if (cx0 == cx1) {
			cx = ((cx0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) << 20;
			cStep = 0;
		} else {
			final int cdx = cx1 - cx0;
			final int cdy = cy1 - cy0;
			final long cn = (((long) cdx) << 16) / cdy;
			cStep = cn << PRECISION_BITS;
			cx = ((long) cx0 << 16) - cn * cy0 + cStep * y0 + 0x100000L;
		}

		if (dx0 == dx1) {
			dx = ((dx0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) << 20;
			dStep = 0;
		} else {
			final int ddx = dx1 - dx0;
			final int ddy = dy1 - dy0;
			final long dn = (((long) ddx) << 16) / ddy;
			dStep = dn << PRECISION_BITS;
			dx = ((long) dx0 << 16) - dn * dy0 + dStep * y0 + 0x100000L;
		}

		for (int y = (y0 << 1); y <= limit; y += 2) {
			long x = ax > bx ? ax : bx;
			if (cx > x) x = cx;
			if (dx > x) x = dx;

			eventData[y] = (int) (x >> 20);

			ax += aStep;
			bx += bStep;
			cx += cStep;
			dx += dStep;
		}
	}

	private void populateRightEvents2(int a, int b) {
		final int[] vertexData = this.vertexData;

		final int ax0 = vertexData[a];
		final int ay0 = vertexData[a + 1];
		final int ax1 = vertexData[a + 2];
		final int ay1 = vertexData[a + 3];

		final int bx0 = vertexData[b];
		final int by0 = vertexData[b + 1];
		final int bx1 = vertexData[b + 2];
		final int by1 = vertexData[b + 3];

		final int[] eventData = this.eventData;
		final int y0 = eventY0;
		// difference from left: is high index in pairs
		final int limit = eventLimit + 1;

		final long aStep;
		long ax;
		final long bStep;
		long bx;

		if (ax0 == ax1) {
			ax = ((ax0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) << 20;
			aStep = 0;
		} else {
			final int ady = ay1 - ay0;
			final int adx = ax1 - ax0;
			final long an = (((long) adx) << 16) / ady;
			aStep = an << PRECISION_BITS;
			// difference from left: rounding looses tie
			ax = ((long) ax0 << 16) - an * ay0 + aStep * y0 + 0x7FFFFL;
		}

		if (bx0 == bx1) {
			bx = ((bx0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) << 20;
			bStep = 0;
		} else {
			final int bdx = bx1 - bx0;
			final int bdy = by1 - by0;
			final long bn = (((long) bdx) << 16) / bdy;
			bStep = bn << PRECISION_BITS;
			// difference from left: rounding looses tie
			bx = ((long) bx0 << 16) - bn * by0 + bStep * y0 + 0x7FFFFL;
		}

		// difference from left: is high index in pairs
		for (int y = (y0 << 1) + 1; y <= limit; y += 2) {
			// difference from left: lower value wins
			final long x = ax < bx ? ax : bx;

			eventData[y] = (int) (x >> 20);

			ax += aStep;
			bx += bStep;
		}
	}

	private void populateRightEvents3(int a, int b, int c) {
		final int[] vertexData = this.vertexData;

		final int ax0 = vertexData[a];
		final int ay0 = vertexData[a + 1];
		final int ax1 = vertexData[a + 2];
		final int ay1 = vertexData[a + 3];

		final int bx0 = vertexData[b];
		final int by0 = vertexData[b + 1];
		final int bx1 = vertexData[b + 2];
		final int by1 = vertexData[b + 3];

		final int cx0 = vertexData[c];
		final int cy0 = vertexData[c + 1];
		final int cx1 = vertexData[c + 2];
		final int cy1 = vertexData[c + 3];

		final int[] eventData = this.eventData;
		final int y0 = eventY0;
		// difference from left: is high index in pairs
		final int limit = eventLimit + 1;

		final long aStep;
		long ax;
		final long bStep;
		long bx;
		final long cStep;
		long cx;

		if (ax0 == ax1) {
			ax = ((ax0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) << 20;
			aStep = 0;
		} else {
			final int ady = ay1 - ay0;
			final int adx = ax1 - ax0;
			final long an = (((long) adx) << 16) / ady;
			aStep = an << PRECISION_BITS;
			// difference from left: rounding looses tie
			ax = ((long) ax0 << 16) - an * ay0 + aStep * y0 + 0x7FFFFL;
		}

		if (bx0 == bx1) {
			bx = ((bx0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) << 20;
			bStep = 0;
		} else {
			final int bdx = bx1 - bx0;
			final int bdy = by1 - by0;
			final long bn = (((long) bdx) << 16) / bdy;
			bStep = bn << PRECISION_BITS;
			// difference from left: rounding looses tie
			bx = ((long) bx0 << 16) - bn * by0 + bStep * y0 + 0x7FFFFL;
		}

		if (cx0 == cx1) {
			cx = ((cx0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) << 20;
			cStep = 0;
		} else {
			final int cdx = cx1 - cx0;
			final int cdy = cy1 - cy0;
			final long cn = (((long) cdx) << 16) / cdy;
			cStep = cn << PRECISION_BITS;
			// difference from left: rounding looses tie
			cx = ((long) cx0 << 16) - cn * cy0 + cStep * y0 + 0x7FFFFL;
		}

		// difference from left: is high index in pairs
		for (int y = (y0 << 1) + 1; y <= limit; y += 2) {
			// difference from left: lower value wins
			long x = ax < bx ? ax : bx;

			if (cx < x) x = cx;

			eventData[y] = (int) (x >> 20);

			ax += aStep;
			bx += bStep;
			cx += cStep;
		}
	}

	private void populateRightEvents4(int a, int b, int c, int d) {
		final int[] vertexData = this.vertexData;

		final int ax0 = vertexData[a];
		final int ay0 = vertexData[a + 1];
		final int ax1 = vertexData[a + 2];
		final int ay1 = vertexData[a + 3];

		final int bx0 = vertexData[b];
		final int by0 = vertexData[b + 1];
		final int bx1 = vertexData[b + 2];
		final int by1 = vertexData[b + 3];

		final int cx0 = vertexData[c];
		final int cy0 = vertexData[c + 1];
		final int cx1 = vertexData[c + 2];
		final int cy1 = vertexData[c + 3];

		final int dx0 = vertexData[d];
		final int dy0 = vertexData[d + 1];
		final int dx1 = vertexData[d + 2];
		final int dy1 = vertexData[d + 3];

		final int[] eventData = this.eventData;
		final int y0 = eventY0;
		// difference from left: is high index in pairs
		final int limit = eventLimit + 1;

		final long aStep;
		long ax;
		final long bStep;
		long bx;
		final long cStep;
		long cx;
		final long dStep;
		long dx;

		if (ax0 == ax1) {
			ax = ((ax0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) << 20;
			aStep = 0;
		} else {
			final int ady = ay1 - ay0;
			final int adx = ax1 - ax0;
			final long an = (((long) adx) << 16) / ady;
			aStep = an << PRECISION_BITS;
			// difference from left: rounding looses tie
			ax = ((long) ax0 << 16) - an * ay0 + aStep * y0 + 0x7FFFFL;
		}

		if (bx0 == bx1) {
			bx = ((bx0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) << 20;
			bStep = 0;
		} else {
			final int bdx = bx1 - bx0;
			final int bdy = by1 - by0;
			final long bn = (((long) bdx) << 16) / bdy;
			bStep = bn << PRECISION_BITS;
			// difference from left: rounding looses tie
			bx = ((long) bx0 << 16) - bn * by0 + bStep * y0 + 0x7FFFFL;
		}

		if (cx0 == cx1) {
			cx = ((cx0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) << 20;
			cStep = 0;
		} else {
			final int cdx = cx1 - cx0;
			final int cdy = cy1 - cy0;
			final long cn = (((long) cdx) << 16) / cdy;
			cStep = cn << PRECISION_BITS;
			// difference from left: rounding looses tie
			cx = ((long) cx0 << 16) - cn * cy0 + cStep * y0 + 0x7FFFFL;
		}

		if (dx0 == dx1) {
			dx = ((dx0 + SCANT_PRECISE_PIXEL_CENTER) >> PRECISION_BITS) << 20;
			dStep = 0;
		} else {
			final int ddx = dx1 - dx0;
			final int ddy = dy1 - dy0;
			final long dn = (((long) ddx) << 16) / ddy;
			dStep = dn << PRECISION_BITS;
			// difference from left: rounding looses tie
			dx = ((long) dx0 << 16) - dn * dy0 + dStep * y0 + 0x7FFFFL;
		}

		// difference from left: is high index in pairs
		for (int y = (y0 << 1) + 1; y <= limit; y += 2) {
			// difference from left: lower value wins
			long x = ax < bx ? ax : bx;

			if (cx < x) x = cx;
			if (dx < x) x = dx;

			eventData[y] = (int) (x >> 20);

			ax += aStep;
			bx += bStep;
			cx += cStep;
			dx += dStep;
		}
	}

	abstract void setupVertex(final int baseIndex, final int x, final int y, final int z);

	int needsNearClip(final int baseIndex) {
		final int[] data = vertexData;
		final float w = Float.intBitsToFloat(data[baseIndex + PV_W]);
		final float z = Float.intBitsToFloat(data[baseIndex + PV_Z]);

		if (w == 0) {
			return 1;
		} else if (w > 0) {
			return (z > 0 && z <= w) ? 0 : 1;
		} else {
			// w < 0
			return (z < 0 && z >= w) ? 0 : 1;
		}
	}

	long computeTileCoverage(int tileIndex) {
		final int[] data = eventData;
		final int tileFirstX = (tileIndex & TILE_WIDTH_MASK) << TILE_AXIS_SHIFT;
		final int tileLastX = tileFirstX + 7;

		// +1 to left shift because events are in pairs
		final int baseEventIndex = (tileIndex >> TILE_WIDTH_BITS) << (TILE_AXIS_SHIFT + 1);
		long mask = 0;
		// distance from left, > 7 means empty
		int l = data[baseEventIndex] - tileFirstX;
		// distance from right, > 7 means empty
		int r = tileLastX - data[baseEventIndex + 1];

		if (l < 8 && r < 8) {
			mask = (l < 0 ? 0xFFL : 0xFFL << l) & (r < 0 ? 0xFFL : 0xFFL >>> r);
		}

		l = data[baseEventIndex + 14] - tileFirstX;
		r = tileLastX - data[baseEventIndex + 15];

		if (l < 8 && r < 8) {
			mask |= ((l < 0 ? 0xFF00000000000000L : 0xFF00000000000000L << l) & (r < 0 ? 0xFF00000000000000L : 0xFF00000000000000L >>> r));
		}

		// Early exit when full coverage is detected
		if (mask == 0xFF000000000000FFL) {
			return -1L;
		}

		l = data[baseEventIndex + 2] - tileFirstX;
		r = tileLastX - data[baseEventIndex + 3];

		if (l < 8 && r < 8) {
			mask |= ((l < 0 ? 0xFF00L : 0xFF00L << l) & (r < 0 ? 0xFF00L : 0xFF00L >>> r));
		}

		l = data[baseEventIndex + 4] - tileFirstX;
		r = tileLastX - data[baseEventIndex + 5];

		if (l < 8 && r < 8) {
			mask |= ((l < 0 ? 0xFF0000L : 0xFF0000L << l) & (r < 0 ? 0xFF0000L : 0xFF0000L >>> r));
		}

		l = data[baseEventIndex + 6] - tileFirstX;
		r = tileLastX - data[baseEventIndex + 7];

		if (l < 8 && r < 8) {
			mask |= ((l < 0 ? 0xFF000000L : 0xFF000000L << l) & (r < 0 ? 0xFF000000L : 0xFF000000L >>> r));
		}

		l = data[baseEventIndex + 8] - tileFirstX;
		r = tileLastX - data[baseEventIndex + 9];

		if (l < 8 && r < 8) {
			mask |= ((l < 0 ? 0xFF00000000L : 0xFF00000000L << l) & (r < 0 ? 0xFF00000000L : 0xFF00000000L >>> r));
		}

		l = data[baseEventIndex + 10] - tileFirstX;
		r = tileLastX - data[baseEventIndex + 11];

		if (l < 8 && r < 8) {
			mask |= ((l < 0 ? 0xFF0000000000L : 0xFF0000000000L << l) & (r < 0 ? 0xFF0000000000L : 0xFF0000000000L >>> r));
		}

		l = data[baseEventIndex + 12] - tileFirstX;
		r = tileLastX - data[baseEventIndex + 13];

		if (l < 8 && r < 8) {
			mask |= ((l < 0 ? 0xFF000000000000L : 0xFF000000000000L << l) & (r < 0 ? 0xFF000000000000L : 0xFF000000000000L >>> r));
		}

		// WIP: remove
		//long bMask = computeTileCoverageB(tileIndex);
		//
		//if (bMask != mask) {
		//	System.out.println("Prior");
		//	Util.printMask(mask);
		//	System.out.println("New");
		//	Util.printMask(bMask);
		//	computeTileCoverageB(tileIndex);
		//}

		return mask;
	}


	public boolean isPixelClear(int x, int y) {
		return (tiles[Indexer.tileIndexFromPixelXY(x, y)] & (1L << (Indexer.pixelIndex(x, y)))) == 0;
	}

	public boolean isPixelSet(int x, int y) {
		return (tiles[Indexer.tileIndexFromPixelXY(x, y)] & (1L << (Indexer.pixelIndex(x, y)))) != 0;
	}

	void drawPixel(int x, int y) {
		tiles[Indexer.tileIndexFromPixelXY(x, y)] |= (1L << (Indexer.pixelIndex(x, y)));
	}

	@FunctionalInterface
	interface EventFiller {
		void apply();
	}

	/**
	 * Classifies the input segment, returning one of the EDGE_ constants.
	 */
	final int edgePosition(int x0In, int y0In, int x1In, int y1In) {
		final int dy = y1In - y0In;
		final int dx = x1In - x0In;
		// signum of dx and dy, with shifted masks to derive the edge constant directly
		// the edge constants are specifically formulated to allow this, inline, avoids any pointer chases
		// sign of dy is inverted for historical reasons
		return (1 << (((-dy >> 31) | (dy >>> 31)) + 1)) | (1 << (((dx >> 31) | (-dx >>> 31)) + 4));
	}

	final int prepareBoundsNoClip(int v0, int v1, int v2, int v3) {
		final int[] data = vertexData;
		int ax0, ay0, ax1, ay1;
		int bx0, by0, bx1, by1;
		int cx0, cy0, cx1, cy1;
		int dx0, dy0, dx1, dy1;
		int minY = 0, maxY = 0, minX = 0, maxX = 0;

		ax0 = data[v0 + PV_PX];
		ay0 = data[v0 + PV_PY];
		bx0 = data[v1 + PV_PX];
		by0 = data[v1 + PV_PY];
		cx0 = data[v2 + PV_PX];
		cy0 = data[v2 + PV_PY];
		dx0 = data[v3 + PV_PX];
		dy0 = data[v3 + PV_PY];

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
		pos0 = position0;
		pos1 = position1;
		pos2 = position2;
		pos3 = position3;

		final int eventKey = (position0 - 1) & EVENT_POSITION_MASK
				| (((position1 - 1) & EVENT_POSITION_MASK) << 2)
				| (((position2 - 1) & EVENT_POSITION_MASK) << 4)
				| (((position3 - 1) & EVENT_POSITION_MASK) << 6);

		prepareEvents(eventKey);
		return BOUNDS_IN;
	}

	void prepareEvents(int eventKey) {
		eventY0 = minPixelY & TILE_AXIS_MASK;
		eventLimit = (((maxPixelY & TILE_AXIS_MASK) + 7) << 1);
		EVENT_FILLERS[eventKey].apply();
	}
}
