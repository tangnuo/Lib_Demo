package com.caowj.lib_logs.ui.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import com.caowj.lib_logs.R
import com.caowj.lib_logs.ui.LogFileInfo
import java.util.*

class LogFileListAdapter : RecyclerView.Adapter<LogFileListAdapter.RecyclerHolder> {

    private val dataList: MutableList<LogFileInfo> = ArrayList()
    var mContext : Context;
    private var mOnItemCheckedListener: OnRecyclerViewCheckedChangeListener? = null
    private var mOnItemClickListener: OnItemClickListener? = null

    constructor(context : Context) : super() {
        mContext = context;
    }



    fun setData(dataList: List<LogFileInfo>?) {
        if (null != dataList) {
            this.dataList.clear()
            this.dataList.addAll(dataList)
            notifyDataSetChanged()
        }else{
            this.dataList.clear()
            notifyDataSetChanged()
        }
    }
    fun getData():MutableList<LogFileInfo> {
        return   this.dataList;
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_log_file_list, parent, false)
        return RecyclerHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerHolder, position: Int) {

        var info = dataList[position]
        holder.fileName.setText(info.fileName)
        holder.fileSize.setText(info.displaySize)
        holder.checkBox.setChecked(info.isSelected)
        holder.checkBox.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean){
                mOnItemCheckedListener?.onItemClick(buttonView,info,isChecked)
            }
        })

        holder.itemView.setOnClickListener(object :View.OnClickListener{
            override fun onClick(v: View) {
              mOnItemClickListener?.onItemClick(v,info)
            }
        })
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
    class RecyclerHolder  constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var checkBox: CheckBox
        var fileName: TextView
        var fileSize: TextView
        init {
            checkBox = itemView.findViewById(R.id.checkbox)
            fileName = itemView.findViewById(R.id.tv_fileName)
            fileSize = itemView.findViewById(R.id.tv_fileSize)
        }
    }


}