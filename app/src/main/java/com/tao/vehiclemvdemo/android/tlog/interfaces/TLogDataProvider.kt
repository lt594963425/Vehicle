package com.tao.vehiclemvdemo.android.tlog.interfaces

import com.tao.vehiclemvdemo.android.tlog.viewers.TLogViewer

/**
 * @author ne0fhyk (Fredia Huya-Kouadio)
 */
interface TLogDataProvider {
    fun registerForTLogDataUpdate(subscriber: TLogViewer)
    fun unregisterForTLogDataUpdate(subscriber: TLogViewer)
}