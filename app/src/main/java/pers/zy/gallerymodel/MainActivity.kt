package pers.zy.gallerymodel

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import pers.zy.gallarylib.gallery.GalleryActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun openGallery(view: View) {
        GalleryActivity.start(this)
    }
}