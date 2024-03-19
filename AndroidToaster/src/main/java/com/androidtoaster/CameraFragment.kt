package com.androidtoaster

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias OnImageCapture = (imagePath: String) -> Unit

const val SWITCH_CAMERA = "SWITCH_CAMERA"

class CameraFragment : Fragment() {

    private lateinit var binding: FragmentCameraBinding
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    private lateinit var imgCaptureExecutor: ExecutorService
    private val cameraPermissionResult =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {

            } else {
                ToasterMessage.showToast(
                    this.requireContext(),
                    "The camera permission is required for capture image"
                )
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext())
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        imgCaptureExecutor = Executors.newSingleThreadExecutor()
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
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
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
                                Log.v("", " onImageSaved : " + outputFileResults.savedUri)
                                onImageCapture.invoke(outputFileResults.savedUri.toString())
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
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    companion object {
        const val TAG = "CameraFragment"
        fun newInstance(switchCameraEnable: Boolean,): CameraFragment {
            val fragment = CameraFragment()
            val args = Bundle()
            args.putBoolean(SWITCH_CAMERA, switchCameraEnable)
            args.putBoolean(SWITCH_CAMERA, switchCameraEnable)
            fragment.arguments = args
            return fragment
        }
    }
}