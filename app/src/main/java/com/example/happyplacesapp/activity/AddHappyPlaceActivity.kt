package com.example.happyplacesapp.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
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
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.happyplacesapp.R
import com.example.happyplacesapp.databinding.ActivityAddHappyPlaceBinding
import com.example.happyplacesapp.room.HappyPlaceApp
import com.example.happyplacesapp.room.HappyPlaceDao
import com.example.happyplacesapp.room.HappyPlaceEntity
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
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {

    private var binding: ActivityAddHappyPlaceBinding? = null
    private var mHappyPlaceModel: HappyPlaceEntity? = null

    private companion object {
        private const val IMAGE_DIRECTORY = "HappyPlaceImage"
    }

    private var saveImageToInternalStorage: Uri? = null

    //for datePicker dialog
    private var calender = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.plump_purple)

        binding?.ivBackArrow?.setOnClickListener {
            onBackPressed()
        }

        binding?.ivDelete?.visibility = View.INVISIBLE

        val happyPlaceDao = (application as HappyPlaceApp).db.happyPlaceDao()

        updateDate()

        dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calender.set(Calendar.YEAR, year)
            calender.set(Calendar.MONTH, month)
            calender.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDate()
        }

        if (intent.hasExtra(MainActivity.HAPPY_PLACE_DETAILS)) {
            mHappyPlaceModel =
                intent.getParcelableExtra(MainActivity.HAPPY_PLACE_DETAILS) as? HappyPlaceEntity
        }
        if (mHappyPlaceModel != null) {

            binding?.title?.text = "Edit Happy place"
            binding?.etTitle?.setText(mHappyPlaceModel!!.title)
            binding?.etDescription?.setText(mHappyPlaceModel!!.description)
            binding?.etDate?.setText(mHappyPlaceModel!!.date)
            binding?.etLocation?.setText(mHappyPlaceModel!!.location)
            saveImageToInternalStorage = Uri.parse(mHappyPlaceModel!!.image)
            binding?.ivPlaceImage?.setImageURI(Uri.parse(mHappyPlaceModel!!.image))
            binding?.ivDelete?.visibility = View.VISIBLE
            binding?.btnSave?.text = "UPDATE"

            binding?.ivDelete?.setOnClickListener {
                alertDialogueForDeletePlace(happyPlaceDao, mHappyPlaceModel!!.id)
            }

        }


        binding?.tvAddImage?.setOnClickListener(this)
        binding?.etDate?.setOnClickListener(this)


        binding?.btnSave?.setOnClickListener {
            when {
                binding?.etTitle?.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter title", Toast.LENGTH_LONG).show()
                }
                binding?.etDescription?.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter description", Toast.LENGTH_LONG).show()
                }
                binding?.etDate?.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter date", Toast.LENGTH_LONG).show()
                }
                binding?.etLocation?.text.isNullOrEmpty() -> {
                    Toast.makeText(this, "Please enter location", Toast.LENGTH_LONG).show()
                }
                saveImageToInternalStorage == null -> {
                    Toast.makeText(this, "please selected image", Toast.LENGTH_LONG).show()
                }

                else -> {
                    if (mHappyPlaceModel == null) {
                        Toast.makeText(this, "Place Added Successfully", Toast.LENGTH_LONG).show()
                        addHappyPlaceToRoom(happyPlaceDao)
                        finish()
                    } else {
                        Toast.makeText(this, "Place Updated Successfully ", Toast.LENGTH_LONG)
                            .show()
                        updateHappyPlaceToRoom(happyPlaceDao, mHappyPlaceModel!!.id)

                        finish()
                    }
                }

            }
        }


    }


    override fun onClick(view: View?) {

        when (view!!.id) {

            R.id.et_date -> {
                DatePickerDialog(
                    this,
                    dateSetListener,
                    calender.get(Calendar.YEAR),
                    calender.get(Calendar.MONTH),
                    calender.get(Calendar.DAY_OF_MONTH)
                ).show()

            }

            R.id.tv_add_image -> {

                val alertDialog = AlertDialog.Builder(this)
                alertDialog.setTitle("Select Action")
                val alertDialogItem = arrayOf("Gallery", "Camera")
                alertDialog.setItems(alertDialogItem) { _, which ->
                    when (which) {
                        0 -> choosePictureFromGallery()
                        1 -> choosePictureFromCamera()
                    }
                }
                alertDialog.show()
            }


        }
    }


    private fun updateDate() {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = sdf.format(calender.time)
        binding?.etDate?.setText(date)
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
                            binding?.ivPlaceImage?.setImageURI(saveImageToInternalStorage)
                        } else {
                            val source = ImageDecoder.createSource(this.contentResolver, it)
                            val galleryBitmap = ImageDecoder.decodeBitmap(source)
                            saveImageToInternalStorage = saveImageToInternalStorage(galleryBitmap)
                            binding?.ivPlaceImage?.setImageURI(saveImageToInternalStorage)
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

    private fun choosePictureFromCamera() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                if (report!!.areAllPermissionsGranted()) {

                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    cameraResultLauncher.launch(intent)
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

    private val cameraResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data

            if (data != null) {
                try {
                    val cameraBitMap = data.extras!!.get("data") as Bitmap
                    saveImageToInternalStorage = saveImageToInternalStorage(cameraBitMap)
                    binding?.ivPlaceImage?.setImageURI(saveImageToInternalStorage)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to load image from camera", Toast.LENGTH_LONG)
                        .show()
                }

            }
        }

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

    private fun addHappyPlaceToRoom(happyPlaceDao: HappyPlaceDao) {

        val title = binding?.etTitle?.text.toString()
        val description = binding?.etDescription?.text.toString()
        val date = binding?.etDate?.text.toString()
        val location = binding?.etLocation?.text.toString()
        val image = saveImageToInternalStorage.toString()

        lifecycleScope.launch {
            happyPlaceDao.insert(
                HappyPlaceEntity(
                    title = title,
                    description = description,
                    date = date,
                    location = location,
                    image = image
                )
            )
        }

    }

    private fun updateHappyPlaceToRoom(happyPlaceDao: HappyPlaceDao, id: Int) {
        val title = binding?.etTitle?.text.toString()
        val description = binding?.etDescription?.text.toString()
        val date = binding?.etDate?.text.toString()
        val location = binding?.etLocation?.text.toString()
        val image = saveImageToInternalStorage.toString()

        lifecycleScope.launch {
            happyPlaceDao.update(
                HappyPlaceEntity(
                    id = id,
                    title = title,
                    description = description,
                    date = date,
                    location = location,
                    image = image
                )
            )
        }

    }

    private fun alertDialogueForDeletePlace(happyPlaceDao: HappyPlaceDao, id: Int) {

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete")
        builder.setIcon(R.drawable.ic_baseline_report_problem_24)
        builder.setPositiveButton("yes") { dialogInterface, _ ->
            dialogInterface.dismiss()
            deletePlaceFromRoom(happyPlaceDao, id)
            finish()
        }
        builder.setNegativeButton("NO") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: android.app.AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

    }

    private fun deletePlaceFromRoom(happyPlaceDao: HappyPlaceDao, id: Int) {

        lifecycleScope.launch {
            happyPlaceDao.delete(
                HappyPlaceEntity(
                    id = id,
                    title = null,
                    description = null,
                    date = null,
                    location = null,
                    image = null
                )
            )
        }

    }


}