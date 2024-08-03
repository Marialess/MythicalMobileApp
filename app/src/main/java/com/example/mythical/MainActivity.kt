package com.example.mythical

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.mythical.data.Product
import com.example.mythical.databinding.ActivityMainBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var selectedImages = mutableListOf<Uri>()
    private val selectedColors = mutableListOf<Int>()
    private val productStorage = Firebase.storage.reference
    private val firestore = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        requestPermissions()

        val selectImagesActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                Log.d("MainActivity", "Intent Data: ${result.data}")

                if (intent?.clipData != null) {
                    val count = intent.clipData?.itemCount ?: 0
                    (0 until count).forEach {
                        val imageUri = intent.clipData?.getItemAt(it)?.uri
                        imageUri?.let { selectedImages.add(it) }
                    }
                } else {
                    val imageUri = intent?.data
                    imageUri?.let { selectedImages.add(it) }
                }
                updateImages()
            } else {
                Log.d("MainActivity", "Result was not OK")
            }
        }

        binding.buttonImagesPicker.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                type = "image/*"
            }
            selectImagesActivityResult.launch(intent)
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionsForAndroid13AndAbove()
        } else {
            requestPermissionsForAndroidBelow13()
        }
    }

    private fun requestPermissionsForAndroidBelow13() {
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 0)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermissionsForAndroid13AndAbove() {
        val permissions = arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        )
        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 0)
        }
    }

    private fun updateImages() {
        binding.tvSelectedImages.text = selectedImages.size.toString()
    }

    private fun updateColors() {
        var colors = ""
        selectedColors.forEach {
            colors = "$colors ${Integer.toHexString(it)}"
        }
        binding.tvSelectedColors.text = colors
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.saveProduct) {
            val productValidation = validateInformation()
            if (!productValidation) {
                Toast.makeText(this, "Check your inputs", Toast.LENGTH_SHORT).show()
                return false
            }
            saveProduct()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveProduct() {
        val name = binding.edName.text.toString().trim()
        val category = binding.edCategory.text.toString().trim()
        val price = binding.edPrice.text.toString().trim()
        val offerPercentage = binding.offerPercentage.text.toString().trim()
        val description = binding.edDescription.text.toString().trim()
        val sizes = getSizedList(binding.edSizes.text.toString().trim())
        val imagesByteArrays = getImagesByteArrays()
        val images = mutableListOf<String>()

        lifecycleScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                showLoading()
            }

            try {
                imagesByteArrays.forEach {
                    val id = UUID.randomUUID().toString()
                    val imageStorage = productStorage.child("products/images/$id")
                    val result = imageStorage.putBytes(it).await()
                    val downloadUrl = result.storage.downloadUrl.await().toString()
                    images.add(downloadUrl)
                }

                val product = Product(
                    UUID.randomUUID().toString(),
                    name,
                    category,
                    price.toFloat(),
                    if (offerPercentage.isEmpty()) null else offerPercentage.toFloat(),
                    if (description.isEmpty()) null else description,
                    if (selectedColors.isEmpty()) null else selectedColors,
                    sizes,
                    images
                )

                firestore.collection("Products").add(product).addOnSuccessListener {
                    hideLoading()
                }.addOnFailureListener {
                    hideLoading()
                    Log.e("Error", it.message.toString())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    hideLoading()
                }
            }
        }
    }

    private fun hideLoading() {
        binding.progressbar.visibility = View.INVISIBLE
    }

    private fun showLoading() {
        binding.progressbar.visibility = View.VISIBLE
    }

    private fun getImagesByteArrays(): List<ByteArray> {
        val imagesByteArray = mutableListOf<ByteArray>()
        selectedImages.forEach {
            val stream = ByteArrayOutputStream()
            val imageBmp = MediaStore.Images.Media.getBitmap(contentResolver, it)
            if (imageBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)) {
                imagesByteArray.add(stream.toByteArray())
            }
        }
        return imagesByteArray
    }

    private fun getSizedList(sizeStr: String): List<String>? {
        if (sizeStr.isEmpty()) return null
        return sizeStr.split(",")
    }

    private fun validateInformation(): Boolean {
        if (binding.edPrice.text.toString().trim().isEmpty()) return false
        if (binding.edName.text.toString().trim().isEmpty()) return false
        if (binding.edCategory.text.toString().trim().isEmpty()) return false
        if (selectedImages.isEmpty()) return false

        return true
    }
}
