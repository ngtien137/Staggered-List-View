package lib.tienuu.staggeredlistview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView

class StaggeredListView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    companion object {
        const val TAG = "StaggeredListView"
        const val SHOW_LOG = true
    }

    val scrollView = LayoutInflater.from(context).inflate(R.layout.staggered_layout, null, false)

    var adapter: StaggeredAdapter<out StaggeredData, out ViewDataBinding>? = null
        set(value) {
            field = value
            value?.staggeredListView = this
            value?.invalidate()
        }

    var countOnLayout = 0
    private var currentSectionScroll = -1

    private var sectionOffset = 0

    val heightScreen by lazy {
        context.resources.displayMetrics.heightPixels
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        countOnLayout++
        loge("Count Measure: $countOnLayout")
        if (width == 0)
            return
        adapter?.data?.firstOrNull()?.let command@{
            val widthItem = width / adapter!!.span
            scrollView.layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            if (scrollView.parent != null) {
                (scrollView.parent as ViewGroup?)?.removeAllViews()
            }
            addView(scrollView)

            val parentWrap =
                scrollView.findViewById<RelativeLayout>(R.id.parentWrap) //Wrap relative layout
            parentWrap.layoutParams =
                parentWrap.layoutParams.also { it.height = adapter!!.maxHeight }
            val itemLayoutParent = scrollView.findViewById<FrameLayout>(R.id.layoutParent)

            (scrollView.findViewById<ScrollView>(R.id.scrollView)).viewTreeObserver.addOnScrollChangedListener {
                val scrollY = scrollView.scrollY
                val indexSection = scrollY / heightScreen
                loge("ParentSize: ${itemLayoutParent.childCount}, currentSection: $currentSectionScroll")
                if (indexSection != currentSectionScroll) {
                    currentSectionScroll = indexSection
                    adapter?.let { adapter ->
                        adapter.listSectionIndex[indexSection].forEach { mapIndex ->
                            loge("AddIndex: $mapIndex")
                            adapter.mapBinding[mapIndex]?.let { itemBinding ->
                                setBindingVisible(itemBinding, itemLayoutParent)
                            }
                        }
                        if (indexSection > 0) {
                            for (mapIndex in 0 until indexSection - sectionOffset) {
                                loge("RemoveIndex: $mapIndex")
                                adapter.mapBinding[mapIndex]?.let { itemBinding ->
                                    setBindingGone(itemBinding, itemLayoutParent)
                                }
                            }
                            if (sectionOffset > 0) {
                                //visible indexSection -offset
                            }
                        }
                        if (indexSection < adapter.listSectionIndex.size - 1) {
                            if (indexSection + 1 + sectionOffset < adapter.listSectionIndex.size)
                                for (mapIndex in indexSection + 1 + sectionOffset until adapter.listSectionIndex.size) {
                                    loge("RemoveIndex: $mapIndex")
                                    adapter.mapBinding[mapIndex]?.let { itemBinding ->
                                        setBindingGone(itemBinding, itemLayoutParent)
                                    }
                                }
                            if (sectionOffset > 0) {
                                //visible indexSection + offset
                            }
                        }
                    }
                }
            }

            adapter?.data?.forEachIndexed(action = { index, staggeredData ->
                adapter!!.mapRowData[staggeredData]?.let { rowData ->
                    val positionX = rowData.columnIndex * widthItem
                    val positionY = rowData.offsetY
                    val itemBinding = adapter!!.getViewBinding(index)
                    itemBinding.root.layoutParams =
                        LayoutParams(rowData.width, rowData.height)
                    itemBinding.root.x = positionX.toFloat()
                    itemBinding.root.y = positionY
                    if (itemBinding.root.parent != null) {
                        (itemBinding.root.parent as ViewGroup?)?.removeAllViews()
                    }
                    itemLayoutParent.addView(itemBinding.root)
                }
            })
            itemLayoutParent.layoutParams.height = adapter!!.maxHeight
        }
    }

    private fun <ViewBinding : ViewDataBinding> setBindingVisible(
        itemBinding: ViewBinding,
        itemLayoutParent: FrameLayout
    ) {
        if (itemBinding.root.parent != null) {
            if (itemBinding.root.parent != itemLayoutParent) {
                (itemBinding.root.parent as ViewGroup?)?.removeAllViews()
                itemLayoutParent.addView(itemBinding.root)
            }
        } else {
            itemLayoutParent.addView(itemBinding.root)
        }
    }

    private fun <ViewBinding : ViewDataBinding> setBindingGone(
        itemBinding: ViewBinding, itemLayoutParent: FrameLayout
    ) {
        itemLayoutParent.removeView(itemBinding.root)
    }

    fun loge(message: String) {
        if (SHOW_LOG) {
            Log.e(TAG, message)
        }
    }

    open class StaggeredAdapter<Data : StaggeredData, ViewBinding : ViewDataBinding>(
        private val context: Context,
        @LayoutRes private val layoutResource: Int,
        private val lifecycleOwner: LifecycleOwner? = null
    ) {

        internal var staggeredListView: StaggeredListView? = null

        /**
         * Current version i'm only calculate for span = 2, i'm not sure if it's working correctly with other values
         */
        var span = 2
            private set

        private val layoutInflater by lazy {
            LayoutInflater.from(context)
        }

        var data = ArrayList<Data>()
            set(value) {
                field = value
                invalidate()
            }

        val heightScreen by lazy {
            context.resources.displayMetrics.heightPixels
        }

        private val listColumns = ArrayList<DataColumn<Data>>()

        val mapBinding = HashMap<Int, ViewBinding>()

        val mapRowData = HashMap<Data, DataRow<Data>>()

        var maxHeight = 0
            private set

        val listSectionIndex = ArrayList<ArrayList<Int>>()

        private fun createItemBinding(): ViewBinding {
            return DataBindingUtil.inflate(layoutInflater, layoutResource, null, false)
        }

        fun getViewBinding(itemPosition: Int): ViewBinding {
            if (mapBinding[itemPosition] == null) {
                mapBinding[itemPosition] = createItemBinding()
            }
            val binding = mapBinding[itemPosition]!!
            binding.setVariable(BR.item, data[itemPosition])
            onConfigureWithBinding(binding, itemPosition)
            if (lifecycleOwner != null) {
                binding.lifecycleOwner = lifecycleOwner
            } else {
                if (context is LifecycleOwner) {
                    binding.lifecycleOwner = context
                }
            }
            binding.executePendingBindings()
            return binding
        }

        open fun onConfigureWithBinding(binding: ViewBinding, itemPosition: Int) {
        }

        fun getItem(position: Int): Data? {
            return data.getOrNull(position)
        }

        fun getCount() = data.size

        fun invalidate() {
            clearData()
            val widthView = staggeredListView?.width ?: 0
            if (!data.isNullOrEmpty() && widthView != 0) {
                val widthItem = widthView / span
                var sumHeight = 0f
                data.forEach { staggeredData ->
                    sumHeight += widthItem / staggeredData.getRatio()
                }
                //Chiều cao dự kiến của mỗi cột
                val heightPerColumns = sumHeight / span
                var col = 0
                var currentItemIndex = 0
                var heightOfColumns = 0f
                var currentOffset = 0f
                while (col < span && currentItemIndex < data.size) {
                    val staggeredData = data[currentItemIndex]
                    val heightOfThisItem = widthItem / staggeredData.getRatio()
                    val heightLess = sumHeight - heightOfColumns - heightOfThisItem
                    if (heightLess < heightOfColumns) {
                        col++
                        currentOffset = 0f
                        heightOfColumns = 0f
                    }
                    if (listColumns.size < col + 1) {
                        listColumns.add(DataColumn())
                    }
                    val rowData = DataRow<Data>(
                        col, currentOffset, widthItem,
                        heightOfThisItem.toInt()
                    )

                    //add list for checking scroll

                    val indexOfSectionScroll = currentOffset.toInt() / heightScreen
                    if (listSectionIndex.getOrNull(indexOfSectionScroll) == null) {
                        listSectionIndex.add(arrayListOf())
                    }
                    listSectionIndex[indexOfSectionScroll].add(currentItemIndex)

                    mapRowData[staggeredData] = rowData
                    listColumns[col].listData.add(staggeredData)
                    heightOfColumns += heightOfThisItem
                    currentItemIndex++

                    if (heightOfColumns >= heightPerColumns) {
                        if (heightOfColumns > maxHeight) {
                            maxHeight = heightOfColumns.toInt()
                        }
                        listColumns[col].height = heightOfColumns.toInt()
                        col++
                        currentOffset = 0f
                        heightOfColumns = 0f
                    } else {
                        currentOffset += heightOfThisItem
                    }
                }
                staggeredListView?.requestLayout()
            }
        }

        private fun clearData() {
            mapBinding.clear()
            listColumns.clear()
            mapRowData.clear()

        }

        class DataColumn<Data : StaggeredData>(
            var height: Int = 0,
            var listData: ArrayList<Data> = arrayListOf()
        )

        class DataRow<Data : StaggeredData>(
            var columnIndex: Int,
            var offsetY: Float,
            var width: Int,
            var height: Int
        )

    }

}