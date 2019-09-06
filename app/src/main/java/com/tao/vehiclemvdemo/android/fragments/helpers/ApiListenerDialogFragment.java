package com.tao.vehiclemvdemo.android.fragments.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;

import com.tao.vehiclemvdemo.android.DroidPlannerApp;
import com.tao.vehiclemvdemo.android.fragments.SettingsFragment;
import com.tao.vehiclemvdemo.android.proxy.mission.MissionProxy;
import com.tao.vehiclemvdemo.android.utils.prefs.DroidPlannerPrefs;
import com.tao.vehiclemvdemo.android.utils.unit.UnitManager;
import com.tao.vehiclemvdemo.android.utils.unit.providers.area.AreaUnitProvider;
import com.tao.vehiclemvdemo.android.utils.unit.providers.length.LengthUnitProvider;
import com.tao.vehiclemvdemo.android.utils.unit.providers.speed.SpeedUnitProvider;
import com.tao.vehiclemvdemo.android.utils.unit.systems.UnitSystem;
import com.o3dr.android.client.Drone;


/**
 * Provides access to the DroidPlannerApi to its derived class.
 */
@SuppressLint("ValidFragment")
public abstract class ApiListenerDialogFragment extends DialogFragment implements
        DroidPlannerApp.ApiListener {

    private static final IntentFilter filter = new IntentFilter();
    static {
        filter.addAction(SettingsFragment.ACTION_PREF_UNIT_SYSTEM_UPDATE);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()){
                case SettingsFragment.ACTION_PREF_UNIT_SYSTEM_UPDATE:
                    setupUnitProviders(context);
                    break;
            }
        }
    };

    private DroidPlannerApp dpApp;
    private LocalBroadcastManager broadcastManager;

    private LengthUnitProvider lengthUnitProvider;
    private AreaUnitProvider areaUnitProvider;
    private SpeedUnitProvider speedUnitProvider;

    protected MissionProxy getMissionProxy(){ return dpApp.getMissionProxy();}
    protected Drone getDrone(){
        return dpApp.getDrone();
    }

    protected LocalBroadcastManager getBroadcastManager(){
        return broadcastManager;
    }

    protected LengthUnitProvider getLengthUnitProvider(){
        return lengthUnitProvider;
    }

    protected AreaUnitProvider getAreaUnitProvider(){
        return areaUnitProvider;
    }

    protected SpeedUnitProvider getSpeedUnitProvider(){
        return speedUnitProvider;
    }

    protected DroidPlannerPrefs getAppPrefs(){
        return DroidPlannerPrefs.getInstance(getContext());
    }

    protected DroidPlannerApp getApplication() {
        return dpApp;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        dpApp = (DroidPlannerApp) activity.getApplication();

        final Context context = activity.getApplicationContext();
        broadcastManager = LocalBroadcastManager.getInstance(context);

        setupUnitProviders(context);
    }

    @Override
    public void onStart() {
        super.onStart();

        setupUnitProviders(getContext());
        broadcastManager.registerReceiver(receiver, filter);

        dpApp.addApiListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        dpApp.removeApiListener(this);

        broadcastManager.unregisterReceiver(receiver);
    }

    private void setupUnitProviders(Context context){
        if(context == null)
            return;

        final UnitSystem unitSystem = UnitManager.getUnitSystem(context);
        lengthUnitProvider = unitSystem.getLengthUnitProvider();
        areaUnitProvider = unitSystem.getAreaUnitProvider();
        speedUnitProvider = unitSystem.getSpeedUnitProvider();
    }
}
