package grondag.bitraster.alt;

import static grondag.bitraster.alt.Common.SUB_TILE_WIDTH;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.IntVector;

import grondag.bitraster.alt.Config.BackfaceWinding;
import grondag.bitraster.alt.Config.Implementation;

public class MaskedOcclusionCulling {
	/**
	 * Used to control scissoring during rasterization. Note that we only provide coarse scissor support.
	 * The scissor box x coordinates must be a multiple of 32, and the y coordinates a multiple of 8.
	 * Scissoring is mainly meant as a means of enabling binning (sort middle) rasterizers in case
	 * application developers want to use that approach for multithreading.
	 */
	static record ScissorRect (
		int minX, //!< Screen space X coordinate for left side of scissor rect, inclusive and must be a multiple of 32
		int minY, //!< Screen space Y coordinate for bottom side of scissor rect, inclusive and must be a multiple of 8
		int maxX, //!< Screen space X coordinate for right side of scissor rect, <B>non</B> inclusive and must be a multiple of 32
		int maxY //!< Screen space Y coordinate for top side of scissor rect, <B>non</B> inclusive and must be a multiple of 8
	) { }

	/**
	 * Used to specify storage area for a binlist, containing triangles. This struct is used for binning
	 * and multithreading. The host application is responsible for allocating memory for the binlists.
	 */
	static record TriList (
		int numTriangles, //!< Maximum number of triangles that may be stored in mPtr
		int triIdx,       //!< Index of next triangle to be written, clear before calling BinTriangles to start from the beginning of the list
		float[] scratch         //!< Scratchpad buffer allocated by the host application
	) { }

	/**
	 * Statistics that can be gathered during occluder rendering and visibility to aid debugging
	 * and profiling. Must be enabled by changing the ENABLE_STATS define.
	 */
	static record OccluderStats (
		long numProcessedTriangles,  //!< Number of occluder triangles processed in total
		long numRasterizedTriangles, //!< Number of occluder triangles passing view frustum and backface culling
		long numTilesTraversed,      //!< Number of tiles traversed by the rasterizer
		long numTilesUpdated        //!< Number of tiles where the hierarchical z buffer was updated
	) { }

	static record OccludeeStats (
		long numProcessedRectangles, //!< Number of rects processed (TestRect())
		long numProcessedTriangles,  //!< Number of ocludee triangles processed (TestTriangles())
		long numRasterizedTriangles, //!< Number of ocludee triangle passing view frustum and backface culling
		long numTilesTraversed      //!< Number of tiles traversed by triangle & rect rasterizers
	) { }

	/**
	 * \brief Sets the resolution of the hierarchical depth buffer. This function will
	 *        re-allocate the current depth buffer (if present). The contents of the
	 *        buffer is undefined until ClearBuffer() is called.
	 * -
	 * \param witdh The width of the buffer in pixels, must be a multiple of 8
	 * \param height The height of the buffer in pixels, must be a multiple of 4
	 */
	void setResolution(int width, int height) {
		//
	}

	/**
	* \brief Gets the resolution of the hierarchical depth buffer.
	* -
	* \param witdh Output: The width of the buffer in pixels
	* \param height Output: The height of the buffer in pixels
	*/
	int getWidth() {
		return 0;
	}

	/**
	* \brief Gets the resolution of the hierarchical depth buffer.
	* -
	* \param witdh Output: The width of the buffer in pixels
	* \param height Output: The height of the buffer in pixels
	*/
	int getHeight() {
		return 0;
	}

	/**
	 * \brief Returns the tile size for the current implementation.
	 *
	 * \param nBinsW Number of vertical bins, the screen is divided into nBinsW x nBinsH
	 *        rectangular bins.
	 * \param nBinsH Number of horizontal bins, the screen is divided into nBinsW x nBinsH
	 *        rectangular bins.
	 * \param outBinWidth Output: The width of the single bin in pixels (except for the
	 *        rightmost bin width, which is extended to resolution width)
	 * \param outBinHeight Output: The height of the single bin in pixels (except for the
	 *        bottommost bin height, which is extended to resolution height)
	 */
	/**
	 *
	 * @return width in 2 LSB and height in 2 MSB
	 */
	int computeBinWidthHeight(int nBinsW, int nBinsH) {
		return 0;
	}

