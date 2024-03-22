package com.librarydemo

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
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

//        ToasterMessage.showToast(this,"Test Message")

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
        cameraFragment.takePhoto(captureImagePath()) {absolutePath , uri , bitmap ->
            Log.v(""," performCameraClick : $absolutePath => URI - ${uri.toString()}")
            ToasterMessage.showToast(this, absolutePath)
        }
    }
    private fun captureImagePath(): File {
        val timeStamp = System.currentTimeMillis()
        val imageFileName = "$timeStamp.jpg"
//        val folder = File(this.getExternalFilesDir(Environment.DIRECTORY_DCIM), "CaptureImages")
        val folder = File(Environment.getExternalStorageDirectory(), "CaptureImages")
        Log.v("", " captureImagePath 1 : ${folder.absolutePath}")
        Log.v("", " captureImagePath 2 : ${this.cacheDir}")
        Log.v("", " captureImagePath 3 : ${this.externalCacheDir}")
        Log.v("", " captureImagePath 4 : ${this.filesDir}")
        if (!folder.exists()) {
            folder.mkdir()
        }
        return File(folder, imageFileName)
    }
    /*private fun hasExternalStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION")
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                resultLauncher.launch(intent)
                return false
            }else{
                return true
            }
        }else{
            return true
        }
    }
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        permissionCheck()
    }
*/
}
