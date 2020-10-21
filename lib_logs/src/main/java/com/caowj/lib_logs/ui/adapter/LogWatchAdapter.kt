package com.caowj.lib_logs.ui.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.caowj.lib_logs.R
import com.caowj.lib_logs.ui.LogFileInfo
import java.util.*

class LogWatchAdapter : RecyclerView.Adapter<LogWatchAdapter.RecyclerHolder> {

    private val dataList: MutableList<String> = ArrayList()
    var mContext: Context
    private var mOnItemCheckedListener: OnRecyclerViewCheckedChangeListener? = null
    private var mOnItemClickListener: OnItemClickListener? = null

    constructor(context: Context) : super() {
        mContext = context
    }


    fun setData(dataList: List<String>?) {
        if (null != dataList) {
            this.dataList.clear()
            this.dataList.addAll(dataList)
            notifyDataSetChanged()
        } else {
            this.dataList.clear()
            notifyDataSetChanged()
        }
    }

    fun addData(line: String) {
        this.dataList.add(line)
        var removeCount = 0
        if (this.dataList.size > 200) {
            while (this.dataList.size > 100) {
                this.dataList.removeAt(0)
                removeCount++
            }

        }

        if (removeCount > 0) {
            notifyItemRangeRemoved(0, removeCount)
        }

        notifyItemInserted(dataList.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_log_watch, parent, false)
        return RecyclerHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {

        var info = dataList[position]
        holder.tvLog.text = info

    }

    override fun getItemCount(): Int {
        return dataList.size
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

        var tvLog: TextView

        init {
            tvLog = itemView.findViewById(R.id.tv_log)

        }
    }


}