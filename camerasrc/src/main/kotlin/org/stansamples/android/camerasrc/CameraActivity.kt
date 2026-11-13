package org.stansamples.android.camerasrc

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView

internal class CameraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context: Context = this
        FrameLayout(context).also { root ->
            root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            val pv = PreviewView(context).also { view ->
                view.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
                root.addView(view)
            }
            val tv = TextView(context).also { view ->
                view.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL,
                )
                root.addView(view)
            }
            setContentView(root)
            val isGranted = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (isGranted) {
                tv.visibility = View.GONE
                val controller = LifecycleCameraController(context)
                controller.bindToLifecycle(this)
                controller.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                pv.controller = controller
            } else {
                pv.visibility = View.GONE
                tv.text = "no camera permission"
            }
        }
    }
}
