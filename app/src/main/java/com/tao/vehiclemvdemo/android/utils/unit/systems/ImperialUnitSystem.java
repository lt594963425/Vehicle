package com.tao.vehiclemvdemo.android.utils.unit.systems;

import com.tao.vehiclemvdemo.android.utils.unit.providers.area.AreaUnitProvider;
import com.tao.vehiclemvdemo.android.utils.unit.providers.area.ImperialAreaUnitProvider;
import com.tao.vehiclemvdemo.android.utils.unit.providers.length.ImperialLengthUnitProvider;
import com.tao.vehiclemvdemo.android.utils.unit.providers.length.LengthUnitProvider;
import com.tao.vehiclemvdemo.android.utils.unit.providers.speed.ImperialSpeedUnitProvider;
import com.tao.vehiclemvdemo.android.utils.unit.providers.speed.SpeedUnitProvider;

/**
 * Created by Fredia Huya-Kouadio on 1/20/15.
 */
public class ImperialUnitSystem implements UnitSystem{

    private static final LengthUnitProvider lengthUnitProvider = new ImperialLengthUnitProvider();
    private static final AreaUnitProvider areaUnitProvider = new ImperialAreaUnitProvider();
    private static final SpeedUnitProvider speedUnitProvider = new ImperialSpeedUnitProvider();

    @Override
    public LengthUnitProvider getLengthUnitProvider() {
        return lengthUnitProvider;
    }

    @Override
    public AreaUnitProvider getAreaUnitProvider() {
        return areaUnitProvider;
    }

    @Override
    public SpeedUnitProvider getSpeedUnitProvider() {
        return speedUnitProvider;
    }

    @Override
    public int getType() {
        return IMPERIAL;
    }
}
