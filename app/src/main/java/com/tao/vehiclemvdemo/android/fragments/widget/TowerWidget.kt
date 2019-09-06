package com.tao.vehiclemvdemo.android.fragments.widget

import com.tao.vehiclemvdemo.android.fragments.helpers.ApiListenerFragment


/**
 * Created by Fredia Huya-Kouadio on 8/28/15.
 */
public abstract class TowerWidget : ApiListenerFragment() {

    abstract fun getWidgetType(): TowerWidgets
}