package grondag.bitraster.alt;

/**
 * Used to specify custom vertex layout. Memory offsets to y and z coordinates are set through
 * mOffsetY and mOffsetW, and vertex stride is given by mStride. It's possible to configure both
 * AoS and SoA layouts. Note that large strides may cause more cache misses and decrease
 * performance. It is advisable to store position data as compactly in memory as possible.
 */
public record VertexLayout(
		int stride, //!< float stride between vertices
		int offsetY, //!< float offset from X to Y coordinate
		int offsetZW //!< float offset from X to Z or W coordinate
) {
}
