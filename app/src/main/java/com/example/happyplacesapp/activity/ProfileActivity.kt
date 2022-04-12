package com.example.happyplacesapp.activity

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.happyplacesapp.R
import com.example.happyplacesapp.databinding.ActivityProfileBinding
import com.example.happyplacesapp.room.HappyPlaceApp
import com.example.happyplacesapp.room.HappyPlaceDao
import com.example.happyplacesapp.room.UserProfileEntity
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private var binding: ActivityProfileBinding? = null
    private var mUserDetails: UserProfileEntity? = null

    private companion object {
        private const val IMAGE_DIRECTORY = "HappyPlaceImage"
    }

    private var saveImageToInternalStorage: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.plump_purple)

        supportActionBar()

        val happyPlaceDao = (application as HappyPlaceApp).db.happyPlaceDao()

        binding?.civUserProfile?.setOnClickListener {
            choosePictureFromGallery()
        }
        if (intent.hasExtra(MainActivity.PROFILE_DETAILS)) {
            mUserDetails =
                intent.getParcelableExtra(MainActivity.PROFILE_DETAILS) as? UserProfileEntity
        }

        if (mUserDetails != null) {

            binding?.civUserProfile?.setImageURI(Uri.parse(mUserDetails!!.userImage))
            binding?.etUserName?.setText(mUserDetails!!.userName)

        }


        binding?.btnUpdate?.setOnClickListener {
            when {
                binding?.etUserName?.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "please enter name", Toast.LENGTH_LONG).show()
                }
                saveImageToInternalStorage == null -> {
                    Toast.makeText(this, "please choose picture", Toast.LENGTH_LONG).show()
                }

                else -> {
                    if (mUserDetails == null) {
                        Toast.makeText(this,"Profile Updated Successfully",Toast.LENGTH_LONG).show()
                        addProfileDetailsToRoom(happyPlaceDao)
                    } else {
                        Toast.makeText(this,"Profile Updated Successfully",Toast.LENGTH_LONG).show()
                        updateUserProfile(happyPlaceDao, mUserDetails!!.userId)

                    }
                }

            }
        }

    }


    private fun supportActionBar() {
        setSupportActionBar(binding?.toolbarProfileActivity)

        if (supportActionBar != null) {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        }
        binding?.toolbarProfileActivity?.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    private fun choosePictureFromGallery() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryResultLauncher.launch(intent)
                }
                if (report.isAnyPermissionPermanentlyDenied) {
                    showRationalDialogForPermission()
                }

            }

            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) {
                token?.continuePermissionRequest()

            }
        }).onSameThread().check()
    }

    @Suppress("Deprecation")
    private val galleryResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {

            val data: Intent? = result.data

            if (data != null) {

                val contentUri = data.data

                try {
                    contentUri?.let {
                        if (Build.VERSION.SDK_INT < 28) {
                            val galleryBitmap =
                                MediaStore.Images.Media.getBitmap(this.contentResolver, it)
                            saveImageToInternalStorage = saveImageToInternalStorage(galleryBitmap)
                            binding?.civUserProfile?.setImageURI(saveImageToInternalStorage)
                        } else {
                            val source = ImageDecoder.createSource(this.contentResolver, it)
                            val galleryBitmap = ImageDecoder.decodeBitmap(source)
                            saveImageToInternalStorage = saveImageToInternalStorage(galleryBitmap)
                            binding?.civUserProfile?.setImageURI(saveImageToInternalStorage)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to load image from gallery", Toast.LENGTH_LONG)
                        .show()
                }

            }
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {

        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")


        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    private fun showRationalDialogForPermission() {
        val rationalDialog = AlertDialog.Builder(this).setMessage(
            "it is look you have turned off permission" +
                    " required for this application." +
                    "It can be enabled under the application settings"
        ).setPositiveButton("GO TO SETTINGS") { _, _ ->
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }.setNegativeButton("SKIP") { dialog, _ ->
            dialog.dismiss()
        }
        rationalDialog.show()

    }

    private fun addProfileDetailsToRoom(happyPlaceDao: HappyPlaceDao) {

        val name = binding?.etUserName?.text.toString()
        val userImage = saveImageToInternalStorage.toString()

        lifecycleScope.launch {
            happyPlaceDao.insert(UserProfileEntity(userName = name, userImage = userImage))
        }

        profileUpdateSuccess()

    }


    private fun updateUserProfile(happyPlaceDao: HappyPlaceDao, userId: Int) {

        val name = binding?.etUserName?.text.toString()
        val userImage = saveImageToInternalStorage.toString()

        lifecycleScope.launch {
            happyPlaceDao.update(
                UserProfileEntity(
                    userId = userId,
                    userImage = userImage,
                    userName = name
                )
            )
        }

        profileUpdateSuccess()

    }

    private fun profileUpdateSuccess() {
        setResult(Activity.RESULT_OK)
        finish()
    }


}