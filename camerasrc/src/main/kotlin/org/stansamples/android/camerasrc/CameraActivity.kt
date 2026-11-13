package org.stansamples.android.camerasrc

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.FileProvider
import java.io.File

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
                view.text = "no camera permission"
                root.addView(view)
            }
            val controller = LifecycleCameraController(context)
            val button = Button(context).also { view ->
                view.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM,
                ).also {
                    it.bottomMargin = 128
                }
                view.text = "take picture"
                view.setOnClickListener { _ ->
                    val file = context
                        .cacheDir
                        .resolve("captures")
                        .let {
                            it.mkdirs()
                            File.createTempFile(BuildConfig.APPLICATION_ID, ".jpg", it)
                        }
                    val options = ImageCapture.OutputFileOptions.Builder(file).build()
                    val callback = object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                            val uri = FileProvider.getUriForFile(context, "$packageName.files", file)
                            val result = Intent()
                                .setDataAndType(uri, "image/jpeg")
                                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            setResult(RESULT_OK, result)
                            finish()
                        }

                        override fun onError(exception: ImageCaptureException) {
                            Log.w("[Camera]", "take picture error: $exception")
                            setResult(RESULT_CANCELED)
                            finish()
                        }
                    }
                    controller.takePicture(options, mainExecutor, callback)
                }
                root.addView(view)
            }
            setContentView(root)
            val isGranted = checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            if (isGranted) {
                tv.visibility = View.GONE
                controller.bindToLifecycle(this)
                controller.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                pv.controller = controller
            } else {
                pv.visibility = View.GONE
                button.visibility = View.GONE
            }
        }
    }
}
