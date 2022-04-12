package com.example.happyplacesapp.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplacesapp.R
import com.example.happyplacesapp.adapter.HappyPlaceAdapter
import com.example.happyplacesapp.databinding.ActivityMainBinding
import com.example.happyplacesapp.databinding.NavHeaderLayoutBinding
import com.example.happyplacesapp.room.HappyPlaceApp
import com.example.happyplacesapp.room.HappyPlaceDao
import com.example.happyplacesapp.room.HappyPlaceEntity
import com.example.happyplacesapp.room.UserProfileEntity
import com.example.happyplacesapp.util.SwipeToEdit
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var binding: ActivityMainBinding? = null
    private lateinit var profileResultLauncher: ActivityResultLauncher<Intent>
    private var mUser: UserProfileEntity? = null


    companion object {
        const val HAPPY_PLACE_DETAILS = "happy_place_details"
        const val REQUEST_CODE = 1
        const val PROFILE_DETAILS = "profile_details"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.plump_purple)

        binding?.navView?.setNavigationItemSelectedListener(this)

        supportActionBar()

        binding?.mainAppBar?.fabAddHappyPlace?.setOnClickListener {
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivity(intent)
        }

        val mHappyPlaceDao = (application as HappyPlaceApp).db.happyPlaceDao()

        lifecycleScope.launch {

            mHappyPlaceDao.fetchAllHappyPlaces().collect {
                val list = ArrayList(it)
                showRecyclerView(list)
            }

        }

        profileResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {

                    fetchUser(mHappyPlaceDao)

                }
            }

        fetchUser(mHappyPlaceDao)

    }


    private fun supportActionBar() {

        setSupportActionBar(binding?.mainAppBar?.toolbarMainActivity)
        binding?.mainAppBar?.toolbarMainActivity?.setNavigationIcon(R.drawable.ic_baseline_menu_24)
        binding?.mainAppBar?.toolbarMainActivity?.setNavigationOnClickListener {
            binding?.drawerLayout?.openDrawer(GravityCompat.START)
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                if (mUser == null) {
                    val intent = Intent(this, ProfileActivity::class.java)
                    profileResultLauncher.launch(intent)
                } else {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra(PROFILE_DETAILS, mUser)
                    profileResultLauncher.launch(intent)
                }

            }
            R.id.nav_exit -> {
                finish()
            }
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showRecyclerView(happyPlaceList: ArrayList<HappyPlaceEntity>) {

        if (happyPlaceList.isNotEmpty()) {
            binding?.mainAppBar?.mainContent?.tvNoPlaceAvailable?.visibility = View.GONE
            binding?.mainAppBar?.mainContent?.rvHappyPlaceList?.visibility = View.VISIBLE

            val adapter = HappyPlaceAdapter(this, happyPlaceList)

            binding?.mainAppBar?.mainContent?.rvHappyPlaceList?.adapter = adapter
            binding?.mainAppBar?.mainContent?.rvHappyPlaceList?.layoutManager =
                LinearLayoutManager(this)

            adapter.onClickListener(object : HappyPlaceAdapter.OnClickListener {
                override fun onClick(position: Int, item: HappyPlaceEntity) {
                    val intent = Intent(this@MainActivity, HappyPlaceDetails::class.java)
                    intent.putExtra(HAPPY_PLACE_DETAILS, item)
                    startActivity(intent)
                }
            })

            val swipeToEditHandler = object : SwipeToEdit(this) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val adapterSwipe =
                        binding?.mainAppBar?.mainContent?.rvHappyPlaceList?.adapter as HappyPlaceAdapter
                    adapterSwipe.notifyItemEdit(
                        this@MainActivity, viewHolder.adapterPosition,
                        REQUEST_CODE
                    )
                }
            }

            val editItemTouchHelper = ItemTouchHelper(swipeToEditHandler)
            editItemTouchHelper.attachToRecyclerView(binding?.mainAppBar?.mainContent?.rvHappyPlaceList)


        } else {

            binding?.mainAppBar?.mainContent?.tvNoPlaceAvailable?.visibility = View.VISIBLE
            binding?.mainAppBar?.mainContent?.rvHappyPlaceList?.visibility = View.GONE

        }


    }


    private fun updateUser(user: UserProfileEntity) {


        val headerView = binding?.navView?.getHeaderView(0)
        val headerBinding = headerView?.let { NavHeaderLayoutBinding.bind(it) }

        headerBinding?.navHeaderUserName?.text = user.userName
        headerBinding?.navHeaderUserProfile?.setImageURI(Uri.parse(user.userImage))


    }

    private fun fetchUser(happyPlaceDao: HappyPlaceDao) {

        lifecycleScope.launch {
            happyPlaceDao.fetchUser().collect {
                mUser = it
                if (mUser != null) {
                    updateUser(mUser!!)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "please update your profile",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    }
}


