package com.tao.vehiclemvdemo.android.maps.providers.mapbox.offline;


public interface MapDownloaderListener {

    void stateChanged(MapDownloader.OfflineMapDownloaderState newState);
    void initialCountOfFiles(int numberOfFiles);
    void progressUpdate(int numberOfFilesWritten, int numberOfFilesExcepted);
    void networkConnectivityError(Throwable error);
    void sqlLiteError(Throwable error);
    void httpStatusError(int status, String url);
    void completionOfOfflineDatabaseMap();

}
