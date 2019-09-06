package com.tao.vehiclemvdemo.android.notifications;

import android.content.Context;

import com.o3dr.android.client.Drone;


/**
 * This class handles DroidPlanner's status bar, and audible notifications. It
 * also provides support for the Android Wear functionality.
 */
public class NotificationHandler {

    /**
     * Defines the methods that need to be supported by Droidplanner's
     * notification provider types (i.e: audible (text to speech), status bar).
     */
    interface NotificationProvider {
        void init();

        /**
         * Release resources used by the provider.
         */
        void onTerminate();
    }

    /**
     * Handles Droidplanner's audible notifications.
     */
    private final TTSNotificationProvider mTtsNotification;

    /**
     * Handles Droidplanner's status bar notification.
     */
    private final StatusBarNotificationProvider mStatusBarNotification;

    /**
     * Handles emergency beep notification.
     */
    private final EmergencyBeepNotificationProvider mBeepNotification;

    private final Context context;

    public NotificationHandler(Context context, Drone drone) {
        this.context = context;

        mTtsNotification = new TTSNotificationProvider(context, drone);
        mStatusBarNotification = new StatusBarNotificationProvider(context, drone);
        mBeepNotification = new EmergencyBeepNotificationProvider(context);
    }



    public void init() {
        mTtsNotification.init();
        mStatusBarNotification.init();
        mBeepNotification.init();
    }

    public void terminate() {
        mTtsNotification.onTerminate();
        mStatusBarNotification.onTerminate();
        mBeepNotification.onTerminate();
    }
}
