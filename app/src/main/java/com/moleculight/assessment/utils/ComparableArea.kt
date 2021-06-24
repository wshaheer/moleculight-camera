package com.moleculight.assessment.utils

import android.util.Size
import java.lang.Long.signum
import java.util.Comparator

class ComparableArea : Comparator<Size> {

    override fun compare(o1: Size, o2: Size) =
        signum(o1.width.toLong() * o1.height - o2.width.toLong() * o2.height)

}