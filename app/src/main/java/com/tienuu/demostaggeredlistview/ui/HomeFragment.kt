package com.tienuu.demostaggeredlistview.ui

import android.os.Bundle
import com.base.baselibrary.fragment.BaseFragment
import com.base.baselibrary.utils.observer
import com.base.baselibrary.viewmodel.autoViewModels
import com.base.baselibrary.views.ext.loge
import com.base.baselibrary.views.ext.toast
import com.scwang.smart.refresh.layout.api.RefreshLayout
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener
import com.tienuu.demostaggeredlistview.MainActivity
import com.tienuu.demostaggeredlistview.R
import com.tienuu.demostaggeredlistview.adapter.ImageListener
import com.tienuu.demostaggeredlistview.data.AppImage
import com.tienuu.demostaggeredlistview.databinding.FragmentHomeBinding
import com.tienuu.demostaggeredlistview.databinding.ItemImageBinding
import lib.tienuu.staggeredlistview.StaggeredListView

class HomeFragment : BaseFragment<FragmentHomeBinding, MainActivity>(), ImageListener {

    private val viewModel by autoViewModels<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootActivity.grantPermissionStorage {
            viewModel.loadListImage()
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
            (object : StaggeredListView.StaggeredAdapter<AppImage, ItemImageBinding>(
                requireContext(),
                R.layout.item_image
            ) {
                override fun onConfigureWithBinding(binding: ItemImageBinding, itemPosition: Int) {
                    //This function like onBindViewHolder in RecyclerView.Adapter
                }
            }).also { adapter ->
                adapter.listener = this
            }
        binding.staggeredListView.adapter = adapter
        observer(viewModel.liveListImage) {
            binding.staggeredListView.post {
                adapter.data = it ?: arrayListOf()
            }
        }

        binding.refreshLayout.setOnRefreshLoadMoreListener(object : OnRefreshLoadMoreListener {
            override fun onRefresh(refreshLayout: RefreshLayout) {
                viewModel.loadListImage {
                    binding.refreshLayout.finishRefresh()
                }
            }

            override fun onLoadMore(refreshLayout: RefreshLayout) {
                viewModel.loadMoreListImage {
                    adapter.validateWithList()
                    binding.staggeredListView.validateWithAdapter()
                    binding.refreshLayout.finishLoadMore()
                }
            }

        })
    }

    override fun onImageClick(image: AppImage, itemPosition: Int) {
        loge("Click Pos: $itemPosition")
    }

}