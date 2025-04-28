package com.mykaimeal.planner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.databinding.AdapterAddItemsWithDeleteIconBinding
import com.mykaimeal.planner.model.DataModel

class AddMoreItemsAdapter(var context: Context, private var itemList: MutableList<DataModel>,private var onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<AddMoreItemsAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: AdapterAddItemsWithDeleteIconBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DataModel, position: Int) {
            with(binding) {
                // Bind data to views

                tvLabel.text = item.title
                tvCounter.text = item.description

                imageDelete.setOnClickListener{
                    onItemClickListener.itemClick(position,"2","")
                }

                imageMinus.setOnClickListener{
                    if (itemList[position].value!=1){
                        val itemList1: MutableList<DataModel> = itemList
                        itemList1[position].value=tvCounter.text.toString().toInt() - 1
                    }
                }

                imagePlus.setOnClickListener{
                    val itemList1: MutableList<DataModel> = itemList
                    itemList1[position].value=tvCounter.text.toString().toInt() + 1
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdapterAddItemsWithDeleteIconBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position], position)
    }

    override fun getItemCount(): Int = itemList.size

    // Add items dynamically
    fun addItems(newItems: List<DataModel>) {
        val startPosition = itemList.size
        itemList.addAll(newItems)
        notifyItemRangeInserted(startPosition, newItems.size)
    }
}