package com.tao.vehiclemvdemo.android.proxy.mission.item.markers;

import com.tao.vehiclemvdemo.R;
import com.tao.vehiclemvdemo.android.proxy.mission.item.MissionItemProxy;

/**
 * This implements the marker source for a waypoint mission item.
 */
class WaypointMarkerInfo extends MissionItemMarkerInfo {

	protected WaypointMarkerInfo(MissionItemProxy origin) {
		super(origin);
	}

	@Override
	protected int getSelectedIconResource() {
		return R.drawable.ic_wp_map_selected;
	}

	@Override
	protected int getIconResource() {
		return R.drawable.ic_wp_map;
	}

}
