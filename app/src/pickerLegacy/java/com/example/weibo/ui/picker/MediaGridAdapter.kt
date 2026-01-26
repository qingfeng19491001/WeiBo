package com.example.weibo.ui.picker

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.weibo.R
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

/**
 * 媒体网格适配器
 * 完全复原backup模块的MediaGridAdapter功能
 */
class MediaGridAdapter(
    private val context: Context,
    private var mediaList: MutableList<MediaItem>,
    private val onItemClick: (MediaItem?) -> Unit
) : RecyclerView.Adapter<MediaGridAdapter.ViewHolder>() {

    companion object {
        const val PAYLOAD_SELECTION_ONLY = "selection_only"
        private const val VIEW_TYPE_CAMERA = 0
        private const val VIEW_TYPE_MEDIA = 1
    }

    private val selectedItems: MutableList<MediaItem> = (context as? ImagePickerActivity)?.selectedItems
        ?: mutableListOf()

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return if (position == 0) {
            Long.MIN_VALUE
        } else {
            mediaList[position - 1].uri.toString().hashCode().toLong()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_CAMERA else VIEW_TYPE_MEDIA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_media_grid, parent, false)
        return ViewHolder(this, view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_CAMERA) {
            holder.bindCamera()
        } else {
            holder.bindMedia(mediaList[position - 1])
        }
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty() && payloads.contains(PAYLOAD_SELECTION_ONLY)) {
            if (getItemViewType(position) == VIEW_TYPE_MEDIA) {
                holder.bindSelectionOnly(mediaList[position - 1])
                return
            }
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun getItemCount(): Int = mediaList.size + 1

    fun updateMedia(newMedia: List<MediaItem>) {
        // Use DiffUtil in a real project for better performance
        val oldSize = mediaList.size
        mediaList.clear()
        notifyItemRangeRemoved(1, oldSize)
        mediaList.addAll(newMedia)
        notifyItemRangeInserted(1, newMedia.size)
    }

    fun findAdapterPositionForItem(item: MediaItem): Int {
        val index = mediaList.indexOfFirst { it.uri == item.uri }
        return if (index >= 0) index + 1 else -1
    }

    fun notifySelectionOrdersChanged(changedPosition: Int) {
        var minAffectedPos = Int.MAX_VALUE
        var maxAffectedPos = -1

        if (changedPosition != -1) {
            minAffectedPos = min(Int.MAX_VALUE, changedPosition)
        }

        selectedItems.forEach { selected ->
            val pos = findAdapterPositionForItem(selected)
            if (pos != -1) {
                maxAffectedPos = max(maxAffectedPos, pos)
                if (pos >= changedPosition) {
                    minAffectedPos = min(minAffectedPos, pos)
                }
            }
        }

        if (maxAffectedPos == -1 || minAffectedPos == Int.MAX_VALUE) {
            return
        }

        if (minAffectedPos > maxAffectedPos) {
            return
        }

        selectedItems.forEach { selected ->
            val pos = findAdapterPositionForItem(selected)
            if (pos != -1 && pos in minAffectedPos..maxAffectedPos && pos != changedPosition) {
                notifyItemChanged(pos, PAYLOAD_SELECTION_ONLY)
            }
        }
    }

    class ViewHolder(
        private val adapter: MediaGridAdapter,
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val ivThumb: ImageView = itemView.findViewById(R.id.iv_thumb)
        private val tvDuration: TextView = itemView.findViewById(R.id.tv_duration)
        private val selectionOverlay: View = itemView.findViewById(R.id.selection_overlay)
        private val tvSelectionIndicator: TextView = itemView.findViewById(R.id.tv_selection_indicator)
        private val cameraLayout: View = itemView.findViewById(R.id.camera_layout)

        init {
            itemView.post {
                val layoutParams = itemView.layoutParams
                layoutParams.height = itemView.width
                itemView.layoutParams = layoutParams
            }
        }

        fun bindCamera() {
            cameraLayout.visibility = View.VISIBLE
            ivThumb.visibility = View.GONE
            tvDuration.visibility = View.GONE
            selectionOverlay.visibility = View.GONE
            tvSelectionIndicator.visibility = View.GONE
            itemView.setOnClickListener {
                adapter.onItemClick(null)
            }
        }

        fun bindMedia(item: MediaItem) {
            cameraLayout.visibility = View.GONE
            ivThumb.visibility = View.VISIBLE

            Glide.with(adapter.context)
                .load(item.uri)
                .centerCrop()
                .placeholder(R.drawable.rounded_gray_bg)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("MediaGridAdapter", "Glide load failed for uri: ${item.uri}", e)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean = false
                })
                .into(ivThumb)

            if (item.isVideo) {
                tvDuration.visibility = View.VISIBLE
                tvDuration.text = formatDuration(item.duration)
            } else {
                tvDuration.visibility = View.GONE
            }

            val isSelected = adapter.selectedItems.contains(item)
            if (isSelected) {
                selectionOverlay.visibility = View.VISIBLE
                tvSelectionIndicator.visibility = View.VISIBLE
                tvSelectionIndicator.text = adapter.context.getString(
                    R.string.media_grid_selection_order,
                    adapter.selectedItems.indexOf(item) + 1
                )
            } else {
                selectionOverlay.visibility = View.GONE
                tvSelectionIndicator.visibility = View.GONE
            }

            itemView.setOnClickListener {
                adapter.onItemClick(item)
            }
        }

        fun bindSelectionOnly(item: MediaItem) {
            val isSelected = adapter.selectedItems.contains(item)
            if (isSelected) {
                selectionOverlay.visibility = View.VISIBLE
                tvSelectionIndicator.visibility = View.VISIBLE
                tvSelectionIndicator.text = adapter.context.getString(
                    R.string.media_grid_selection_order,
                    adapter.selectedItems.indexOf(item) + 1
                )
            } else {
                selectionOverlay.visibility = View.GONE
                tvSelectionIndicator.visibility = View.GONE
            }
        }

        private fun formatDuration(ms: Long): String {
            val seconds = ms / 1000
            val s = seconds % 60
            val m = (seconds / 60) % 60
            return String.format(Locale.getDefault(), "%02d:%02d", m, s)
        }
    }
}








