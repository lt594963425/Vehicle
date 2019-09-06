package com.tao.vehiclemvdemo.android.maps.providers.mapbox;


import com.baidu.mapapi.map.UrlTileProvider;

/**
 * Created by Fredia Huya-Kouadio on 5/11/15.
 */
public class MapboxTileProvider extends UrlTileProvider {

    private final static String TAG = MapboxTileProvider.class.getSimpleName();

    private final String mapboxId;
    private final String mapboxAccessToken;
    private final int maxZoomLevel;

    public MapboxTileProvider(String mapboxId, String mapboxAccessToken, int maxZoomLevel) {
        super();
        this.mapboxId = mapboxId;
        this.mapboxAccessToken = mapboxAccessToken;
        this.maxZoomLevel = maxZoomLevel;
    }



    public String getMapboxAccessToken() {
        return mapboxAccessToken;
    }

    public String getMapboxId() {
        return mapboxId;
    }

    @Override
    public String getTileUrl() {
        return null;
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
