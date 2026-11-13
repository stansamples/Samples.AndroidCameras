package org.stansamples.android.cameradst

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class MainActivity : ComponentActivity() {
    private fun getBitmap(uri: Uri): Bitmap {
        val source = ImageDecoder.createSource(contentResolver, uri)
        return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            decoder.setTargetSize(info.size.width, info.size.height)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context: Context = this
        val root = FrameLayout(context).also { root ->
            root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            val iv = ImageView(context).also { view ->
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                root.addView(view)
            }
            val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            runCatching {
                                getBitmap(uri = result.data?.data ?: error("No uri!"))
                            }
                        }.fold(
                            onSuccess = { bitmap ->
                                iv.setImageBitmap(bitmap)
                            },
                            onFailure = { error ->
                                Log.w("[Main]", "take picture error: $error")
                            },
                        )
                    }
                }
            }
            LinearLayout(context).also { columns ->
                columns.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM,
                ).also {
                    it.bottomMargin = 128
                }
                columns.orientation = LinearLayout.VERTICAL
                Button(context).also { view ->
                    view.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
                    view.text = "open camera"
                    view.setOnClickListener { _ ->
                        val component = ComponentName(
                            "org.stansamples.android.camerasrc.debug",
                            "org.stansamples.android.camerasrc.CameraActivity"
                        )
                        val intent = Intent().setComponent(component)
                        cameraLauncher.launch(intent)
                    }
                    columns.addView(view)
                }
                root.addView(columns)
            }
        }
        setContentView(root)
    }
}
