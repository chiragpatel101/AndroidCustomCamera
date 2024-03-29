Step 1 :

Add it in your settings.gradle.kts file or if you don't have 'settings.gradle.kts' then add it into your root build.gradle at the end of repositories:

	dependencyResolutionManagement {
			repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
			repositories {
				mavenCentral()
				maven { url 'https://jitpack.io' }
			}
		}

Step 2 :

1) Add below dependancy into your app level gradle file 

		dependencies {
				implementation 'com.github.chiragpatel101:AndroidToaster:Tag'
			}

3) Note : here 'Tag' will be the latest version of the dependancy, suppose right now latest version is 1.1.4 in that case dependency will be like below

		dependencies {
				implementation 'com.github.chiragpatel101:AndroidToaster:1.1.4'
			}

Step 3 :

1) Add Framlayout into your XML where you need camera

		<FrameLayout	
			android:layout_width="match_parent"
			android:layout_height="0dp"
			android:id="@+id/mainContainer"
			android:layout_weight="1"/>

 //Note : Adjust your framlayout according to your need

4) In your activity define cameraFragment like below

	
		//If you don't want to configure value from your side then you can define CameraFragment like below, it will consider default values

	    private val cameraFragment by lazy {
			CameraFragment.newInstance()
		}
	
		//If you want to pass values from your side then you can defaine CameraFragment like below


		/*
				CameraFragment.newInstance(defaultCameraMode = CameraFragment.DefaultCameraMode.BACK
				,switchCameraEnable = true
				,allowZoomInZoomOut = true
				,allowCompressImage = true
				, compressImageQuality = 100
				,enableFlashMode = true
				, defaultFlashMode = CameraFragment.FlashMode.AUTO)
		*/

	Key parameter explanation : 
	
		defaultCameraMode = it will allow you to set default camera, you can set value for back and front
	
		switchCameraEnable = It will give you switch camera button on camera
	
		allowZoomInZoomOut = it will give you zoom in and zoom out functionality, it's true by default
	
		allowCompressImage = it will perform compression on captured image by default it's true but you can disable it by setting false value
	
		compressImageQuality = it will allow you to set quality of image after compression
	
		enableFlashMode = it will give you the flash button over the image
	
		defaultFlashMode = You can set default Flash mode by using this parameter, default it's AUTO
	
	
	
	
5) In OnCreate method add your cameraFragment into container like below

		val fragmentManager = supportFragmentManager
        val ft = fragmentManager.beginTransaction()
        ft.replace(R.id.mainContainer, cameraFragment)
        ft.commitAllowingStateLoss()


6) For capture the image, on click of your button just perform below line,

        cameraFragment.takePhoto(captureImagePath()) {absolutePath , uri , bitmap ->
		
			// TO DO here
			
        }
		
		//above method will give you the captured image absolutePath, uri and bitmap image for the same.

		//captureImagePath() -> This function should return image file path where it get stored.
		
		
		private fun captureImagePath(): File {
		
			val timeStamp = System.currentTimeMillis()
			val imageFileName = "$timeStamp.jpg"
			val folder = File(this.getExternalFilesDir(Environment.DIRECTORY_DCIM), "CaptureImages")
			if (!folder.exists()) {
				folder.mkdir()
			}
			
			return File(folder, imageFileName)
		
		}

		
		
		



