package com.first.mywallpapers

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_image.view.*
import java.io.InputStream


class ImageAdapter(private val items: Array<String>, private val context: Context?) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_image, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val inputStream: InputStream = context!!.assets.open(items[position])
            val drawable = Drawable.createFromStream(inputStream, null)
            holder.image.setImageDrawable(drawable)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.image
    }
}