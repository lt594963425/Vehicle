package com.tao.vehiclemvdemo.android.maps.providers.mapbox;

import android.content.Context;

import com.baidu.mapapi.map.TileProvider;


/**
 * Created by Fredia Huya-Kouadio on 5/11/15.
 */
public class OfflineTileProvider implements TileProvider {

    private static final String TAG = OfflineTileProvider.class.getSimpleName();

    private final Context context;
    private final String mapboxId;
    private final String mapboxAccessToken;
    private final int maxZoomLevel;

    public OfflineTileProvider(Context context, String mapboxId, String mapboxAccessToken, int maxZoomLevel) {
        this.context = context;
        this.mapboxId = mapboxId;
        this.mapboxAccessToken = mapboxAccessToken;
        this.maxZoomLevel = maxZoomLevel;
    }

    @Override
    public int getMaxDisLevel() {
        return 0;
    }

    @Override
    public int getMinDisLevel() {
        return 0;
    }
}
