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
            /**
             * I'm using a Linearlayout with orientation vertical inside a scrollview
            So when i add a view to linearlayout, it's height will be increase though i has set it to another columns
            I try to fix it by set linearlayout height, but it's not working
            So for fixing it, i wrap a Relative with height is max height of columns
            Then linear layout will be perfect inside relative layout
             */
            val parentWrap =
                scrollView.findViewById<RelativeLayout>(R.id.parentWrap) //Wrap relative layout
            parentWrap.layoutParams =
                parentWrap.layoutParams.also { it.height = adapter!!.maxHeight }
            val itemLayoutParent = scrollView.findViewById<LinearLayoutCompat>(R.id.layoutParent)
            adapter?.data?.forEachIndexed(action = { index, staggeredData ->
                adapter!!.mapRowData[staggeredData]?.let { rowData ->
                    val positionX = rowData.columnIndex * widthItem
                    val positionY = rowData.offsetY
                    val itemBinding = adapter!!.getViewBinding(index)
                    itemBinding.root.layoutParams =
                        FrameLayout.LayoutParams(rowData.width, rowData.height)
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

        private val listColumns = ArrayList<DataColumn<Data>>()

        val mapBinding = HashMap<Int, ViewBinding>()

        val mapRowData = HashMap<Data, DataRow<Data>>()

        var maxHeight = 0
            private set

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