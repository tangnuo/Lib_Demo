package com.caowj.lib_logs.ui.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.caowj.lib_logs.R
import com.caowj.lib_logs.ui.LogFileInfo

class LogContentAdapter : RecyclerView.Adapter<LogContentAdapter.RecyclerHolder> {

    //    private val dataList: MutableList<String> = ArrayList()
    var mContext: Context
    private var mOnItemCheckedListener: OnRecyclerViewCheckedChangeListener? = null
    private var mOnItemClickListener: OnItemClickListener? = null
    var mItemCount = 0
    var mPageData: PageData

    constructor(context: Context, pageData: PageData) : super() {
        mContext = context
        mPageData = pageData
    }

    fun setItemCount(itemCount: Int) {
        mItemCount = itemCount
    }
//    fun addItemCount(itemCount:Int){
//        mItemCount+=itemCount;
//    }
//    fun setData(dataList: List<String>?) {
//        if (null != dataList) {
//            this.dataList.clear()
//            this.dataList.addAll(dataList)
//            notifyDataSetChanged()
//        }else{
//            this.dataList.clear()
//            notifyDataSetChanged()
//        }
//       if( this.dataList.size>0){
//
//       }
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_log_content, parent, false)
        return RecyclerHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {

        var info = mPageData.getData(position, mItemCount)
        holder.logTv.text = info

    }

    override fun getItemCount(): Int {
        return mItemCount
    }


    fun setOnCheckedChangeListener(listener: OnRecyclerViewCheckedChangeListener?) {
        mOnItemCheckedListener = listener
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        mOnItemClickListener = listener
    }

    interface OnRecyclerViewCheckedChangeListener {
        fun onItemClick(view: View, data: LogFileInfo, isChecked: Boolean)
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, data: LogFileInfo)
    }

    class RecyclerHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var logTv: TextView

        init {
            logTv = itemView.findViewById(R.id.tv_log)

        }
    }


}