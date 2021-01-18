package com.cyberlogitec.freight9.lib.ui.fragmentstepper

import com.trello.rxlifecycle3.components.support.RxFragment

interface StepsManager {
    fun getCount(): Int
    fun getStep(position: Int): RxFragment
}