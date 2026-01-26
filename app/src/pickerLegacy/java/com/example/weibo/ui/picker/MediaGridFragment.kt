package com.example.weibo.ui.picker

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weibo.R

/**
 * 媒体网格Fragment
 * 完全复原backup模块的MediaGridFragment功能
 */
class MediaGridFragment : Fragment() {

    companion object {
        private const val ARG_MEDIA_TYPE = "media_type"

        fun newInstance(mediaType: MediaType): MediaGridFragment {
            return MediaGridFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_MEDIA_TYPE, mediaType)
                }
            }
        }
    }

    private lateinit var recyclerView: RecyclerView
    private var mediaAdapter: MediaGridAdapter? = null
    private var mediaType: MediaType = MediaType.ALL
    private var pendingMedia: List<MediaItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mediaType = it.getSerializable(ARG_MEDIA_TYPE) as? MediaType ?: MediaType.ALL
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_media_grid, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!::recyclerView.isInitialized) {
            recyclerView = view.findViewById(R.id.recycler_view)
            setupRecyclerView()
        }
        pendingMedia?.let {
            setMedia(it)
            pendingMedia = null
        }
    }

    private fun setupRecyclerView() {
        mediaAdapter = MediaGridAdapter(
            requireContext(),
            mutableListOf()
        ) { mediaItem ->
            val activity = activity as? ImagePickerActivity
            activity?.toggleSelection(mediaItem)
        }

        recyclerView.layoutManager = GridLayoutManager(context, 4)
        recyclerView.adapter = mediaAdapter
    }

    fun refreshSelectionChanged(changedItem: MediaItem) {
        mediaAdapter?.let { adapter ->
            val position = adapter.findAdapterPositionForItem(changedItem)
            if (position != -1) {
                adapter.notifyItemChanged(position, MediaGridAdapter.PAYLOAD_SELECTION_ONLY)
                adapter.notifySelectionOrdersChanged(position)
            }
        }
    }

    fun setMedia(media: List<MediaItem>) {
        if (mediaAdapter != null) {
            Log.d("MediaGridFragment", "Setting media for $mediaType: ${media.size} items")
            mediaAdapter?.updateMedia(media)
        } else {
            pendingMedia = media
        }
    }

    fun notifyItemChanged(position: Int) {
        mediaAdapter?.notifyItemChanged(position)
    }

    fun refreshAdapter() {
        mediaAdapter?.notifyDataSetChanged()
    }
}








