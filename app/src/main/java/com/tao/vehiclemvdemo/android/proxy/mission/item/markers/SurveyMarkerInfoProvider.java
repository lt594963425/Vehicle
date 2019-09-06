package com.tao.vehiclemvdemo.android.proxy.mission.item.markers;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.o3dr.services.android.lib.drone.mission.item.complex.Survey;
import com.tao.vehiclemvdemo.android.maps.MarkerInfo;
import com.tao.vehiclemvdemo.android.proxy.mission.item.MissionItemProxy;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class SurveyMarkerInfoProvider {

	private final Survey mSurvey;
	protected final MissionItemProxy markerOrigin;
	private final List<MarkerInfo> mPolygonMarkers = new ArrayList<MarkerInfo>();

	protected SurveyMarkerInfoProvider(MissionItemProxy origin) {
		this.markerOrigin = origin;
		mSurvey = (Survey) origin.getMissionItem();
		updateMarkerInfoList();
	}

	private void updateMarkerInfoList() {
        List<LatLong> points = mSurvey.getPolygonPoints();
        if(points != null) {
            final int pointsCount = points.size();
            for (int i = 0; i < pointsCount; i++) {
                mPolygonMarkers.add(new PolygonMarkerInfo(points.get(i), markerOrigin, mSurvey, i));
            }
        }
	}

	public List<MarkerInfo> getMarkersInfos() {
		return mPolygonMarkers;
	}

	public MissionItemProxy getMarkerOrigin() {
		return markerOrigin;
	}
}
