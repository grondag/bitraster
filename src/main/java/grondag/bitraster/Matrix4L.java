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

public class Matrix4L {
	public static final int MATRIX_PRECISION_BITS = 16;
	public static final long MATRIX_PRECISION_UNITY = 1 << MATRIX_PRECISION_BITS;
	public static final long MATRIX_PRECISION_HALF = MATRIX_PRECISION_UNITY / 2;
	public static final float FLOAT_CONVERSION = 1f / MATRIX_PRECISION_UNITY;

	private long a00;
	private long a01;
	private long a02;
	private long a03;
	private long a10;
	private long a11;
	private long a12;
	private long a13;
	private long a20;
	private long a21;
	private long a22;
	private long a23;
	private long a30;
	private long a31;
	private long a32;
	private long a33;

	@Override
	public String toString() {
		return String.format("[%d, %d, %d, %d], [%d, %d, %d, %d], [%d, %d, %d, %d], [%d, %d, %d, %d]",
			a00, a01, a02, a03, a10, a11, a12, a13, a20, a21, a22, a23, a30, a31, a32, a33);
	}

	public void set(
			float a00, float a01, float a02, float a03,
			float a10, float a11, float a12, float a13,
			float a20, float a21, float a22, float a23,
			float a30, float a31, float a32, float a33
	) {
		this.a00 = Math.round(a00 * MATRIX_PRECISION_UNITY);
		this.a01 = Math.round(a01 * MATRIX_PRECISION_UNITY);
		this.a02 = Math.round(a02 * MATRIX_PRECISION_UNITY);
		this.a03 = Math.round(a03 * MATRIX_PRECISION_UNITY);

		this.a10 = Math.round(a10 * MATRIX_PRECISION_UNITY);
		this.a11 = Math.round(a11 * MATRIX_PRECISION_UNITY);
		this.a12 = Math.round(a12 * MATRIX_PRECISION_UNITY);
		this.a13 = Math.round(a13 * MATRIX_PRECISION_UNITY);

		this.a20 = Math.round(a20 * MATRIX_PRECISION_UNITY);
		this.a21 = Math.round(a21 * MATRIX_PRECISION_UNITY);
		this.a22 = Math.round(a22 * MATRIX_PRECISION_UNITY);
		this.a23 = Math.round(a23 * MATRIX_PRECISION_UNITY);

		this.a30 = Math.round(a30 * MATRIX_PRECISION_UNITY);
		this.a31 = Math.round(a31 * MATRIX_PRECISION_UNITY);
		this.a32 = Math.round(a32 * MATRIX_PRECISION_UNITY);
		this.a33 = Math.round(a33 * MATRIX_PRECISION_UNITY);
	}

	public void copyFrom(Matrix4L other) {
		a00 = other.a00;
		a01 = other.a01;
		a02 = other.a02;
		a03 = other.a03;

		a10 = other.a10;
		a11 = other.a11;
		a12 = other.a12;
		a13 = other.a13;

		a20 = other.a20;
		a21 = other.a21;
		a22 = other.a22;
		a23 = other.a23;

		a30 = other.a30;
		a31 = other.a31;
		a32 = other.a32;
		a33 = other.a33;
	}

	public boolean matches(Matrix4L other) {
		return a00 == other.a00
				&& a01 == other.a01
				&& a02 == other.a02
				&& a03 == other.a03

				&& a10 == other.a10
				&& a11 == other.a11
				&& a12 == other.a12
				&& a13 == other.a13

				&& a20 == other.a20
				&& a21 == other.a21
				&& a22 == other.a22
				&& a23 == other.a23

				&& a30 == other.a30
				&& a31 == other.a31
				&& a32 == other.a32
				&& a33 == other.a33;
	}

	public void loadIdentity() {
		a00 = MATRIX_PRECISION_UNITY;
		a01 = 0;
		a02 = 0;
		a03 = 0;
		a10 = 0;
		a11 = MATRIX_PRECISION_UNITY;
		a12 = 0;
		a13 = 0;
		a20 = 0;
		a21 = 0;
		a22 = MATRIX_PRECISION_UNITY;
		a23 = 0;
		a30 = 0;
		a31 = 0;
		a32 = 0;
		a33 = MATRIX_PRECISION_UNITY;
	}

