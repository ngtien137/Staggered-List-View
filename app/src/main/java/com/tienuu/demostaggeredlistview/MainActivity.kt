package com.tienuu.demostaggeredlistview

import android.Manifest
import com.base.baselibrary.activity.BaseActivity
import com.tienuu.demostaggeredlistview.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {

    val listPermission = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    fun grantPermissionStorage(onAllow: () -> Unit) {
        doRequestPermission(listPermission, onAllow)
    }

}