	/**
	 * Sets the distance for the near clipping plane. Default is nearDist = 0.
	 * -
	 * \param nearDist The distance to the near clipping plane, given as clip space w
	 */
	void getNearClipPlane(float nearDist) {
		//
	}

	/**
	* Gets the distance for the near clipping plane.
	*/
	float getNearClipPlane() {
		return 0;
	}

	/**
	 * Clears the hierarchical depth buffer.
	 */
	void clearBuffer() {
		//
	}

	/**
	 * Renders a mesh of occluder triangles and updates the hierarchical z buffer
	 *        with conservative depth values.
	 * -
	 * This function is optimized for vertex layouts with stride 16 and y and w
	 * offsets of 4 and 12 bytes, respectively.
	 * -
	 * \param inVtx Pointer to an array of input vertices, should point to the x component
	 *        of the first vertex. The input vertices are given as (x,y,w) coordinates
	 *        in clip space. The memory layout can be changed using vtxLayout.
	 * \param inTris Pointer to an array of vertex indices. Each triangle is created
	 *        from three indices consecutively fetched from the array.
	 * \param nTris The number of triangles to render (inTris must contain atleast 3*nTris
	 *        entries)
	 * \param modelToClipMatrix all vertices will be transformed by this matrix before
	 *        performing projection. If nullptr is passed the transform step will be skipped
	 * \param bfWinding Sets triangle winding order to consider backfacing, must be one one
	 *        of (BACKFACE_NONE, BACKFACE_CW and BACKFACE_CCW). Back-facing triangles are culled
	 *        and will not be rasterized. You may use BACKFACE_NONE to disable culling for
	 *        double sided geometry
	 * \param clipPlaneMask A mask indicating which clip planes should be considered by the
	 *        triangle clipper. Can be used as an optimization if your application can
	 *        determine (for example during culling) that a group of triangles does not
	 *        intersect a certain frustum plane. However, setting an incorrect mask may
	 *        cause out of bounds memory accesses.
	 * \param vtxLayout A struct specifying the vertex layout (see struct for detailed
	 *        description). For best performance, it is advisable to store position data
	 *        as compactly in memory as possible.
	 * \return Will return VIEW_CULLED if all triangles are either outside the frustum or
	 *         backface culled, returns VISIBLE otherwise.
	 */
	int RenderTriangles(float[] inVtx, int[] inTris, int nTris,
			float[] modelToClipMatrix, BackfaceWinding bfWinding, int clipPlaneMask, VertexLayout vtxLayout
	) {
		return 0;
	}

	/**
	 * Occlusion query for a rectangle with a given depth. The rectangle is given
	 *        in normalized device coordinates where (x,y) coordinates between [-1,1] map
	 *        to the visible screen area. The query uses a GREATER_EQUAL (reversed) depth
	 *        test meaning that depth values equal to the contents of the depth buffer are
	 *        counted as visible.
	 * -
	 * \param xmin NDC coordinate of the left side of the rectangle.
	 * \param ymin NDC coordinate of the bottom side of the rectangle.
	 * \param xmax NDC coordinate of the right side of the rectangle.
	 * \param ymax NDC coordinate of the top side of the rectangle.
	 * \param ymax NDC coordinate of the top side of the rectangle.
	 * \param wmin Clip space W coordinate for the rectangle.
	 * \return The query will return VISIBLE if the rectangle may be visible, OCCLUDED
	 *         if the rectangle is occluded by a previously rendered  object, or VIEW_CULLED
	 *         if the rectangle is outside the view frustum.
	 */
	int testRect(float xmin, float ymin, float xmax, float ymax, float wmin) {
		return 0;
	}

