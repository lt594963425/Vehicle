package com.tao.vehiclemvdemo.android.utils.unit.systems;


import com.tao.vehiclemvdemo.android.utils.unit.providers.area.AreaUnitProvider;
import com.tao.vehiclemvdemo.android.utils.unit.providers.length.LengthUnitProvider;
import com.tao.vehiclemvdemo.android.utils.unit.providers.speed.SpeedUnitProvider;

/**
 * Created by Fredia Huya-Kouadio on 1/20/15.
 */
public interface UnitSystem {

    int AUTO = 0;
    int METRIC = 1;
    int IMPERIAL = 2;

    LengthUnitProvider getLengthUnitProvider();

    AreaUnitProvider getAreaUnitProvider();

    SpeedUnitProvider getSpeedUnitProvider();

    int getType();

}
