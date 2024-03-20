package com.androidtoaster

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.androidtoaster.databinding.FragmentCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Float.max
import java.lang.Float.min
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias OnImageCapture = (imagePath: String) -> Unit

const val SWITCH_CAMERA = "SWITCH_CAMERA"
const val ENABLE_ZOOM_IN_ZOOM_OUT = "ENABLE_ZOOM_IN_ZOOM_OUT"
const val COMPRESS_IMAGE = "COMPRESS_IMAGE"
const val COMPRESS_IMAGE_QUALITY = "COMPRESS_IMAGE_QUALITY"

class CameraFragment : Fragment() {

    private lateinit var binding: FragmentCameraBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    private lateinit var imgCaptureExecutor: ExecutorService
    private var zoomRatio: Float = 1f
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var camera: Camera? = null
    private var allowCompressImage = false
    private var compressImageQuality : Int = 100
    private val cameraPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (!permissionGranted) {
                ToasterMessage.showToast(
                    this.requireContext(),
                    "The camera permission is required for capture image"
                )
            }else{
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                    storagePermissionResult.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }
        }
    private val storagePermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (!permissionGranted) {
                ToasterMessage.showToast(
                    this.requireContext(),
                    "Storage permission is required to save the image"
                )
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imgCaptureExecutor = Executors.newSingleThreadExecutor()
        scaleGestureDetector = ScaleGestureDetector(requireContext(), ScaleListener())
        cameraPermissionResult.launch(Manifest.permission.CAMERA)
        startCamera()

    }

    private fun initView() {
        val switchCameraEnable = arguments?.getBoolean(SWITCH_CAMERA)
        if (switchCameraEnable == true) {
            binding.ivSwitchCamera.visibility = View.VISIBLE
            binding.ivSwitchCamera.setOnClickListener {
                performSwitchCamera()
            }
        }else{
            binding.ivSwitchCamera.visibility = View.GONE
        }

        val allowZoomInZoomOut = arguments?.getBoolean(ENABLE_ZOOM_IN_ZOOM_OUT)

        if (allowZoomInZoomOut == true){
            binding.preview.setOnTouchListener { _, event ->
                scaleGestureDetector.onTouchEvent(event)
                true
            }
        }

        allowCompressImage = arguments?.getBoolean(COMPRESS_IMAGE)!!

        compressImageQuality = arguments?.getInt(COMPRESS_IMAGE_QUALITY)!!
    }

    private fun performSwitchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera()
    }

    private fun startCamera() {
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.preview.surfaceProvider)
        }
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                camera  = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.v("", " startCamera : " + e.printStackTrace())
            }
        }, ContextCompat.getMainExecutor(this.requireContext()))
    }

    fun takePhoto(file : File,onImageCapture: OnImageCapture) {
        if (checkCameraPermission()) {
            imageCapture?.let {
                val outputFileOptions =
                    ImageCapture.OutputFileOptions.Builder(file).build()
                it.takePicture(
                    outputFileOptions,
                    imgCaptureExecutor,
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            lifecycleScope.launch(Dispatchers.Main) {
                                Log.v("", " onImageSaved 1 : " + outputFileResults.savedUri)
                                Log.v("", " onImageSaved 2 : " + file.absolutePath)
                                onImageCapture.invoke(outputFileResults.savedUri.toString())
                                context?.let { context ->
                                    CaptureImageHelper.handleSamplingAndRotationBitmap(
                                        context,
                                        file.toUri()
                                    )?.let {
                                        if (allowCompressImage) {
                                            storeBitmap(it, file)
                                        }
                                    }
                                }
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            lifecycleScope.launch(Dispatchers.Main) {
                                Toast.makeText(
                                    binding.root.context,
                                    "Error in taking photo",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    })
            }
        }else{
            cameraPermissionResult.launch(Manifest.permission.CAMERA)
        }
    }
    private fun storeBitmap(bitmap: Bitmap, file: File) {
        file.toUri().run {
            context?.contentResolver?.openOutputStream(this)?.run {
                bitmap.compress(Bitmap.CompressFormat.JPEG, compressImageQuality, this)
                close()
            }
        }
    }

    private fun checkCameraPermission(): Boolean {

        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED)
        }else{
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    private inner class ScaleListener : ScaleGestureDetector.OnScaleGestureListener {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (detector != null) {
                val currentZoomRatio = zoomRatio * detector.scaleFactor
                zoomRatio = max(1.0f, min(currentZoomRatio, camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1.0f))
                camera?.cameraControl?.setZoomRatio(zoomRatio)
            }
            return true
        }

        override fun onScaleBegin(p0: ScaleGestureDetector): Boolean {
            return true
        }

        override fun onScaleEnd(p0: ScaleGestureDetector) {

        }
    }
    companion object {
        const val TAG = "CameraFragment"
        @Retention(AnnotationRetention.RUNTIME)
        @Target(AnnotationTarget.VALUE_PARAMETER)
        annotation class Range(val min: Int, val max: Int)

        fun newInstance(switchCameraEnable: Boolean = true,
                        allowZoomInZoomOut : Boolean = true,
                        allowCompressImage : Boolean = false,
                        @Range(min = 1, max = 100) compressQuality: Int = 100): CameraFragment {

            if (allowCompressImage && compressQuality>100){
                throw IllegalArgumentException("compressQuality parameter value should not be $compressQuality , it should be in range between 0 to 100. ")
            }

            val fragment = CameraFragment()
            val args = Bundle()
            args.putBoolean(SWITCH_CAMERA, switchCameraEnable)
            args.putBoolean(ENABLE_ZOOM_IN_ZOOM_OUT, allowZoomInZoomOut)
            args.putBoolean(COMPRESS_IMAGE, allowCompressImage)
            args.putInt(COMPRESS_IMAGE_QUALITY, compressQuality)
            fragment.arguments = args
            return fragment
        }
    }
}