	/**
	 * This function is similar to RenderTriangles(), but performs an occlusion
	 *        query instead and does not update the hierarchical z buffer. The query uses
	 *        a GREATER_EQUAL (reversed) depth test meaning that depth values equal to the
	 *        contents of the depth buffer are counted as visible.
	 * -
	 * This function is optimized for vertex layouts with stride 16 and y and w
	 * offsets of 4 and 12 bytes, respectively.
	 * -
	 * \param inVtx Pointer to an array of input vertices, should point to the x component
	 *        of the first vertex. The input vertices are given as (x,y,w) coordinates
	 *        in clip space. The memory layout can be changed using vtxLayout.
	 * \param inTris Pointer to an array of triangle indices. Each triangle is created
	 *        from three indices consecutively fetched from the array.
	 * \param nTris The number of triangles to render (inTris must contain atleast 3*nTris
	 *        entries)
	 * \param modelToClipMatrix all vertices will be transformed by this matrix before
	 *        performing projection. If nullptr is passed the transform step will be skipped
	 * \param bfWinding Sets triangle winding order to consider backfacing, must be one one
	 *        of (BACKFACE_NONE, BACKFACE_CW and BACKFACE_CCW). Back-facing triangles are culled
	 *        and will not be occlusion tested. You may use BACKFACE_NONE to disable culling
	 *        for double sided geometry
	 * \param clipPlaneMask A mask indicating which clip planes should be considered by the
	 *        triangle clipper. Can be used as an optimization if your application can
	 *        determine (for example during culling) that a group of triangles does not
	 *        intersect a certain frustum plane. However, setting an incorrect mask may
	 *        cause out of bounds memory accesses.
	 * \param vtxLayout A struct specifying the vertex layout (see struct for detailed
	 *        description). For best performance, it is advisable to store position data
	 *        as compactly in memory as possible.
	 * \return The query will return VISIBLE if the triangle mesh may be visible, OCCLUDED
	 *         if the mesh is occluded by a previously rendered object, or VIEW_CULLED if all
	 *         triangles are entirely outside the view frustum or backface culled.
	 */
	int testTriangles(float[] inVtx, int[] inTris, int nTris, float[] modelToClipMatrix, BackfaceWinding bfWinding, int clipPlaneMask, VertexLayout vtxLayout) {
		return 0;
	}

	/**
	 * Perform input assembly, clipping , projection, triangle setup, and write
	 *        triangles to the screen space bins they overlap. This function can be used to
	 *        distribute work for threading (See the CullingThreadpool class for an example)
	 * -
	 * \param inVtx Pointer to an array of input vertices, should point to the x component
	 *        of the first vertex. The input vertices are given as (x,y,w) coordinates
	 *        in clip space. The memory layout can be changed using vtxLayout.
	 * \param inTris Pointer to an array of vertex indices. Each triangle is created
	 *        from three indices consecutively fetched from the array.
	 * \param nTris The number of triangles to render (inTris must contain atleast 3*nTris
	 *        entries)
	 * \param triLists Pointer to an array of TriList objects with one TriList object per
	 *        bin. If a triangle overlaps a bin, it will be written to the corresponding
	 *        trilist. Note that this method appends the triangles to the current list, to
	 *        start writing from the beginning of the list, set triList.mTriIdx = 0
	 * \param nBinsW Number of vertical bins, the screen is divided into nBinsW x nBinsH
	 *        rectangular bins.
	 * \param nBinsH Number of horizontal bins, the screen is divided into nBinsW x nBinsH
	 *        rectangular bins.
	 * \param modelToClipMatrix all vertices will be transformed by this matrix before
	 *        performing projection. If nullptr is passed the transform step will be skipped
	 * \param clipPlaneMask A mask indicating which clip planes should be considered by the
	 *        triangle clipper. Can be used as an optimization if your application can
	 *        determine (for example during culling) that a group of triangles does not
	 *        intersect a certain frustum plane. However, setting an incorrect mask may
	 *        cause out of bounds memory accesses.
	 * \param vtxLayout A struct specifying the vertex layout (see struct for detailed
	 *        description). For best performance, it is advisable to store position data
	 *        as compactly in memory as possible.
	 * \param bfWinding Sets triangle winding order to consider backfacing, must be one one
	 *        of (BACKFACE_NONE, BACKFACE_CW and BACKFACE_CCW). Back-facing triangles are culled
	 *        and will not be binned / rasterized. You may use BACKFACE_NONE to disable culling
	 *        for double sided geometry
	 */
	void binTriangles(float[] inVtx, int[] inTris, int nTris, TriList triLists, int nBinsW, int nBinsH, float[] modelToClipMatrix, BackfaceWinding bfWinding, int clipPlaneMask, VertexLayout vtxLayout) {
		//
	}

