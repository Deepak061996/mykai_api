package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterBankNameLayoutBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponsecard.CardData
import com.mykaimeal.planner.listener.CardBankListener

class MyWalletAdapter(var context: Context, var itemList: MutableList<CardData>, var type:String, var onCardBankListener: CardBankListener) : RecyclerView.Adapter<MyWalletAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: AdapterBankNameLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: CardData, position: Int) {

                if (item.name!=null){
                    binding.textName.text = item.name
                }

                if (item.last4!=null){
                    binding.textDes.text = "**** **** **** "+item.last4
                }
                binding.imageIcon.setImageResource(R.drawable.ic_card_number_icon)

            binding.select.setOnClickListener {
                onCardBankListener.itemSelect(position,type,type)
            }

            binding.imageThreeDots.setOnClickListener {
                showPopup(position,binding.imageThreeDots)
            }


        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showPopup(position: Int, imageThreeDots: LinearLayout) {
        val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val popupView: View? = inflater?.inflate(R.layout.item_select_layout, null)
        val popupWindow = PopupWindow(popupView, 400, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.showAsDropDown(imageThreeDots,  0, 0, Gravity.END)

        // Access views inside the inflated layout using findViewById
        val rcyData = popupView?.findViewById<RelativeLayout>(R.id.reldelete)

        rcyData?.setOnClickListener {
            popupWindow.dismiss()
            onCardBankListener.itemSelect(position,type,"delete")

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdapterBankNameLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        holder.bind(itemList[position], position)
    }


    @SuppressLint("NotifyDataSetChanged")
    fun upDateList(item: MutableList<CardData>, typeData:String){
        itemList=item
        type=typeData
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = itemList.size

}