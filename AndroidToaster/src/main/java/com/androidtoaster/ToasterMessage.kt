package com.androidtoaster

import android.content.Context
import android.widget.Toast

object ToasterMessage {
     fun showToast(context : Context, message : String){
        Toast.makeText(context,message,Toast.LENGTH_LONG).show()
    }
}