	/**
	 * Renders all occluder triangles in a trilist. This function can be used in
	 *        combination with BinTriangles() to create a threded (binning) rasterizer. The
	 *        bins can be processed independently by different threads without risking writing
	 *        to overlapping memory regions.
	 * -
	 * \param triLists A triangle list, filled using the BinTriangles() function that is to
	 *        be rendered.
	 * \param scissor A scissor box limiting the rendering region to the bin. The size of each
	 *        bin must be a multiple of 32x8 pixels due to implementation constraints. For a
	 *        render target with (width, height) resolution and (nBinsW, nBinsH) bins, the
	 *        size of a bin is:
	 *          binWidth = (width / nBinsW) - (width / nBinsW) % 32;
	 *          binHeight = (height / nBinsH) - (height / nBinsH) % 8;
	 *        The last row and column of tiles have a different size:
	 *          lastColBinWidth = width - (nBinsW-1)*binWidth;
	 *          lastRowBinHeight = height - (nBinsH-1)*binHeight;
	 */
	void renderTrilist(TriList triList, ScissorRect scissor) {
		//
	}

	/**
	 * Creates a per-pixel depth buffer from the hierarchical z buffer representation.
	 *        Intended for visualizing the hierarchical depth buffer for debugging. The
	 *        buffer is written in scanline order, from the top to bottom (D3D) or bottom to
	 *        top (OGL) of the surface. See the USE_D3D define.
	 * -
	 * \param depthData Pointer to memory where the per-pixel depth data is written. Must
	 *        hold storage for atleast width*height elements as set by setResolution.
	 */
	void computePixelDepthBuffer(float[] depthData, boolean flipY) {
		//
	}

	/**
	 * Fetch occlusion culling statistics, returns zeroes if ENABLE_STATS define is
	 *        not defined. The statistics can be used for profiling or debugging.
	 */
	OccluderStats getOccluderStatistics () {
		return null;
	}

	/**
	 * Fetch occlusion culling statistics, returns zeroes if ENABLE_STATS define is
	 *        not defined. The statistics can be used for profiling or debugging.
	 */
	OccludeeStats getOccludeeStatistics () {
		return null;
	}

	/**
	 * Returns the implementation (CPU instruction set) version of this object.
	 */
	Implementation getImplementation() {
		return null;
	}

