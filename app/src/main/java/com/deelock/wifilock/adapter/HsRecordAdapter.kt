package com.deelock.wifilock.adapter

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.deelock.wifilock.R
import com.deelock.wifilock.entity.HsRecord
import com.deelock.wifilock.overwrite.ScrollItem

/**
 * Created by forgive for on 2018\6\29 0029.
 */
class HsRecordAdapter(private var data: List<HsRecord>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val typeTitle = 1
    private val typeItem = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent!!.context)
        return if (viewType == typeTitle){
            TitleHolder(inflater.inflate(R.layout.item_hs_record_title, parent, false))
        } else{
            ItemHolder(inflater.inflate(R.layout.item_hs_record_item, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TitleHolder){
            holder.titleTv.text = data[position].month
        } else if (holder is ItemHolder){
            holder.nameTv.text = data[position].openName

            when(data[position].type){
                0 -> holder.typeIv.setImageResource(R.mipmap.password_once)
                1 -> holder.typeIv.setImageResource(R.mipmap.time_password)
                2 -> holder.typeIv.setImageResource(R.mipmap.password_loop)
            }

            holder.deleteIb.setOnClickListener {  }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        if (data[position].isTitle == typeTitle){
            return typeTitle
        }
        return typeItem
    }

    inner class TitleHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleTv: TextView = itemView.findViewById(R.id.titleTv)
    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var si: ScrollItem = itemView.findViewById(R.id.si)
        var nameTv: TextView = itemView.findViewById(R.id.nameTv)
        var typeIv: ImageView = itemView.findViewById(R.id.typeIv)
        var periodTv: TextView = itemView.findViewById(R.id.periodTv)
        var deleteIb: ImageButton = itemView.findViewById(R.id.deleteIb)
    }
}