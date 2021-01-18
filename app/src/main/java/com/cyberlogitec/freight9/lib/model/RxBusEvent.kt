package com.cyberlogitec.freight9.lib.model

data class RxBusEvent(val code: Int, val sent: Any) {
    companion object {
        // activity finish
        val EVENT_FINISH = 9000001
        // connect or disconnect - (boolean)
        val EVENT_INTERNET = 9000002
        /**
         * [state, type, name] - (NetworkStatus)
         * state=DISCONNECTED, type=-1, name='NONE'
         * state=CONNECTED, type=1, name='WIFI'
         * state=CONNECTED, type=0, name='MOBILE'
         */
        val EVENT_NETWORK = 9000003

        // FINISH KIND
        val FINISH_ALL = 9100001
        val FINISH_INVENTORY = 9100002
        val FINISH_OFFERS = 9100003

        // Push KIND
        val EVENT_OFFER_DISCARD = 9200001
        val EVENT_OFFER_REFRESH = 9200002
    }
}