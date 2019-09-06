package com.tao.vehiclemvdemo.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.o3dr.android.client.BuildConfig;
import com.o3dr.android.client.ControlTower;
import com.o3dr.android.client.Drone;
import com.o3dr.android.client.apis.VehicleApi;
import com.o3dr.android.client.interfaces.DroneListener;
import com.o3dr.android.client.interfaces.LinkListener;
import com.o3dr.android.client.interfaces.TowerListener;
import com.o3dr.services.android.lib.drone.attribute.AttributeEvent;
import com.o3dr.services.android.lib.drone.connection.ConnectionParameter;
import com.o3dr.services.android.lib.drone.connection.ConnectionType;
import com.o3dr.services.android.lib.gcs.link.LinkConnectionStatus;
import com.o3dr.services.android.lib.model.AbstractCommandListener;
import com.o3dr.services.android.lib.util.Utils;
import com.tao.vehiclemvdemo.android.droneshare.data.DroneShareDB;
import com.tao.vehiclemvdemo.android.activities.helpers.BluetoothDevicesActivity;
import com.tao.vehiclemvdemo.android.droneshare.UploaderService;
import com.tao.vehiclemvdemo.android.droneshare.data.SessionDB;
import com.tao.vehiclemvdemo.android.proxy.mission.MissionProxy;
import com.tao.vehiclemvdemo.android.utils.LogToFileTree;
import com.tao.vehiclemvdemo.android.utils.LogUtil;
import com.tao.vehiclemvdemo.android.utils.TLogUtils;
import com.tao.vehiclemvdemo.android.utils.file.IO.ExceptionWriter;
import com.tao.vehiclemvdemo.android.utils.prefs.DroidPlannerPrefs;
import com.tao.vehiclemvdemo.android.utils.sound.SoundManager;


import org.droidplanner.services.android.impl.core.drone.manager.MavLinkDroneManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

public class DroidPlannerApp extends MultiDexApplication implements DroneListener, TowerListener, LinkListener {

    private static final long DELAY_TO_DISCONNECTION = 1000L; // ms

    private static final String TAG = DroidPlannerApp.class.getSimpleName();

    public static final String ACTION_TOGGLE_DRONE_CONNECTION = Utils.PACKAGE_NAME
            + ".ACTION_TOGGLE_DRONE_CONNECTION";
    public static final String EXTRA_ESTABLISH_CONNECTION = "extra_establish_connection";

    private static final long EVENTS_DISPATCHING_PERIOD = 200L; //MS

    private static final long INVALID_SESSION_ID = -1L;

