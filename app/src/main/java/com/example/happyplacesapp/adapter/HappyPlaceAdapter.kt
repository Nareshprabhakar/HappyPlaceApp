package com.example.happyplacesapp.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplacesapp.activity.AddHappyPlaceActivity
import com.example.happyplacesapp.activity.MainActivity
import com.example.happyplacesapp.databinding.ItemMainBinding
import com.example.happyplacesapp.room.HappyPlaceEntity

class HappyPlaceAdapter(
    private val context: Context,
    private val item: ArrayList<HappyPlaceEntity>
) : RecyclerView.Adapter<HappyPlaceAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null


    class ViewHolder(binding: ItemMainBinding) : RecyclerView.ViewHolder(binding.root) {
        var image = binding.ivPlaceImage
        var title = binding.tvTitle
        var description = binding.tvDescription
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMainBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = item[position]
        holder.image.setImageURI(Uri.parse(model.image))
        holder.title.text = model.title
        holder.description.text = model.description

        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, model)
            }
        }


    }

    override fun getItemCount(): Int {
        return item.size
    }

    fun onClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, item: HappyPlaceEntity)
    }

    fun notifyItemEdit(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddHappyPlaceActivity::class.java)
        intent.putExtra(MainActivity.HAPPY_PLACE_DETAILS, item[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }


}
