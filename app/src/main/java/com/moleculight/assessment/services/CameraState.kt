package com.moleculight.assessment.services

enum class CameraState(val state: Int) {
    IDLE(-1),
    PREVIEW(0),
    FOCUS(1),
    PRE_CAPTURE(2),
    EXPOSURE(3),
    CAPTURE(4)
}