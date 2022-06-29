package org.pepfar.pdma.app.data.types;

public enum LocationAccuracy {

	ROOF_TOP(0),

	APPROXIMATE(1),

	CENTROID(2);

	private final int number;

	private LocationAccuracy(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}
}
