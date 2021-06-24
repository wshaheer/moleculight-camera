package com.moleculight.assessment.services

import java.io.File

interface ICamera {
    fun didChangeFrames(camera: CameraService, frames: Int)
    fun didChangeState(camera: CameraService, state: CameraState)
    fun didSaveImage(camera: CameraService, file: File)
}