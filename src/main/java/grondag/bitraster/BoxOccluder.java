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

import static grondag.bitraster.Constants.CAMERA_PRECISION_BITS;
import static grondag.bitraster.Constants.CAMERA_PRECISION_UNITY;
import static grondag.bitraster.Constants.DOWN;
import static grondag.bitraster.Constants.EAST;
import static grondag.bitraster.Constants.EMPTY_BITS;
import static grondag.bitraster.Constants.NORTH;
import static grondag.bitraster.Constants.SOUTH;
import static grondag.bitraster.Constants.TILE_COUNT;
import static grondag.bitraster.Constants.UP;
import static grondag.bitraster.Constants.V000;
import static grondag.bitraster.Constants.V001;
import static grondag.bitraster.Constants.V010;
import static grondag.bitraster.Constants.V011;
import static grondag.bitraster.Constants.V100;
import static grondag.bitraster.Constants.V101;
import static grondag.bitraster.Constants.V110;
import static grondag.bitraster.Constants.V111;
import static grondag.bitraster.Constants.WEST;

import java.util.function.Consumer;

public class BoxOccluder {
	/** How close face must be to trigger aggressive refresh of occlusion. */
	private static final int NEAR_RANGE = 8 << CAMERA_PRECISION_BITS;

	private final Matrix4L baseMvpMatrix = new Matrix4L();

	protected final Rasterizer raster = new Rasterizer();
	private int occluderVersion = 1;
	private final BoxTest[] boxTests = new BoxTest[128];
	private final BoxDraw[] boxDraws = new BoxDraw[128];
	private long viewX;
	private long viewY;
	private long viewZ;
	// Add these to region-relative box coordinates to get camera-relative coordinates
	// They are in camera fixed precision.
	private int offsetX;
	private int offsetY;
	private int offsetZ;
	private int occlusionRange;
	private int regionSquaredChunkDist;
	private int viewVersion = -1;
	private int regionVersion = -1;
	private volatile boolean forceRedraw = false;
	private boolean needsRedraw = false;
	private int maxSquaredChunkDistance;
	private boolean hasNearOccluders = false;

	@Override
	public final String toString() {
		return String.format("OccluderVersion:%d  viewX:%d  viewY:%d  viewZ:%d  offsetX:%d  offsetY:%d  offsetZ:%d viewVersion:%d  regionVersion:%d  forceRedraw:%b  needsRedraw:%b  matrix:%s",
				occluderVersion, viewX, viewY, viewZ, offsetX, offsetY, offsetZ, viewVersion, regionVersion, forceRedraw, needsRedraw, raster.mvpMatrix.toString());
	}

	{
		boxTests[0] = (x0, y0, z0, x1, y1, z1) -> {
			return false;
		};

		boxTests[UP] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			return raster.testQuad(V110, V010, V011, V111);
		};

