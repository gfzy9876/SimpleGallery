package pers.zy.gallerylib.ui.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import pers.zy.gallerylib.databinding.FragGalleryPreviewBinding
import pers.zy.gallerylib.model.MediaInfo
import pers.zy.gallerylib.ui.GalleryMediaLoader
import java.lang.RuntimeException

/**
 * date: 5/17/21   time: 10:29 AM
 * author zy
 * Have a nice day :)
 **/
class GalleryMediaPreviewFrag : Fragment() {

    companion object {
        const val EXTRA_MEDIA_INFO = "extra_media_info"

        fun newInstance(mediaInfo: MediaInfo): GalleryMediaPreviewFrag {
            val frag = GalleryMediaPreviewFrag()
            frag.arguments = Bundle().apply {
                putParcelable(EXTRA_MEDIA_INFO, mediaInfo)
            }
            return frag
        }
    }

    lateinit var binding: FragGalleryPreviewBinding
    lateinit var mediaInfo: MediaInfo
    lateinit var galleryMediaPreviewAct: GalleryMediaPreviewAct

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        binding = FragGalleryPreviewBinding.inflate(inflater)
        galleryMediaPreviewAct = requireActivity() as GalleryMediaPreviewAct
        mediaInfo = arguments?.getParcelable(EXTRA_MEDIA_INFO)!!
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (mediaInfo.mediaType) {
            GalleryMediaLoader.MEDIA_TYPE_IMAGE -> {
                bindMediaTypeImage()
            }
            GalleryMediaLoader.MEDIA_TYPE_VIDEO -> {
                bindMediaTypeVideo()
            }
            else -> {
                throw RuntimeException("wrong mediaType: ${mediaInfo.mediaType}")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        //TODO:ZY 关于视频播放各种case处理
    }

    private fun bindMediaTypeImage() {
        binding.ivPlayVideo.visibility = View.GONE
        binding.ivPlayVideo.setOnClickListener(null)
        bindIvPreview()
    }

    private fun bindMediaTypeVideo() {
        binding.ivPlayVideo.visibility = View.VISIBLE
        binding.ivPlayVideo.setOnClickListener {
            //TODO:ZY 播放视频
        }
        bindIvPreview()
    }

    private fun bindIvPreview() {
        Glide.with(this)
            .load(mediaInfo.contentUriPath)
            .into(binding.ivPreview)

        //TODO:ZY 超大图处理
//        Glide.with(this)
//            .load(mediaInfo.contentUriPath)
//            .into(object : CustomViewTarget<ImageView, Drawable>(binding.ivPreview) {
//                override fun onLoadFailed(errorDrawable: Drawable?) {}
//                override fun onResourceCleared(placeholder: Drawable?) {}
//                override fun onResourceReady(
//                    resource: Drawable,
//                    transition: Transition<in Drawable>?
//                ) {
//                    binding.ivPreview.setImageDrawable(resource)
//                }
//            })
    }
}