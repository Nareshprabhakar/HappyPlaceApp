package com.example.happyplacesapp.activity

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.happyplacesapp.R
import com.example.happyplacesapp.databinding.ActivityHappyPlaceDetailsBinding
import com.example.happyplacesapp.room.HappyPlaceEntity

class HappyPlaceDetails : AppCompatActivity() {

    private var binding: ActivityHappyPlaceDetailsBinding? = null
    private var happyPlacesModel: HappyPlaceEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.plump_purple)

        binding?.ivBackArrow?.setOnClickListener {
            onBackPressed()
        }

        if (intent.hasExtra(MainActivity.HAPPY_PLACE_DETAILS)) {
            happyPlacesModel =
                intent.getParcelableExtra(MainActivity.HAPPY_PLACE_DETAILS) as? HappyPlaceEntity
        }


        if (happyPlacesModel != null) {

            binding?.civHappyPlace?.setImageURI(Uri.parse(happyPlacesModel!!.image))
            binding?.etTitleDetails?.setText(happyPlacesModel!!.title)
            binding?.etDescriptionDetails?.setText(happyPlacesModel!!.description)
            binding?.etLocationDetail?.setText(happyPlacesModel!!.location)

        }

    }
}