package pers.zy.gallerylib.config

import pers.zy.gallerylib.ui.GalleryMediaLoader

/**
 * date: 4/20/21   time: 11:07 AM
 * author zy
 * Have a nice day :)
 **/

class MediaInfoConfig {
    companion object {
        @JvmStatic var minMediaCount: Int = 1 //最少选择文件数量
        @JvmStatic var maxMediaCount: Int = 9 //最大选择文件数量
        @JvmStatic var mimeType: Int = GalleryMediaLoader.MIME_TYPE_ALL
        /** 选择文件类型:
         *  图片 @see[GalleryMediaLoader.MIME_TYPE_IMAGE]
         *  视频 @see[GalleryMediaLoader.MIME_TYPE_VIDEO]
         *  全部 @see[GalleryMediaLoader.MIME_TYPE_ALL]
         **/
        @JvmStatic var showCamera: Boolean = true //列表是否显示相机
        @JvmStatic var columnCount: Int = 4 //列表每行图片数量
        @JvmStatic var targetName: String = "" //result回调接收类的名字
        @JvmStatic var clickPreview: Boolean = true //点击图片是否预览
        @JvmStatic var showGif: Boolean = false //是否显示gif
        @JvmStatic var pagingLoad: Boolean = false //是否分页加载
        @JvmStatic var perPage: Int = 200 //分页加载情况下，每页加载个数
    }
}