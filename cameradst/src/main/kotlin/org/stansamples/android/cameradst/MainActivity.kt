package org.stansamples.android.cameradst

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class MainActivity : ComponentActivity() {
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        if (result.resultCode == RESULT_OK && uri != null) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    runCatching {
                        contentResolver.openInputStream(uri)!!.use { it.readBytes() }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context: Context = this
        val root = LinearLayout(context).also { root ->
            root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            root.orientation = LinearLayout.VERTICAL
            root.gravity = Gravity.CENTER_VERTICAL
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
                root.addView(view)
            }
        }
        setContentView(root)
    }
}
