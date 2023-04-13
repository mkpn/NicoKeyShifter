package com.nicokeyshifter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class VideoAdapter(private val videos: List<Video>, private val onClick: () -> Unit) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]
        holder.titleTextView.text = video.title
        holder.viewCounterTextView.text = "Views: ${video.viewCounter}"
        Glide.with(holder.itemView)
            .load(video.thumbnailUrl)
            .into(holder.thumbnailImageView)
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    inner class VideoViewHolder(itemView: View, private val onClick: () -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        val thumbnailImageView: ImageView = itemView.findViewById(R.id.thumbnail_image_view)
        val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
        val viewCounterTextView: TextView = itemView.findViewById(R.id.view_counter_text_view)
        private val container: View = itemView.findViewById(R.id.container)

        init {
            container.setOnClickListener {
                onClick()
            }
        }
    }
}