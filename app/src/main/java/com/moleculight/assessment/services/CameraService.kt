package com.moleculight.assessment.services

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import androidx.core.content.ContextCompat
import com.moleculight.assessment.utils.ComparableArea
import com.moleculight.assessment.views.ResizableTextureView
import java.io.File
import java.lang.NullPointerException
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class CameraService(
    val id: String,
    val thread: String,
    val output: ResizableTextureView,
    val listener: ICamera
) {
    val activity: Activity
    get() = output.context as Activity

    var elapsedFrames = 0
    var elapsedSeconds = SystemClock.elapsedRealtime()

    var previewing = false
    var state = CameraState.IDLE
        set(value) {
            field = value
            output.post {
                listener.didChangeState(this@CameraService, value)
            }
        }

    private val observer = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            setPreviewTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            val currentSeconds = SystemClock.elapsedRealtime()
            val currentFrames = (1000 / (currentSeconds - elapsedSeconds).toFloat()).roundToInt()

            if (currentFrames != elapsedFrames) {
                output.post { listener.didChangeFrames(this@CameraService, currentFrames) }
            }

            elapsedSeconds = currentSeconds
            elapsedFrames = currentFrames
        }
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(device: CameraDevice) {
            cameraDevice = device

            Log.i("CameraService", "onOpened: Camera opened")

            cameraLock.release()
            setCaptureSession()
        }

        override fun onClosed(camera: CameraDevice) {
            super.onClosed(camera)
            Log.i("CameraService", "onClosed: Camera closed")
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraLock.release()
            cameraDevice?.close()

            Log.i("CameraService", "onDisconnected: Camera disconnected")

            cameraDevice = null
        }

        override fun onError(device: CameraDevice, error: Int) {
            onDisconnected(device)
        }
    }

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
            super.onCaptureProgressed(session, request, partialResult)
            process(partialResult)
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)
            process(result)
        }

        private fun process(result: CaptureResult) {
            when (state) {
                CameraState.FOCUS -> capture(result)
                CameraState.PRE_CAPTURE -> {
                    val exposure = result.get(CaptureResult.CONTROL_AE_STATE)

                    if (exposure == null ||
                        exposure == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        exposure == CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        state = CameraState.EXPOSURE
                    }
                }
                CameraState.EXPOSURE -> {
                    val exposure = result.get(CaptureResult.CONTROL_AE_STATE)

                    if (exposure == null || exposure != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        state = CameraState.CAPTURE
                    }
                }
                else -> Unit
            }
        }

        private fun capture(result: CaptureResult) {
            val focus = result.get(CaptureResult.CONTROL_AF_STATE)

            if (focus == null || focus == 0) {
                shouldCaptureImage()
            } else if (focus == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                focus == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                val exposure = result.get(CaptureResult.CONTROL_AE_STATE)

                if (exposure == null || exposure == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                    state = CameraState.CAPTURE
                    shouldCaptureImage()
                } else {
                    setCaptureSequence()
                }
            }
        }
    }