		boxTests[DOWN] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			return raster.testQuad(V000, V100, V101, V001);
		};

		boxTests[EAST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			return raster.testQuad(V101, V100, V110, V111);
		};

		boxTests[WEST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			return raster.testQuad(V000, V001, V011, V010);
		};

		boxTests[NORTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V110, x1, y1, z0);
			return raster.testQuad(V100, V000, V010, V110);
		};

		boxTests[SOUTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V111, x1, y1, z1);
			return raster.testQuad(V001, V101, V111, V011);
		};

		// NB: Split across two quads to give more evenly-sized test regions vs potentially one big and one very small
		boxTests[UP | EAST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			return raster.testQuad(V010, V011, V111, V101)
					|| raster.testQuad(V101, V100, V110, V010);
		};

		boxTests[UP | WEST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			return raster.testQuad(V111, V110, V010, V000)
					|| raster.testQuad(V000, V001, V011, V111);
		};

		boxTests[UP | NORTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			return raster.testQuad(V011, V111, V110, V100)
					|| raster.testQuad(V100, V000, V010, V011);
		};

		boxTests[UP | SOUTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			return raster.testQuad(V110, V010, V011, V001)
					|| raster.testQuad(V001, V101, V111, V110);
		};

		boxTests[DOWN | EAST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			return raster.testQuad(V001, V000, V100, V110)
					|| raster.testQuad(V110, V111, V101, V001);
		};

		boxTests[DOWN | WEST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			return raster.testQuad(V100, V101, V001, V011)
					|| raster.testQuad(V011, V010, V000, V100);
		};

		boxTests[DOWN | NORTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			return raster.testQuad(V101, V001, V000, V010)
					|| raster.testQuad(V010, V110, V100, V101);
		};

		boxTests[DOWN | SOUTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V111, x1, y1, z1);
			return raster.testQuad(V000, V100, V101, V111)
					|| raster.testQuad(V111, V011, V001, V000);
		};

		boxTests[NORTH | EAST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			return raster.testQuad(V000, V010, V110, V111)
					|| raster.testQuad(V111, V101, V100, V000);
		};

		boxTests[NORTH | WEST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V110, x1, y1, z0);
			return raster.testQuad(V110, V100, V000, V001)
					|| raster.testQuad(V001, V011, V010, V110);
		};

		boxTests[SOUTH | EAST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			return raster.testQuad(V011, V001, V101, V100)
					|| raster.testQuad(V100, V110, V111, V011);
		};

		boxTests[SOUTH | WEST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V111, x1, y1, z1);
			return raster.testQuad(V101, V111, V011, V010)
					|| raster.testQuad(V010, V000, V001, V101);
		};

		// NB: When three faces are visible, omit nearest vertex and draw two quads instead of three.

		boxTests[UP | EAST | NORTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V111, x1, y1, z1);
			return raster.testQuad(V011, V111, V101, V100)
					|| raster.testQuad(V100, V000, V010, V011);
		};

		boxTests[UP | WEST | NORTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			return raster.testQuad(V111, V110, V100, V000)
					|| raster.testQuad(V000, V001, V011, V111);
		};

		boxTests[UP | EAST | SOUTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			return raster.testQuad(V010, V011, V001, V101)
					|| raster.testQuad(V101, V100, V110, V010);
		};

		boxTests[UP | WEST | SOUTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			return raster.testQuad(V110, V010, V000, V001)
					|| raster.testQuad(V001, V101, V111, V110);
		};

		boxTests[DOWN | EAST | NORTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			return raster.testQuad(V001, V000, V010, V110)
					|| raster.testQuad(V110, V111, V101, V001);
		};

		boxTests[DOWN | WEST | NORTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			return raster.testQuad(V101, V001, V011, V010)
					|| raster.testQuad(V010, V110, V100, V101);
		};

		boxTests[DOWN | EAST | SOUTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			return raster.testQuad(V000, V100, V110, V111)
					|| raster.testQuad(V111, V011, V001, V000);
		};

		boxTests[DOWN | WEST | SOUTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V111, x1, y1, z1);
			return raster.testQuad(V100, V101, V111, V011)
					|| raster.testQuad(V011, V010, V000, V100);
		};

		////

		boxDraws[0] = (x0, y0, z0, x1, y1, z1) -> {
			// NOOP
		};

		boxDraws[UP] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			raster.drawQuad(V110, V010, V011, V111);
		};

		boxDraws[DOWN] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.drawQuad(V000, V100, V101, V001);
		};

		boxDraws[EAST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			raster.drawQuad(V101, V100, V110, V111);
		};

		boxDraws[WEST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.drawQuad(V000, V001, V011, V010);
		};

		boxDraws[NORTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V110, x1, y1, z0);
			raster.drawQuad(V100, V000, V010, V110);
		};

		boxDraws[SOUTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V111, x1, y1, z1);
			raster.drawQuad(V001, V101, V111, V011);
		};

		// NB: Split across two quads to give more evenly-sized test regions vs potentially one big and one very small
		boxDraws[UP | EAST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			raster.drawQuad(V010, V011, V111, V101);
			raster.drawQuad(V101, V100, V110, V010);
		};

		boxDraws[UP | WEST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			raster.drawQuad(V111, V110, V010, V000);
			raster.drawQuad(V000, V001, V011, V111);
		};

		boxDraws[UP | NORTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			raster.drawQuad(V011, V111, V110, V100);
			raster.drawQuad(V100, V000, V010, V011);
		};

		boxDraws[UP | SOUTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			raster.drawQuad(V110, V010, V011, V001);
			raster.drawQuad(V001, V101, V111, V110);
		};

		boxDraws[DOWN | EAST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			raster.drawQuad(V001, V000, V100, V110);
			raster.drawQuad(V110, V111, V101, V001);
		};

		boxDraws[DOWN | WEST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.drawQuad(V100, V101, V001, V011);
			raster.drawQuad(V011, V010, V000, V100);
		};

		boxDraws[DOWN | NORTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.drawQuad(V101, V001, V000, V010);
			raster.drawQuad(V010, V110, V100, V101);
		};

		boxDraws[DOWN | SOUTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V111, x1, y1, z1);
			raster.drawQuad(V000, V100, V101, V111);
			raster.drawQuad(V111, V011, V001, V000);
		};

		boxDraws[NORTH | EAST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			raster.drawQuad(V000, V010, V110, V111);
			raster.drawQuad(V111, V101, V100, V000);
		};

		boxDraws[NORTH | WEST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V110, x1, y1, z0);
			raster.drawQuad(V110, V100, V000, V001);
			raster.drawQuad(V001, V011, V010, V110);
		};

		boxDraws[SOUTH | EAST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			raster.drawQuad(V011, V001, V101, V100);
			raster.drawQuad(V100, V110, V111, V011);
		};

		boxDraws[SOUTH | WEST] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V111, x1, y1, z1);
			raster.drawQuad(V101, V111, V011, V010);
			raster.drawQuad(V010, V000, V001, V101);
		};

		// NB: When three faces are visible, omit nearest vertex and draw two quads instead of three.

		boxDraws[UP | EAST | NORTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V111, x1, y1, z1);
			raster.drawQuad(V011, V111, V101, V100);
			raster.drawQuad(V100, V000, V010, V011);
		};

		boxDraws[UP | WEST | NORTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			raster.drawQuad(V111, V110, V100, V000);
			raster.drawQuad(V000, V001, V011, V111);
		};

		boxDraws[UP | EAST | SOUTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.drawQuad(V010, V011, V001, V101);
			raster.drawQuad(V101, V100, V110, V010);
		};

		boxDraws[UP | WEST | SOUTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			raster.drawQuad(V110, V010, V000, V001);
			raster.drawQuad(V001, V101, V111, V110);
		};

		boxDraws[DOWN | EAST | NORTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			raster.drawQuad(V001, V000, V010, V110);
			raster.drawQuad(V110, V111, V101, V001);
		};

		boxDraws[DOWN | WEST | NORTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V110, x1, y1, z0);
			raster.drawQuad(V101, V001, V011, V010);
			raster.drawQuad(V010, V110, V100, V101);
		};

		boxDraws[DOWN | EAST | SOUTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V001, x0, y0, z1);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V110, x1, y1, z0);
			raster.setupVertex(V111, x1, y1, z1);
			raster.drawQuad(V000, V100, V110, V111);
			raster.drawQuad(V111, V011, V001, V000);
		};

		boxDraws[DOWN | WEST | SOUTH] = (x0, y0, z0, x1, y1, z1) -> {
			raster.setupVertex(V000, x0, y0, z0);
			raster.setupVertex(V010, x0, y1, z0);
			raster.setupVertex(V011, x0, y1, z1);
			raster.setupVertex(V100, x1, y0, z0);
			raster.setupVertex(V101, x1, y0, z1);
			raster.setupVertex(V111, x1, y1, z1);
			raster.drawQuad(V100, V101, V111, V011);
			raster.drawQuad(V011, V010, V000, V100);
		};
	}

	public void copyFrom(BoxOccluder source) {
		baseMvpMatrix.copyFrom(source.baseMvpMatrix);
		raster.copyFrom(source.raster);
		viewX = source.viewX;
		viewY = source.viewY;
		viewZ = source.viewZ;

		offsetX = source.offsetX;
		offsetY = source.offsetY;
		offsetZ = source.offsetZ;

		occlusionRange = source.occlusionRange;

		viewVersion = source.viewVersion;
		regionVersion = source.regionVersion;
		occluderVersion = source.occluderVersion;
		maxSquaredChunkDistance = source.maxSquaredChunkDistance;

		forceRedraw = source.forceRedraw;
		needsRedraw = source.needsRedraw;
	}

	/**
	 * Previously tested regions can reuse test results if their version matches.
	 * However, they must still be drawn (if visible) if indicated by {@link #clearSceneIfNeeded(int, int)}.
	 */
	public final int version() {
		return occluderVersion;
	}

	/**
	 * Force update to new version.
	 */
	public final void invalidate() {
		forceRedraw = true;
	}

	public final void prepareRegion(int originX, int originY, int originZ, int occlusionRange, int squaredChunkDistance) {
		this.occlusionRange = occlusionRange;
		regionSquaredChunkDist = squaredChunkDistance;

		// PERF: could perhaps reuse CameraRelativeCenter values in BuildRenderRegion that are used by Frustum
		offsetX = (int) ((originX << CAMERA_PRECISION_BITS) - viewX);
		offsetY = (int) ((originY << CAMERA_PRECISION_BITS) - viewY);
		offsetZ = (int) ((originZ << CAMERA_PRECISION_BITS) - viewZ);

		final Matrix4L mvpMatrix = raster.mvpMatrix;
		mvpMatrix.copyFrom(baseMvpMatrix);
		mvpMatrix.translate(offsetX, offsetY, offsetZ, CAMERA_PRECISION_BITS);
	}

	/**
	 * Check if needs redrawn and prep for redraw if so.
	 * When false, regions should be drawn only if their occluder version is not current.
	 */
	public final boolean prepareScene(int viewVersion, double cameraX, double cameraY, double cameraZ, Consumer<Matrix4L> modelMatrixSetter, Consumer<Matrix4L> projectionMatrixSetter) {
		if (this.viewVersion != viewVersion) {
			final Matrix4L baseMvpMatrix = this.baseMvpMatrix;
			final Matrix4L tempMatrix = raster.mvpMatrix;

			baseMvpMatrix.loadIdentity();

			projectionMatrixSetter.accept(tempMatrix);
			baseMvpMatrix.multiply(tempMatrix);

			modelMatrixSetter.accept(tempMatrix);
			baseMvpMatrix.multiply(tempMatrix);

			viewX = Math.round(cameraX * CAMERA_PRECISION_UNITY);
			viewY = Math.round(cameraY * CAMERA_PRECISION_UNITY);
			viewZ = Math.round(cameraZ * CAMERA_PRECISION_UNITY);
		}

		if (forceRedraw || this.viewVersion != viewVersion) {
			this.viewVersion = viewVersion;
			System.arraycopy(EMPTY_BITS, 0, raster.tiles, 0, TILE_COUNT);
			forceRedraw = false;
			needsRedraw = true;
			hasNearOccluders = false;
			maxSquaredChunkDistance = 0;
			++occluderVersion;
		} else {
			needsRedraw = false;
		}

		return needsRedraw;
	}

	/**
	 * True if occlusion includes geometry within the near region.
	 * When true, simple movement distance test isn't sufficient for knowing if redraw is needed.
	*/
	public final boolean hasNearOccluders() {
		return hasNearOccluders;
	}

	public final boolean needsRedraw() {
		return needsRedraw;
	}

	final MicroTimer timer = new MicroTimer("boxTests.apply", 500000);

	// baseline
	//	[11:29:18] [Render thread/INFO] (Minecraft) [STDOUT]: Avg boxTests.apply duration = 976 ns, min = 280, max = 177222, total duration = 488, total runs = 500,000
	//	[11:29:20] [Render thread/INFO] (Minecraft) [STDOUT]: Avg boxTests.apply duration = 949 ns, min = 281, max = 47392, total duration = 474, total runs = 500,000
	//	[11:29:22] [Render thread/INFO] (Minecraft) [STDOUT]: Avg boxTests.apply duration = 962 ns, min = 293, max = 363102, total duration = 481, total runs = 500,000
	//	[11:29:24] [Render thread/INFO] (Minecraft) [STDOUT]: Avg boxTests.apply duration = 932 ns, min = 304, max = 39615, total duration = 466, total runs = 500,000
	//	[11:29:26] [Render thread/INFO] (Minecraft) [STDOUT]: Avg boxTests.apply duration = 964 ns, min = 312, max = 218238, total duration = 482, total runs = 500,000
	//	[11:29:28] [Render thread/INFO] (Minecraft) [STDOUT]: Avg boxTests.apply duration = 957 ns, min = 310, max = 4753785, total duration = 478, total runs = 500,000
	//	[11:29:29] [Render thread/INFO] (Minecraft) [STDOUT]: Avg boxTests.apply duration = 912 ns, min = 290, max = 104227, total duration = 456, total runs = 500,000
	//	[11:29:31] [Render thread/INFO] (Minecraft) [STDOUT]: Avg boxTests.apply duration = 910 ns, min = 303, max = 69905, total duration = 455, total runs = 500,000
	//	[11:29:33] [Render thread/INFO] (Minecraft) [STDOUT]: Avg boxTests.apply duration = 928 ns, min = 298, max = 100685, total duration = 464, total runs = 500,000
	//	[11:29:35] [Render thread/INFO] (Minecraft) [STDOUT]: Avg boxTests.apply duration = 899 ns, min = 296, max = 170822, total duration = 449, total runs = 500,000


	/**
	 * Does not rely on winding order but instead the distance from
	 * plane with known facing to camera position.
	 */
	public final boolean isBoxVisible(int packedBox) {
		final int x0 = PackedBox.x0(packedBox) - 1;
		final int y0 = PackedBox.y0(packedBox) - 1;
		final int z0 = PackedBox.z0(packedBox) - 1;
		final int x1 = PackedBox.x1(packedBox) + 1;
		final int y1 = PackedBox.y1(packedBox) + 1;
		final int z1 = PackedBox.z1(packedBox) + 1;

		final int offsetX = this.offsetX;
		final int offsetY = this.offsetY;
		final int offsetZ = this.offsetZ;

		int outcome = 0;

		// if camera below top face can't be seen
		if (offsetY < -(y1 << CAMERA_PRECISION_BITS)) {
			outcome |= UP;
		} else if (offsetY > -(y0 << CAMERA_PRECISION_BITS)) {
			outcome |= DOWN;
		}

		if (offsetX < -(x1 << CAMERA_PRECISION_BITS)) {
			outcome |= EAST;
		} else if (offsetX > -(x0 << CAMERA_PRECISION_BITS)) {
			outcome |= WEST;
		}

		if (offsetZ < -(z1 << CAMERA_PRECISION_BITS)) {
			outcome |= SOUTH;
		} else if (offsetZ > -(z0 << CAMERA_PRECISION_BITS)) {
			outcome |= NORTH;
		}

		timer.start();
		final boolean result = boxTests[outcome].apply(x0, y0, z0, x1, y1, z1);
		timer.stop();

		return result;
		//return boxTests[outcome].apply(x0, y0, z0, x1, y1, z1);
	}

	public final boolean isEmptyRegionVisible(int originX, int originY, int originZ) {
		prepareRegion(originX, originY, originZ, 0, 0);
		return isBoxVisible(PackedBox.FULL_BOX);
	}

	/**
	 * Does not rely on winding order but instead the distance from
	 * plane with known facing to camera position.
	 */
	private void occludeInner(int packedBox) {
		final int x0 = PackedBox.x0(packedBox);
		final int y0 = PackedBox.y0(packedBox);
		final int z0 = PackedBox.z0(packedBox);
		final int x1 = PackedBox.x1(packedBox);
		final int y1 = PackedBox.y1(packedBox);
		final int z1 = PackedBox.z1(packedBox);

		final int offsetX = this.offsetX;
		final int offsetY = this.offsetY;
		final int offsetZ = this.offsetZ;

		int outcome = 0;

		boolean hasNear = true;

		final int top = (y1 << CAMERA_PRECISION_BITS) + offsetY;

		// NB: entirely possible for neither top or bottom to be visible.
		// This happens when camera is between them.

		if (top < 0) {
			// camera above top face
			outcome |= UP;
			hasNear &= top > -NEAR_RANGE;
		} else {
			final int bottom = (y0 << CAMERA_PRECISION_BITS) + offsetY;

			if (bottom > 0) {
				// camera below bottom face
				outcome |= DOWN;
				hasNear &= bottom < NEAR_RANGE;
			}
		}

		final int east = (x1 << CAMERA_PRECISION_BITS) + offsetX;

		if (east < 0) {
			outcome |= EAST;
			hasNear &= east > -NEAR_RANGE;
		} else {
			final int west = (x0 << CAMERA_PRECISION_BITS) + offsetX;

			if (west > 0) {
				outcome |= WEST;
				hasNear &= west < NEAR_RANGE;
			}
		}

		final int south = (z1 << CAMERA_PRECISION_BITS) + offsetZ;

		if (south < 0) {
			outcome |= SOUTH;
			hasNear &= south > -NEAR_RANGE;
		} else {
			final int north = (z0 << CAMERA_PRECISION_BITS) + offsetZ;

			if (north > 0) {
				outcome |= NORTH;
				hasNear &= north < NEAR_RANGE;
			}
		}

		hasNearOccluders |= hasNear;

		boxDraws[outcome].apply(x0, y0, z0, x1, y1, z1);
	}

	public final void occlude(int[] visData) {
		final int occlusionRange = this.occlusionRange;
		final int limit = visData.length;

		if (limit > 1) {
			boolean updateDist = false;

			for (int i = 1; i < limit; i++) {
				final int box = visData[i];

				if (occlusionRange > PackedBox.range(box)) {
					break;
				}

				updateDist = true;
				occludeInner(box);
			}

			if (updateDist) {
				if (maxSquaredChunkDistance < regionSquaredChunkDist) {
					maxSquaredChunkDistance = regionSquaredChunkDist;
				}
			}
		}
	}

	@FunctionalInterface
	interface BoxTest {
		boolean apply(int x0, int y0, int z0, int x1, int y1, int z1);
	}

	@FunctionalInterface
	interface BoxDraw {
		void apply(int x0, int y0, int z0, int x1, int y1, int z1);
	}

	public final int maxSquaredChunkDistance() {
		return maxSquaredChunkDistance;
	}
}
