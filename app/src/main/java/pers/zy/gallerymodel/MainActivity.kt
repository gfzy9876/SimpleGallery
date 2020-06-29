package pers.zy.gallerymodel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import pers.zy.gallarylib.gallery.ui.GalleryMediaActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun loadImage(view: View) {
        GalleryMediaActivity.startShowImage(this)
    }

    fun loadVideo(view: View) {
        GalleryMediaActivity.startShowVideo(this)
    }
}