/*    private val imageListener = ImageReader.OnImageAvailableListener {
        val metadata = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "CAM${id}-${FILE_NAME.format(Calendar.getInstance().time)}.jpeg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.Images.Media.IS_PENDING, true)
        }

        val uri: Uri? = activity.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, metadata)

        if (uri != null) {
            serviceHandler?.post(ImageService(it.acquireNextImage(), file, activity.applicationContext))
            metadata.put(MediaStore.Images.Media.IS_PENDING, false)
            activity.contentResolver.update(uri, metadata, null, null)
        }
    }*/


    private val imageListener = ImageReader.OnImageAvailableListener {
        file = File(
            activity.applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "MolecuLight/CAM${id}-${FILE_NAME.format(Calendar.getInstance().time)}.jpeg"
        )

        serviceHandler?.post(FileService(it.acquireNextImage(), file))
    }


    private val cameraLock = Semaphore(1)

    private var serviceHandler: Handler? = null
    private var serviceThread: HandlerThread? = null

    private var cameraOrientation: Int = 0
    private var cameraDevice: CameraDevice? = null

    private var imageReader: ImageReader? = null
    private var captureSession: CameraCaptureSession? = null

    private lateinit var previewSize: Size
    private lateinit var previewRequest: CaptureRequest
    private lateinit var requestBuilder: CaptureRequest.Builder

    private lateinit var file: File


    fun openCamera(width: Int, height: Int) {
        val permission = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        if (permission != PackageManager.PERMISSION_GRANTED) {
            return
        }

        setPreviewOutput(width, height)
        setPreviewTransform(width, height)

        try {
            if (!cameraLock.tryAcquire(3000, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Camera service thread timeout")
            }

            manager.openCamera(id, stateCallback, serviceHandler)
        } catch (e: CameraAccessException) {
            // TODO: 2021-06-23
        } catch (e: InterruptedException) {
            throw RuntimeException("Camera service thread interruption")
        }
    }

    fun closeCamera() {
        try {
            cameraLock.acquire()
            captureSession?.close()
            cameraDevice?.close()
            imageReader?.close()

            captureSession = null
            cameraDevice = null
            imageReader = null
        } catch (e: InterruptedException) {
            throw RuntimeException("Camera service thread interruption")
        } finally {
            cameraLock.release()
        }
    }

    fun shouldLockFocus() {
        try {
            state = CameraState.FOCUS

            requestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_START)
            captureSession?.capture(requestBuilder.build(), captureCallback, serviceHandler)
        } catch (e: CameraAccessException) {
            Log.e("CameraService", "shouldLockFocus: Camera access error", e)
        }
    }

    fun shouldUnlockFocus() {
        try {
            requestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CaptureRequest.CONTROL_AF_TRIGGER_CANCEL)
            captureSession?.capture(requestBuilder.build(), captureCallback, serviceHandler)

            state = CameraState.PREVIEW
            captureSession?.setRepeatingRequest(previewRequest, captureCallback, serviceHandler)
        } catch (e: CameraAccessException) {
            Log.e("CameraService", "shouldUnlockFocus: Camera access error", e)
        }
    }


    fun shouldCaptureImage() {
        try {
            if (cameraDevice == null)
                return

            val rotation = activity.windowManager.defaultDisplay.rotation
            val builder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                ?.apply {
                    imageReader?.surface?.let { addTarget(it) }

                    set(
                        CaptureRequest.JPEG_ORIENTATION,
                        (IMAGE_ORIENTATIONS.get(rotation) + cameraOrientation + 270) % 360
                    )

                    set(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                    )
                }

            val callback = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult
                ) {
                    super.onCaptureCompleted(session, request, result)
                    shouldUnlockFocus()
                    output.post { listener.didSaveImage(this@CameraService, file) }
                }
            }

            captureSession?.apply {
                stopRepeating()
                abortCaptures()
                builder?.build()?.let {
                    capture(it, callback, null)
                }
            }
        } catch (e: CameraAccessException) {
            Log.e("CameraService", "shouldCaptureImage: Camera access failed", e)
        }
    }

    fun shouldStartPreview() {
        serviceThread = HandlerThread(thread).also { it.start() }
        serviceHandler = Handler(serviceThread?.looper!!)

        output.surfaceTextureListener = observer

        if (output.isAvailable) {
            openCamera(output.width, output.height)
        }
    }

    fun shouldUpdatePreview() {
        if (previewing) {
            captureSession?.setRepeatingRequest(previewRequest, captureCallback, serviceHandler)

            state = CameraState.PREVIEW
        } else {
            captureSession?.stopRepeating()

            state = CameraState.IDLE
        }
    }

    fun shouldStopPreview() {
        closeCamera()
        serviceThread?.quitSafely()

        try {
            serviceThread?.join()
            serviceThread = null
            serviceHandler = null
        } catch (e: InterruptedException) {
            Log.e("CameraService", "shouldStopPreview: thread interruption", e)
        }
    }

    private fun setCaptureSequence() {
        try {
            requestBuilder.set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START
            )

            state = CameraState.PRE_CAPTURE
            captureSession?.capture(requestBuilder.build(), captureCallback, serviceHandler)
        } catch (e: CameraAccessException) {
            Log.e("CameraService", "setCaptureSequence: Camera access error", e)
        }
    }

    private fun setCaptureSession() {
        try {
            val texture = output.surfaceTexture

            texture?.setDefaultBufferSize(previewSize.width, previewSize.height)

            val surface = Surface(texture)

            requestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            requestBuilder.addTarget(surface)

            cameraDevice?.createCaptureSession(
                listOf(surface, imageReader?.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        if (cameraDevice == null)
                            return

                        captureSession = session

                        try {
                            requestBuilder.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                            )

                            previewRequest = requestBuilder.build()
                            shouldUpdatePreview()
                        } catch (e: CameraAccessException) {
                            // TODO: 2021-06-23
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        TODO("Not yet implemented")
                    }
                },
                null
            )
        } catch (e: CameraAccessException) {
            Log.e("CameraService", "setCaptureSession: Camera access error", e)
        }
    }

    private fun setPreviewTransform(width: Int, height: Int) {
        val rotation = activity.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val transRectF = RectF(0f, 0f, previewSize.height.toFloat(), previewSize.width.toFloat())
        val centerX = viewRectF.centerX()
        val centerY = viewRectF.centerY()

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            transRectF.offset(centerX - transRectF.centerX(), centerY - transRectF.centerY())

            val scale = (height.toFloat() / previewSize.height)
                .coerceAtLeast(width.toFloat() / previewSize.width)
            with(matrix) {
                setRectToRect(viewRectF, transRectF, Matrix.ScaleToFit.FILL)
                postScale(scale, scale, centerX, centerY)
                postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
            }
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }

        output.setTransform(matrix)
    }

    private fun setPreviewOutput(width: Int, height: Int) {
        val manager = activity.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        try {
            for (id in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(id)
                val configurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?: continue
                val imageSize = Collections.max(Arrays.asList(*configurationMap.getOutputSizes(ImageFormat.JPEG)),
                    ComparableArea())

                imageReader = ImageReader.newInstance(imageSize.width, imageSize.height, ImageFormat.JPEG, 2)
                    .apply {
                        setOnImageAvailableListener(imageListener, serviceHandler)
                    }

                val rotation = activity.windowManager.defaultDisplay.rotation

                cameraOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) !!

                val dimensions = didRotateDisplay(rotation)
                val displaySize = Point()

                activity.windowManager.defaultDisplay.getSize(displaySize)

                val viewWidth = if (dimensions) height else width
                val viewHeight = if (dimensions) width else height
                var screenWidth = if (dimensions) displaySize.y else displaySize.x
                var screenHeight = if (dimensions) displaySize.x else displaySize.y

                if (screenWidth > MAX_WIDTH) screenWidth = MAX_WIDTH
                if (screenHeight > MAX_HEIGHT) screenHeight = MAX_HEIGHT

                previewSize = getOptimalSize(
                    configurationMap.getOutputSizes(SurfaceTexture::class.java),
                    viewWidth,
                    viewHeight,
                    screenWidth,
                    screenHeight,
                    imageSize
                )

                output.setAspectRatio(previewSize.width, previewSize.height)

                return
            }
        } catch (e: CameraAccessException) {
            Log.e("CameraService", "setPreviewOutput: Camera access error", e)
        } catch (e: NullPointerException) {
            //TODO:
        }
    }

    private fun didRotateDisplay(rotation: Int): Boolean {
        return when (rotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                cameraOrientation == 90 || cameraOrientation == 270
            }

            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                cameraOrientation == 0 || cameraOrientation == 180
            }

            else -> {
                false
            }
        }
    }

    companion object {
        private val MAX_WIDTH = 1920
        private val MAX_HEIGHT = 1080
        private val FILE_NAME = SimpleDateFormat("yyyy-MM-dd HH.mm.ss.SSSS", Locale.CANADA)
        private val IMAGE_ORIENTATIONS = SparseIntArray()

        init {
            IMAGE_ORIENTATIONS.append(Surface.ROTATION_0, 90)
            IMAGE_ORIENTATIONS.append(Surface.ROTATION_90, 0)
            IMAGE_ORIENTATIONS.append(Surface.ROTATION_180, 270)
            IMAGE_ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        @JvmStatic
        private fun getOptimalSize(
            supported: Array<Size>,
            viewWidth: Int,
            viewHeight: Int,
            maxWidth: Int,
            maxHeight: Int,
            ratio: Size
        ): Size {
            val large = ArrayList<Size>()
            val small = ArrayList<Size>()

            for (size in supported) {
                if (size.width <= maxWidth && size.height <= maxHeight
                    && size.height == size.width * ratio.height / ratio.width) {
                    if (size.width >= viewWidth && size.height >= viewHeight) {
                        large.add(size)
                    } else {
                        small.add(size)
                    }
                }
            }

            return when {
                large.size > 0 -> {
                    Collections.min(large, ComparableArea())
                }
                small.size > 0 -> {
                    Collections.max(small, ComparableArea())
                }
                else -> {
                    supported[0]
                }
            }
        }
    }
}