	/**
	 * Utility function for transforming vertices and outputting them to an (x,y,z,w)
	 *        format suitable for the occluder rasterization and occludee testing functions.
	 * \param mtx Pointer to matrix data. The matrix should column major for post
	 *        multiplication (OGL) and row major for pre-multiplication (DX). This is
	 *        consistent with OpenGL / DirectX behavior.
	 * \param inVtx Pointer to an array of input vertices. The input vertices are given as
	 *        (x,y,z) coordinates. The memory layout can be changed using vtxLayout.
	 * \param xfVtx Pointer to an array to store transformed vertices. The transformed
	 *        vertices are always stored as array of structs (AoS) (x,y,z,w) packed in memory.
	 * \param nVtx Number of vertices to transform.
	 * \param vtxLayout A struct specifying the vertex layout (see struct for detailed
	 *        description). For best performance, it is advisable to store position data
	 *        as compactly in memory as possible. Note that for this function, the
	 *        w-component is assumed to be 1.0.
	 */
	static void transformVertices(float[] mtx, float[] inVtx, float[] xfVtx, int nVtx, VertexLayout vtxLayout) {
		// This function pretty slow, about 10-20% slower than if the vertices are stored in aligned SOA form.
		if (nVtx == 0) {
			return;
		}

		// Load matrix and swizzle out the z component. For post-multiplication (OGL), the matrix is assumed to be column
		// major, with one column per SSE register. For pre-multiplication (DX), the matrix is assumed to be row major.
		FloatVector mtxCol0 = FloatVector.fromArray(FloatVector.SPECIES_128, mtx, 0);
		FloatVector mtxCol1 = FloatVector.fromArray(FloatVector.SPECIES_128, mtx, 4);
		FloatVector mtxCol2 = FloatVector.fromArray(FloatVector.SPECIES_128, mtx, 8);
		FloatVector mtxCol3 = FloatVector.fromArray(FloatVector.SPECIES_128, mtx, 12);
		//		__m128 mtxCol0 = _mm_loadu_ps(mtx);
		//		__m128 mtxCol1 = _mm_loadu_ps(mtx + 4);
		//		__m128 mtxCol2 = _mm_loadu_ps(mtx + 8);
		//		__m128 mtxCol3 = _mm_loadu_ps(mtx + 12);

		int stride = vtxLayout.stride();
		int inIndex = 0;
		//		const char *vPtr = (const char *)inVtx;
		//float outPtr = xfVtx;
		int outIndex = 0;

		// Iterate through all vertices and transform
		for (int vtx = 0; vtx < nVtx; ++vtx) {
			FloatVector xVal = FloatVector.broadcast(FloatVector.SPECIES_128, inVtx[inIndex]);
			FloatVector yVal = FloatVector.broadcast(FloatVector.SPECIES_128, inVtx[inIndex + vtxLayout.offsetY()]);
			FloatVector zVal = FloatVector.broadcast(FloatVector.SPECIES_128, inVtx[inIndex + vtxLayout.offsetZW()]);
			//			__m128 xVal = _mm_load1_ps((float*)(vPtr));
			//			__m128 yVal = _mm_load1_ps((float*)(vPtr + vtxLayout.mOffsetY));
			//			__m128 zVal = _mm_load1_ps((float*)(vPtr + vtxLayout.mOffsetZ));

			FloatVector xform = mtxCol0.mul(xVal).add(mtxCol1.mul(yVal)).add(mtxCol2.mul(zVal)).add(mtxCol3);
			//__m128 xform = _mm_add_ps(_mm_mul_ps(mtxCol0, xVal), _mm_add_ps(_mm_mul_ps(mtxCol1, yVal), _mm_add_ps(_mm_mul_ps(mtxCol2, zVal), mtxCol3)));
			//_mm_storeu_ps(outPtr, xform);
			xform.intoArray(xfVtx, outIndex);
			inIndex += stride;
			outIndex += 4;
		}
	}

	private static IntVector _mm_setr_epi32(int i0, int i1, int i2, int i3) {
		int[] array = new int[4];
		array[3] = i0;
		array[2] = i1;
		array[1] = i2;
		array[0] = i3;

		return (IntVector) IntVector.SPECIES_128.fromArray(array, 0);
	}

	//#define SIMD_LANE_IDX _mm_setr_epi32(0, 1, 2, 3)
	private static final IntVector SIMD_LANE_IDX = _mm_setr_epi32(0, 1, 2, 3);

	//	#define SIMD_SUB_TILE_COL_OFFSET _mm_setr_epi32(0, SUB_TILE_WIDTH, SUB_TILE_WIDTH * 2, SUB_TILE_WIDTH * 3)
	private static final IntVector SIMD_SUB_TILE_COL_OFFSET = _mm_setr_epi32(0, SUB_TILE_WIDTH, SUB_TILE_WIDTH * 2, SUB_TILE_WIDTH * 3);

	//	#define SIMD_SUB_TILE_ROW_OFFSET _mm_setzero_si128()
	//	#define SIMD_SUB_TILE_COL_OFFSET_F _mm_setr_ps(0, SUB_TILE_WIDTH, SUB_TILE_WIDTH * 2, SUB_TILE_WIDTH * 3)
	//	#define SIMD_SUB_TILE_ROW_OFFSET_F _mm_setzero_ps()

	//	#define SIMD_LANE_YCOORD_I _mm_setr_epi32(128, 384, 640, 896)
	//	#define SIMD_LANE_YCOORD_F _mm_setr_ps(128.0f, 384.0f, 640.0f, 896.0f)
}
