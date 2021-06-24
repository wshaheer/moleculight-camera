package com.moleculight.assessment

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.moleculight.assessment.databinding.ActivityMainBinding
import com.moleculight.assessment.services.CameraService
import com.moleculight.assessment.services.CameraState
import com.moleculight.assessment.services.ICamera
import java.io.File

class MainActivity : AppCompatActivity() {

    private val cameraStatus = object : ICamera {
        override fun didChangeFrames(camera: CameraService, frames: Int) {
            when {
                camera.state == CameraState.IDLE -> {
                    return
                }
                camera.id == cam0?.id -> {
                    binding.cameraText0.text = getString(R.string.camera_view_frames).format(camera.id, frames)
                }
                else -> {
                    binding.cameraText1.text = getString(R.string.camera_view_frames).format(camera.id, frames)
                }
            }
        }

        override fun didChangeState(camera: CameraService, state: CameraState) {
            if (state != CameraState.IDLE) {
                binding.captureButton.isEnabled = true

                if (camera.id == cam0?.id) {
                    binding.cameraText0.text = getString(R.string.camera_view_frames).format(camera.id, 0)
                } else {
                    binding.cameraText1.text = getString(R.string.camera_view_frames).format(camera.id, 0)
                }
            } else {
                if (camera.id == cam0?.id) {
                    binding.cameraText0.text = getString(R.string.camera_view_state).format(camera.id)
                } else {
                    binding.cameraText1.text = getString(R.string.camera_view_state).format(camera.id)
                }

                if (cam0?.state == CameraState.IDLE && cam1?.state == CameraState.IDLE) {
                    binding.captureButton.isEnabled = false
                }
            }
        }

        override fun didSaveImage(camera: CameraService, file: File) {
            AlertDialog.Builder(this@MainActivity)
                .setMessage(getString(R.string.camera_capture_path).format(camera.id, file.absolutePath))
                .setNeutralButton("OK", null)
                .create()
                .show()
        }
    }

    private var cam0: CameraService? = null
    private var cam1: CameraService? = null

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if (!didRequirePermissions()) {
            didGrantPermissions()
        }
    }

    override fun onPause() {
        super.onPause()

        cam0?.shouldStopPreview()
        cam1?.shouldStopPreview()
    }

    override fun onResume() {
        super.onResume()

        cam0?.shouldStartPreview()
        cam1?.shouldStartPreview()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            val cameraPermission = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA)
            val storagePermission = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

            if (cameraPermission != PackageManager.PERMISSION_GRANTED
                || storagePermission != PackageManager.PERMISSION_GRANTED) {
                finish()
                return
            }

            didGrantPermissions()
        }
    }

    private fun initialize() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        if (manager.cameraIdList.size < 1) {
            AlertDialog.Builder(this@MainActivity)
                .setMessage("Camera sensor(s) not found")
                .setNeutralButton("OK") { _, _ ->
                    finish()
                }
                .create()
                .show()
        }

        cam0 = CameraService(
            manager.cameraIdList[0],
            "CAM#${manager.cameraIdList[0]}",
            binding.cameraView0,
            cameraStatus
        )

        if (manager.cameraIdList.size >= 2) {
            cam1 = CameraService(
                manager.cameraIdList[1],
                "CAM#${manager.cameraIdList[1]}",
                binding.cameraView1,
                cameraStatus
            )
        }
    }

    private fun toggle(camera: CameraService?) {
        camera?.let {
            it.previewing = !it.previewing
            it.shouldUpdatePreview()
        }
    }

    private fun didRequirePermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA)
        val storagePermission = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (cameraPermission != PackageManager.PERMISSION_GRANTED || storagePermission != PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "Missiong required permissions")

            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ||
                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage("Application requires CAMERA and STORAGE permissions")
                    .setPositiveButton("ALLOW") { _, _ ->
                        requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
                    }
                    .setNegativeButton("DENY") { _, _ ->
                        finish()
                    }
                    .create()
                    .show()
            } else {
                requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
            }

            return true
        }

        return false
    }

    private fun didGrantPermissions() {
        val directory = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString(), "/MolecuLight")

        if (!directory.exists()) {
            directory.mkdirs()
        }

        Log.d("MainActivity", "Image path: $directory")

        initialize()

        cam0?.previewing = true

        binding.cameraView0.setOnClickListener { toggle(cam0) }
        binding.cameraView1.setOnClickListener { toggle(cam1) }

        binding.captureButton.setOnClickListener {
            cam0?.let {
                if (it.state == CameraState.IDLE) {
                    return@let
                }

                it.shouldLockFocus()
            }

            cam1?.let {
                if (it.state == CameraState.IDLE) {
                    return@let
                }

                it.shouldLockFocus()
            }
        }
    }

    companion object {
        private val PERMISSIONS_REQUEST_CODE = 101
        private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA, Manifest.permission
            .WRITE_EXTERNAL_STORAGE)
    }
}