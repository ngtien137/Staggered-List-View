package com.tienuu.demostaggeredlistview.ui

import android.os.Bundle
import com.base.baselibrary.fragment.BaseFragment
import com.base.baselibrary.utils.observer
import com.base.baselibrary.viewmodel.autoViewModels
import com.base.baselibrary.views.ext.toast
import com.tienuu.demostaggeredlistview.MainActivity
import com.tienuu.demostaggeredlistview.R
import com.tienuu.demostaggeredlistview.data.AppImage
import com.tienuu.demostaggeredlistview.databinding.FragmentHomeBinding
import com.tienuu.demostaggeredlistview.databinding.ItemImageBinding
import lib.tienuu.staggeredlistview.StaggeredListView

class HomeFragment : BaseFragment<FragmentHomeBinding, MainActivity>() {

    private val viewModel by autoViewModels<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootActivity.grantPermissionStorage {
            viewModel.loadListImage(-1)
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_home
    }

    override fun initBinding() {
        binding.viewModel = viewModel
    }

    override fun initView() {
        val adapter =
            StaggeredListView.StaggeredAdapter<AppImage, ItemImageBinding>(
                requireContext(),
                R.layout.item_image
            )
        binding.staggeredListView.adapter = adapter
        observer(viewModel.liveListImage) {
            adapter.data = it ?: arrayListOf()
        }
    }

}