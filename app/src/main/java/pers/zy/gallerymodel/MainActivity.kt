package pers.zy.gallerymodel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import pers.zy.gallarylib.gallery.engine.GalleryObserver
import pers.zy.gallarylib.gallery.ui.GalleryMediaActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val handler = Handler()
        contentResolver.registerContentObserver(
            MediaStore.Files.getContentUri("external"),
            false,
            GalleryObserver(handler)
        )
    }

    fun loadImage(view: View) {
        GalleryMediaActivity.startShowImage(this)
    }

    fun loadVideo(view: View) {
        GalleryMediaActivity.startShowVideo(this)
    }

    fun loadAll(view: View) {
        GalleryMediaActivity.startShowAll(this)
    }
}