package com.example.banderas

import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import com.example.banderas.databinding.ActivityMainBinding
import com.example.banderas.ml.Modelo
import org.tensorflow.lite.support.image.TensorImage

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var imageView: ImageView
    private lateinit var button: Button
    private lateinit var tvInfo: TextView
    private lateinit var tvPais: TextView
    private val GALLERY_REQUEST_CODE = 123


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        imageView = binding.imageView
        button = binding.btnCapture
        tvInfo = binding.txtInfo
        tvPais = binding.txtPais

        button.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                takePicturePreview.launch(null)
            } else {
                requestPermission.launch(android.Manifest.permission.CAMERA)
            }
        }

    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                takePicturePreview.launch(null)
            } else {
                Toast.makeText(this, "Permiso denegado !! Intente nuevamente", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    private val takePicturePreview =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
                outputGenerator(bitmap)
            }
        }

    private fun outputGenerator(bitmap: Bitmap) {
        val model = Modelo.newInstance(this)

// Creates inputs for reference.
        val newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true)
        val tfimage = TensorImage.fromBitmap(newBitmap)

// Runs model inference and gets result.
        val outputs = model.process(tfimage)
            .probabilityAsCategoryList.apply {
                sortByDescending { it.score }
            }

        val highprobabilityOutput = outputs[0]

        tvPais.text = highprobabilityOutput.label
        Log.i("TAG","outputGenerator: $highprobabilityOutput")

    }

}