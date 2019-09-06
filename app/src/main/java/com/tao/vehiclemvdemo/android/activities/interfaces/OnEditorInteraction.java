package com.tao.vehiclemvdemo.android.activities.interfaces;

import com.o3dr.services.android.lib.coordinate.LatLong;
import com.tao.vehiclemvdemo.android.proxy.mission.item.MissionItemProxy;


public interface OnEditorInteraction {
	void onItemClick(MissionItemProxy item, boolean zoomToFit);

	void onMapClick(LatLong coord);

	void onListVisibilityChanged();
}
