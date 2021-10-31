package com.tienuu.demostaggeredlistview.data

import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.base.baselibrary.utils.media_provider.MediaInfo
import com.base.baselibrary.utils.media_provider.MediaModelBase
import lib.tienuu.staggeredlistview.StaggeredData

class AppImage(
    @MediaInfo(MediaStore.Images.ImageColumns._ID)
    val id: Long = 0,
    @MediaInfo(MediaStore.Images.ImageColumns.DATA)
    val path: String = "", val width: Int = 0, val height: Int = 0
) :
    MediaModelBase(), StaggeredData {

    var imageUri: Uri? = null

    override fun getUri(): Uri {
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    override fun getRatio(): Float {
        return if (height == 0) 1f else width.toFloat() / height
    }

    fun getPathForVisible(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            imageUri?.toString() ?: ""
        } else
            path
    }
}