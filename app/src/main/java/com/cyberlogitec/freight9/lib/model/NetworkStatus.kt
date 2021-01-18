package com.cyberlogitec.freight9.lib.model

import android.net.NetworkInfo


/**
 * [state, type, name] - (NetworkStatus)
 * state=DISCONNECTED, type=-1, name='NONE'
 * state=CONNECTED, type=1, name='WIFI'
 * state=CONNECTED, type=0, name='MOBILE'
 */
data class NetworkStatus(
    var state: NetworkInfo.State = NetworkInfo.State.DISCONNECTED,
    var type: Int = -1,
    var name: String = ""
)