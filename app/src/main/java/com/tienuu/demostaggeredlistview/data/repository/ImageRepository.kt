package com.tienuu.demostaggeredlistview.data.repository

import android.content.ContentUris
import android.provider.MediaStore
import com.base.baselibrary.utils.getApplication
import com.base.baselibrary.utils.media_provider.getMedia
import com.bumptech.glide.Glide
import com.tienuu.demostaggeredlistview.data.AppImage
import java.io.File
import java.lang.Exception

class ImageRepository {

    fun loadImages(pageSize: Int = -1): ArrayList<AppImage> {
        val images = arrayListOf<AppImage>()
        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        getApplication().getMedia<AppImage>(onCheckIfAddItem = { currentList, image ->
            val isAddFileToList = File(image.path).exists()
            if (isAddFileToList) {
                image.imageUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    image.id
                )
                var width = 0
                var height = 0
                try {
                    val bitmap =
                        Glide.with(getApplication()).asBitmap().load(image.imageUri).submit().get()
                    width = bitmap.width
                    height = bitmap.height
                    bitmap.recycle()
                } catch (e: Exception) {
                }
                images.add(AppImage(image.id, image.path, width, height).also {
                    it.imageUri = image.imageUri
                })
            }
            isAddFileToList

        }, onCheckContinueLoad = { currentList, item ->
            if (pageSize == -1) true else currentList.size < pageSize
        }, sortOrder = sortOrder)
        return images
    }

}