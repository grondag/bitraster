package grondag.bitraster.alt;

public class Config {
	/**
	 * Configure the algorithm used for updating and merging hierarchical z buffer entries. If QUICK_MASK
	 * is defined to 1, use the algorithm from the paper "Masked Software Occlusion Culling", which has good
	 * balance between performance and low leakage. If QUICK_MASK is defined to 0, use the algorithm from
	 * "Masked Depth Culling for Graphics Hardware" which has less leakage, but also lower performance.
	 */
	public static final boolean QUICK_MASK = true;

	/**
	 * Define PRECISE_COVERAGE to 1 to more closely match GPU rasterization rules. The increased precision comes
	 * at a cost of slightly lower performance.
	 */
	public static final boolean PRECISE_COVERAGE = true;

	/**
	 * Define CLIPPING_PRESERVES_ORDER to 1 to prevent clipping from reordering triangle rasterization
	 * order; This comes at a cost (approx 3-4%) but removes one source of temporal frame-to-frame instability.
	 */
	public static final boolean CLIPPING_PRESERVES_ORDER = true;

	/**
	 * Define ENABLE_STATS to 1 to gather various statistics during occlusion culling. Can be used for profiling
	 * and debugging. Note that enabling this function will reduce performance significantly.
	 */
	public static final boolean ENABLE_STATS = false;

	enum Implementation {
		SSE2,
		SSE41,
		AVX2,
		AVX512
	}

	enum BackfaceWinding {
		BACKFACE_NONE,
		BACKFACE_CW,
		BACKFACE_CCW
	}

	public static final int CULLING_RESULT_VISIBLE = 0x0;
	public static final int CULLING_RESULT_OCCLUDED = 0x1;
	public static final int CULLING_RESULT_VIEW_CULLED = 0x3;

	public static final int CLIP_PLANE_NONE = 0x00;
	public static final int CLIP_PLANE_NEAR = 0x01;
	public static final int CLIP_PLANE_LEFT = 0x02;
	public static final int CLIP_PLANE_RIGHT = 0x04;
	public static final int CLIP_PLANE_BOTTOM = 0x08;
	public static final int CLIP_PLANE_TOP = 0x10;
	public static final int CLIP_PLANE_SIDES = (CLIP_PLANE_LEFT | CLIP_PLANE_RIGHT | CLIP_PLANE_BOTTOM | CLIP_PLANE_TOP);
	public static final int CLIP_PLANE_ALL = (CLIP_PLANE_LEFT | CLIP_PLANE_RIGHT | CLIP_PLANE_BOTTOM | CLIP_PLANE_TOP | CLIP_PLANE_NEAR);

	static final VertexLayout DEFAULT_LAYOUT = new VertexLayout(4, 1, 2);
}
