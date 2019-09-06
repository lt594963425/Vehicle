package com.tao.vehiclemvdemo.android.graphic.map;


import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;

public class GenericMarker {

	public static MarkerOptions build(LatLng coord) {
		return new MarkerOptions().position(coord).draggable(true).anchor(0.5f, 0.5f);
	}

}
