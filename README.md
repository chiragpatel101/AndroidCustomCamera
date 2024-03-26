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

2) dependencies {
	        implementation 'com.github.chiragpatel101:AndroidToaster:Tag'
         }

3) Note : here 'Tag' will be the latest version of the dependancy, suppose right now latest version is 1.1.4 in that case dependency will be like below

4) dependencies {
	        implementation 'com.github.chiragpatel101:AndroidToaster:1.1.4'
	}
