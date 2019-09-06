package com.tao.vehiclemvdemo.android.data;

import android.content.Context;
import android.text.TextUtils;


import com.tao.vehiclemvdemo.android.maps.providers.mapbox.offline.db.OfflineDatabaseHandler;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Fredia Huya-Kouadio on 5/13/15.
 */
public class DatabaseState {

    private static final ConcurrentHashMap<String, OfflineDatabaseHandler> databaseHandlers = new ConcurrentHashMap<>();

    public static OfflineDatabaseHandler getOfflineDatabaseHandlerForMapId(Context context, String dbName) {
        final String lowerMapId = dbName.toLowerCase(Locale.US);
        OfflineDatabaseHandler dbh = databaseHandlers.get(lowerMapId);
        if(dbh == null){
            dbh = new OfflineDatabaseHandler(context, lowerMapId);
            databaseHandlers.put(lowerMapId, dbh);
        }

        return dbh;
    }

    public static void deleteDatabase(Context context, String dbName){
        if(context == null || TextUtils.isEmpty(dbName))
            return;

        final String lowerMapId = dbName.toLowerCase(Locale.US);
        OfflineDatabaseHandler dbHandler = databaseHandlers.remove(lowerMapId);
        if(dbHandler != null)
            dbHandler.close();

        context.deleteDatabase(lowerMapId);
    }
}