	public void multiply(Matrix4L other) {
		final long f = a00 * other.a00 + a01 * other.a10 + a02 * other.a20 + a03 * other.a30;
		final long g = a00 * other.a01 + a01 * other.a11 + a02 * other.a21 + a03 * other.a31;
		final long h = a00 * other.a02 + a01 * other.a12 + a02 * other.a22 + a03 * other.a32;
		final long i = a00 * other.a03 + a01 * other.a13 + a02 * other.a23 + a03 * other.a33;
		final long j = a10 * other.a00 + a11 * other.a10 + a12 * other.a20 + a13 * other.a30;
		final long k = a10 * other.a01 + a11 * other.a11 + a12 * other.a21 + a13 * other.a31;
		final long l = a10 * other.a02 + a11 * other.a12 + a12 * other.a22 + a13 * other.a32;
		final long m = a10 * other.a03 + a11 * other.a13 + a12 * other.a23 + a13 * other.a33;
		final long n = a20 * other.a00 + a21 * other.a10 + a22 * other.a20 + a23 * other.a30;
		final long o = a20 * other.a01 + a21 * other.a11 + a22 * other.a21 + a23 * other.a31;
		final long p = a20 * other.a02 + a21 * other.a12 + a22 * other.a22 + a23 * other.a32;
		final long q = a20 * other.a03 + a21 * other.a13 + a22 * other.a23 + a23 * other.a33;
		final long r = a30 * other.a00 + a31 * other.a10 + a32 * other.a20 + a33 * other.a30;
		final long s = a30 * other.a01 + a31 * other.a11 + a32 * other.a21 + a33 * other.a31;
		final long t = a30 * other.a02 + a31 * other.a12 + a32 * other.a22 + a33 * other.a32;
		final long u = a30 * other.a03 + a31 * other.a13 + a32 * other.a23 + a33 * other.a33;
		a00 = ((f + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
		a01 = ((g + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
		a02 = ((h + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
		a03 = ((i + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
		a10 = ((j + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
		a11 = ((k + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
		a12 = ((l + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
		a13 = ((m + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
		a20 = ((n + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
		a21 = ((o + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
		a22 = ((p + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
		a23 = ((q + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
		a30 = ((r + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
		a31 = ((s + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
		a32 = ((t + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
		a33 = ((u + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
	}

	public void translate(long x, long y, long z, int precision) {
		final int shift = MATRIX_PRECISION_BITS - precision;
		assert shift >= 0;

		final long t_a03 = x << shift;
		final long t_a13 = y << shift;
		final long t_a23 = z << shift;

		final long i = a00 * t_a03 + a01 * t_a13 + a02 * t_a23 + a03;
		final long m = a10 * t_a03 + a11 * t_a13 + a12 * t_a23 + a13;
		final long q = a20 * t_a03 + a21 * t_a13 + a22 * t_a23 + a23;
		final long u = a30 * t_a03 + a31 * t_a13 + a32 * t_a23 + a33;

		a03 += ((i + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
		a13 += ((m + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
		a23 += ((q + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
		a33 += ((u + MATRIX_PRECISION_HALF) >> MATRIX_PRECISION_BITS);
	}

	public float a00f() {
		return FLOAT_CONVERSION * a00;
	}

	public float a01f() {
		return FLOAT_CONVERSION * a01;
	}

	public float a02f() {
		return FLOAT_CONVERSION * a02;
	}

	public float a03f() {
		return FLOAT_CONVERSION * a03;
	}

	public float a10f() {
		return FLOAT_CONVERSION * a10;
	}

	public float a11f() {
		return FLOAT_CONVERSION * a11;
	}

	public float a12f() {
		return FLOAT_CONVERSION * a12;
	}

	public float a13f() {
		return FLOAT_CONVERSION * a13;
	}

	public float a20f() {
		return FLOAT_CONVERSION * a20;
	}

	public float a21f() {
		return FLOAT_CONVERSION * a21;
	}

	public float a22f() {
		return FLOAT_CONVERSION * a22;
	}

	public float a23f() {
		return FLOAT_CONVERSION * a23;
	}

	public float a30f() {
		return FLOAT_CONVERSION * a30;
	}

	public float a31f() {
		return FLOAT_CONVERSION * a31;
	}

	public float a32f() {
		return FLOAT_CONVERSION * a32;
	}

	public float a33f() {
		return FLOAT_CONVERSION * a33;
	}

	public long a00() {
		return a00;
	}

	public long a01() {
		return a01;
	}

	public long a02() {
		return a02;
	}

	public long a03() {
		return a03;
	}

	public long a10() {
		return a10;
	}

	public long a11() {
		return a11;
	}

	public long a12() {
		return a12;
	}

	public long a13() {
		return a13;
	}

	public long a20() {
		return a20;
	}

	public long a21() {
		return a21;
	}

	public long a22() {
		return a22;
	}

	public long a23() {
		return a23;
	}

	public long a30() {
		return a30;
	}

	public long a31() {
		return a31;
	}

	public long a32() {
		return a32;
	}

	public long a33() {
		return a33;
	}

	/**
	 * Computes X component of transformed vector. Assumes input W is 1.
	 * Result has standard precision.
	 */
	public long transformVec4X(int x, int y, int z) {
		return a00 * x + a01 * y + a02 * z + a03;
	}

	/**
	 * Computes Y component of transformed vector. Assumes input W is 1.
	 * Result has standard precision.
	 */
	public long transformVec4Y(int x, int y, int z) {
		return a10 * x + a11 * y + a12 * z + a13;
	}

	/**
	 * Computes Z component of transformed vector. Assumes input W is 1.
	 * Result has standard precision.
	 */
	public long transformVec4Z(int x, int y, int z) {
		return a20 * x + a21 * y + a22 * z + a23;
	}

	/**
	 * Computes W component of transformed vector. Assumes input W is 1.
	 * Result has standard precision.
	 */
	public long transformVec4W(int x, int y, int z) {
		return a30 * x + a31 * y + a32 * z + a33;
	}

	/**
	 * Computes X component of transformed vector.
	 * Result has standard precision.
	 */
	public long transformVec4X(int x, int y, int z, int w) {
		return a00 * x + a01 * y + a02 * z + a03 * w;
	}

	/**
	 * Computes Y component of transformed vector.
	 * Result has standard precision.
	 */
	public long transformVec4Y(int x, int y, int z, int w) {
		return a10 * x + a11 * y + a12 * z + a13 * w;
	}

	/**
	 * Computes Z component of transformed vector.
	 * Result has standard precision.
	 */
	public long transformVec4Z(int x, int y, int z, int w) {
		return a20 * x + a21 * y + a22 * z + a23 * w;
	}

	/**
	 * Computes W component of transformed vector.
	 * Result has standard precision.
	 */
	public long transformVec4W(int x, int y, int z, int w) {
		return a30 * x + a31 * y + a32 * z + a33 * w;
	}
}
