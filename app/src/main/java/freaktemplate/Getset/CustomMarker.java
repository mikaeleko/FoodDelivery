package freaktemplate.Getset;

public class CustomMarker {

	private final String id;
	private final Double latitude;
	private final Double longitude;

	public CustomMarker(String id, Double latitude, Double longitude) {

		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
	}



	public String getCustomMarkerId() {
		return id;
	}


	public Double getCustomMarkerLatitude() {
		return latitude;
	}


	public Double getCustomMarkerLongitude() {
		return longitude;
	}

}
