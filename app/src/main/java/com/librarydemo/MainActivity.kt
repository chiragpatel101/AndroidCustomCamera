package com.librarydemo

import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.androidtoaster.CameraFragment
import com.androidtoaster.ToasterMessage
import com.librarydemo.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private val cameraFragment by lazy {
        CameraFragment.newInstance()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ToasterMessage.showToast(this,"Test Message")

        addFragment(cameraFragment)
        binding.btnCaptureImage.setOnClickListener {
            performCameraClick()
        }
    }

    private fun addFragment(cameraFragment: CameraFragment) {
        val fragmentManager = supportFragmentManager
        val ft = fragmentManager.beginTransaction()
        ft.replace(R.id.mainContainer, cameraFragment)
        ft.commitAllowingStateLoss()
    }
    private fun performCameraClick(){
        cameraFragment.takePhoto(captureImagePath()) {
            Log.v(""," performCameraClick : $it")
            ToasterMessage.showToast(this, it)
        }
    }
    fun captureImagePath(): File {
        val timeStamp = System.currentTimeMillis()
        val imageFileName = "$timeStamp.jpg"
        val folder = File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "CaptureImages")
        Log.v("", " captureImagePath : ${folder.absolutePath}")
        if (!folder.exists()) {
            folder.mkdir()
        }
        return File(folder, imageFileName)
    }
}