    private static final AtomicBoolean isCellularNetworkOn = new AtomicBoolean(false);

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            switch (action) {
                case ACTION_TOGGLE_DRONE_CONNECTION:
                    boolean connect = intent.getBooleanExtra(EXTRA_ESTABLISH_CONNECTION, !drone.isConnected());
                    LogUtil.e("App", "---------------------------收到无人机连接广播---------------------------");
                    if (connect) {
                        LogUtil.e("App", "---------------------------连接无人机---------------------------");
                        connectToDrone();
                    } else {
                        LogUtil.e("App", "---------------------------断开连接无人机---------------------------");
                        disconnectFromDrone();
                    }

                    break;
            }
        }
    };

    @Override
    public void onTowerConnected() {
        Timber.d("-----------------------连接控制器---------------------");
        LogUtil.e("App", "---------------------------连接控制器---------------------------");
        drone.unregisterDroneListener(this);
        controlTower.registerDrone(drone, handler);
        drone.registerDroneListener(this);
        notifyApiConnected();
    }

    @Override
    public void onTowerDisconnected() {
        Timber.d("Disconnection from the control tower.");
        notifyApiDisconnected();
    }

    @Override
    public void onLinkStateUpdated(@NonNull LinkConnectionStatus connectionStatus) {
        LogUtil.e("连接", "-----------------2----------------。onLinkStateUpdated:" + connectionStatus.getStatusCode());
        switch (connectionStatus.getStatusCode()) {
            case LinkConnectionStatus.FAILED:
                Bundle extras = connectionStatus.getExtras();
                String errorMsg = null;
                if (extras != null) {
                    errorMsg = extras.getString(LinkConnectionStatus.EXTRA_ERROR_MSG);
                }

                Toast.makeText(getApplicationContext(), "Connection failed: " + errorMsg,
                        Toast.LENGTH_LONG).show();
                break;

        }
    }

    public interface ApiListener {
        void onApiConnected();

        void onApiDisconnected();
    }

    private final Runnable disconnectionTask = new Runnable() {
        @Override
        public void run() {
            LogUtil.e("App", "-----------------------------控制器失去连接----------------------------");
            Timber.d("-----------------------------控制器失去连接----------------------------");
            controlTower.unregisterDrone(drone);
            controlTower.disconnect();

            handler.removeCallbacks(this);
        }
    };

    private final Handler handler = new Handler();
    private final List<ApiListener> apiListeners = new ArrayList<ApiListener>();

    private Thread.UncaughtExceptionHandler exceptionHandler;

    private ControlTower controlTower;
    private Drone drone;

    private MissionProxy missionProxy;
    private DroidPlannerPrefs dpPrefs;
    private LocalBroadcastManager lbm;

    private LogToFileTree logToFileTree;
    private SoundManager soundManager;

    private long currentSessionId = INVALID_SESSION_ID;
    private SessionDB sessionDB;
    private DroneShareDB droneShareDb;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    /**
     * 初始化
     */
    @Override
    public void onCreate() {
        super.onCreate();

        final Context context = getApplicationContext();

        dpPrefs = DroidPlannerPrefs.getInstance(context);
        lbm = LocalBroadcastManager.getInstance(context);
        soundManager = new SoundManager(context);
        //初始化日志
        initLoggingAndAnalytics();
        //初始化无人机
        initDronekit();
        //初始化数据库
        initDatabases();
    }

    private void initLoggingAndAnalytics() {

        final Context context = getApplicationContext();

        final Thread.UncaughtExceptionHandler dpExceptionHandler = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                new ExceptionWriter(ex).saveStackTraceToSD(context);
                exceptionHandler.uncaughtException(thread, ex);
            }
        };

        exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(dpExceptionHandler);

        if (BuildConfig.WRITE_LOG_FILE) {
            logToFileTree = new LogToFileTree();
            Timber.plant(logToFileTree);
        } else if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }


    }

    private void initDronekit() {
        LogUtil.e("App", "---------------初始化droneKit-----------------");
        Context context = getApplicationContext();

        controlTower = new ControlTower(context);
        drone = new Drone(context);
        missionProxy = new MissionProxy(this, this.drone);

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_TOGGLE_DRONE_CONNECTION);
        //注册广播
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void initDatabases() {
        Context context = getApplicationContext();
        sessionDB = new SessionDB(context);
        droneShareDb = new DroneShareDB(context);
        cleanupDroneSessions();
    }

    public void addApiListener(ApiListener listener) {
        if (listener == null)
            return;

        handler.removeCallbacks(disconnectionTask);
        boolean isTowerConnected = controlTower.isTowerConnected();
        if (isTowerConnected)
            listener.onApiConnected();

        if (!isTowerConnected) {
            try {
                controlTower.connect(this);
            } catch (IllegalStateException e) {
                //Ignore
            }
        }

        apiListeners.add(listener);
    }

    public void removeApiListener(ApiListener listener) {
        if (listener != null) {
            apiListeners.remove(listener);
            if (controlTower.isTowerConnected())
                listener.onApiDisconnected();
        }

        shouldWeTerminate();
    }

    private void shouldWeTerminate() {
        if (apiListeners.isEmpty() && !drone.isConnected()) {
            // Wait 30s, then disconnect the service binding.
            handler.postDelayed(disconnectionTask, DELAY_TO_DISCONNECTION);
        }
    }

    private void notifyApiConnected() {
        if (apiListeners.isEmpty())
            return;

        for (ApiListener listener : apiListeners)
            listener.onApiConnected();
    }

    private void notifyApiDisconnected() {
        if (apiListeners.isEmpty())
            return;

        for (ApiListener listener : apiListeners)
            listener.onApiDisconnected();
    }

    public void connectToDrone() {
        final ConnectionParameter connParams = retrieveConnectionParameters();
        if (connParams == null)
            return;

        boolean isDroneConnected = drone.isConnected();
        if (!connParams.equals(drone.getConnectionParameter()) && isDroneConnected) {
            LogUtil.e("App", "-----------------2-----------------使用不同参数重新连接之前，请断开连接。");

            Timber.d("使用不同参数重新连接之前，雄蜂断开连接。");
            drone.disconnect();
            isDroneConnected = false;
        }

        if (!isDroneConnected) {

            String ip = connParams.getParamsBundle().getString(ConnectionType.EXTRA_TCP_SERVER_IP);

            int port = connParams.getParamsBundle().getInt(ConnectionType.EXTRA_TCP_SERVER_PORT);
            LogUtil.e("App", "-----------------2-----------------使用参数" + ip +"----"+port + "连接到无人机。");
            Timber.d("Connecting to drone using parameter %s", connParams);
            drone.connect(connParams, this);
        }
    }

    public static void connectToDrone(Context context) {
        context.sendBroadcast(new Intent(DroidPlannerApp.ACTION_TOGGLE_DRONE_CONNECTION)
                .putExtra(DroidPlannerApp.EXTRA_ESTABLISH_CONNECTION, true));
    }

    public static void disconnectFromDrone(Context context) {
        context.sendBroadcast(new Intent(DroidPlannerApp.ACTION_TOGGLE_DRONE_CONNECTION)
                .putExtra(DroidPlannerApp.EXTRA_ESTABLISH_CONNECTION, false));
    }

    public void disconnectFromDrone() {
        if (drone.isConnected()) {
            Timber.d("Disconnecting from drone.");
            LogUtil.e("App", "------------------无人机断开连接---------------------");
            drone.disconnect();
        }
    }

    public Drone getDrone() {
        return this.drone;
    }

    public MissionProxy getMissionProxy() {
        return this.missionProxy;
    }

    private ConnectionParameter retrieveConnectionParameters() {
        final @ConnectionType.Type int connectionType = dpPrefs.getConnectionParameterType();
        LogUtil.e("App", "------------------连接类型---------------------" + connectionType);
        // Generate the uri for logging the tlog data for this session.
        Uri tlogLoggingUri = TLogUtils.getTLogLoggingUri(getApplicationContext(), connectionType, System.currentTimeMillis());

        ConnectionParameter connParams;
        switch (connectionType) {
            case ConnectionType.TYPE_USB:
                connParams = ConnectionParameter.newUsbConnection(dpPrefs.getUsbBaudRate(),
                        tlogLoggingUri, EVENTS_DISPATCHING_PERIOD);
                break;

            case ConnectionType.TYPE_UDP:
                if (dpPrefs.isUdpPingEnabled()) {
                    connParams = ConnectionParameter.newUdpWithPingConnection(
                            dpPrefs.getUdpServerPort(),
                            dpPrefs.getUdpPingReceiverIp(),
                            dpPrefs.getUdpPingReceiverPort(),
                            "Hello".getBytes(),
                            ConnectionType.DEFAULT_UDP_PING_PERIOD,
                            tlogLoggingUri,
                            EVENTS_DISPATCHING_PERIOD);
                } else {
                    connParams = ConnectionParameter.newUdpConnection(dpPrefs.getUdpServerPort(),
                            tlogLoggingUri, EVENTS_DISPATCHING_PERIOD);
                }
                break;

            case ConnectionType.TYPE_TCP:
                connParams = ConnectionParameter.newTcpConnection(dpPrefs.getTcpServerIp(),
                        dpPrefs.getTcpServerPort(), tlogLoggingUri, EVENTS_DISPATCHING_PERIOD);
                break;

            case ConnectionType.TYPE_BLUETOOTH:
                String btAddress = dpPrefs.getBluetoothDeviceAddress();
                if (TextUtils.isEmpty(btAddress)) {
                    connParams = null;
                    startActivity(new Intent(getApplicationContext(),
                            BluetoothDevicesActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                } else {
                    connParams = ConnectionParameter.newBluetoothConnection(btAddress,
                            tlogLoggingUri, EVENTS_DISPATCHING_PERIOD);
                }
                break;

            default:
                Log.e(TAG, "无法识别的连接类型: " + connectionType);
                LogUtil.e("App", "------------------无法识别的连接类型---------------------" + connectionType);
                connParams = null;
                break;
        }

        return connParams;
    }

    @Override
    public void onDroneEvent(String event, Bundle extras) {
        LogUtil.e("App", "------------------onDroneEvent---------无人机的状态------------");
        switch (event) {
            case AttributeEvent.STATE_CONNECTED: {
                handler.removeCallbacks(disconnectionTask);

                startDroneSession(System.currentTimeMillis());

                startService(new Intent(getApplicationContext(), AppService.class));

                final boolean isReturnToMeOn = dpPrefs.isReturnToMeEnabled();
                VehicleApi.getApi(drone).enableReturnToMe(isReturnToMeOn, new AbstractCommandListener() {
                    @Override
                    public void onSuccess() {
                        Timber.i("Return to me %s successfully.", isReturnToMeOn ? "started" : "stopped");

                        LogUtil.e("App", "------------------onSuccess---------操作启用返回给我------------");
                    }

                    @Override
                    public void onError(int i) {
                        Timber.e("%s return to me failed: %d", isReturnToMeOn ? "Starting" : "Stopping", i);
                        LogUtil.e("App", "------------------onError---------返回失败------------");
                    }

                    @Override
                    public void onTimeout() {
                        Timber.w("%s return to me timed out.", isReturnToMeOn ? "Starting" : "Stopping");
                        LogUtil.e("App", "------------------onTimeout---------返回失败 超时------------");
                    }
                });

                final Intent droneIntent = new Intent(event);
                if (extras != null)
                    droneIntent.putExtras(extras);
                lbm.sendBroadcast(droneIntent);
                break;
            }

            case AttributeEvent.STATE_DISCONNECTED: {
                LogUtil.e("App", "------------------onDroneEvent---------是去链接------------");
                shouldWeTerminate();

                final Intent droneIntent = new Intent(event);
                if (extras != null)
                    droneIntent.putExtras(extras);
                lbm.sendBroadcast(droneIntent);

                endDroneSession();
                // Fire the droneshare log uploader
                UploaderService.kickStart(getApplicationContext());
                break;
            }

            case AttributeEvent.PARAMETERS_REFRESH_COMPLETED:
                LogUtil.e("App", "------------------onDroneEvent---------参数刷新完成------------");
                // Grab the vehicle default speed, and update the preferences.
                double speedParameter = drone.getSpeedParameter() / 100; //cm/s to m/s conversion.
                if (speedParameter != 0) {
                    dpPrefs.setVehicleDefaultSpeed((float) speedParameter);
                }
                // FALL THROUGH

            default: {
                final Intent droneIntent = new Intent(event);
                if (extras != null)
                    droneIntent.putExtras(extras);
                lbm.sendBroadcast(droneIntent);
                break;
            }
        }
    }

    @Override
    public void onDroneServiceInterrupted(String errorMsg) {
        Timber.d("Drone service interrupted: %s", errorMsg);
        controlTower.unregisterDrone(drone);
        LogUtil.e("App", "------------------onDroneServiceInterrupted---------无人机服务中断------------");
        if (!TextUtils.isEmpty(errorMsg))
            Log.e(TAG, errorMsg);
    }

    public static void setCellularNetworkAvailability(boolean isAvailable) {
        isCellularNetworkOn.set(isAvailable);
    }

    public static boolean isCellularNetworkAvailable() {
        return isCellularNetworkOn.get();
    }

    public void createFileStartLogging() {
        if (logToFileTree != null) {
            logToFileTree.createFileStartLogging(getApplicationContext());
        }
    }

    public void closeLogFile() {
        if (logToFileTree != null) {
            logToFileTree.stopLoggingThread();
        }
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    private void startDroneSession(long startTime) {
        ConnectionParameter connParams = drone.getConnectionParameter();
        @ConnectionType.Type int connectionType = connParams.getConnectionType();
        final String connectionTypeLabel = ConnectionType.getConnectionTypeLabel(connectionType);
        Uri tlogLoggingUri = connParams.getTLogLoggingUri();

        // Record the starting drone session
        currentSessionId = this.sessionDB.startSession(startTime, connectionTypeLabel, tlogLoggingUri);
        if (tlogLoggingUri != null && dpPrefs.isDroneshareEnabled()) {
            //Create an entry in the droneshare upload queue
            droneShareDb.queueDataUploadEntry(dpPrefs.getDroneshareLogin(), currentSessionId);
        }
    }

    private void endDroneSession() {
        //log into the database the disconnection time.
        if (currentSessionId != INVALID_SESSION_ID) {
            this.sessionDB.endSessions(System.currentTimeMillis(), currentSessionId);
        }
    }

    private void cleanupDroneSessions() {
        //Cleanup all the opened drone sessions
        sessionDB.cleanupOpenedSessions(System.currentTimeMillis());

        // Check for droneshare logs to upload.
        UploaderService.kickStart(getApplicationContext());
    }

    public DroneShareDB getDroneShareDatabase() {
        return droneShareDb;
    }

    /**
     * Return the vehicle speed in meters per second.
     */
    public double getVehicleSpeed() {
        double speedParameter = drone.getSpeedParameter() / 100; //cm/s to m/s conversion.
        if (speedParameter == 0) {
            speedParameter = dpPrefs.getVehicleDefaultSpeed();
        }
        return speedParameter;
    }

    public SessionDB getSessionDatabase() {
        return sessionDB;
    }
}
