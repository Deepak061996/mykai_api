package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.content.Context
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
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.productpaymentscreen.model.GetCardMealMeModelData
import com.mykaimeal.planner.listener.CardBankListener

class AdapterPaymentCreditDebitItem(
    var context: Context,
    var itemList: MutableList<GetCardMealMeModelData>?, var onCardBankListener: CardBankListener
) : RecyclerView.Adapter<AdapterPaymentCreditDebitItem.ViewHolder>() {

    inner class ViewHolder(private val binding: AdapterBankNameLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: GetCardMealMeModelData, position: Int) {

            if (item.card_num != null) {
                binding.textDes.text = "**** **** **** " + item.card_num
            }
            binding.imageIcon.setImageResource(R.drawable.ic_card_number_icon)

            /*binding.select.setOnClickListener {
                onCardBankListener.itemSelect(position, "", "")
            }
*/
            binding.imageThreeDots.setOnClickListener {
                showPopup(position, binding.imageThreeDots, item.status)
            }

        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showPopup(position: Int, imageThreeDots: LinearLayout, status: Int?) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val popupView: View? = inflater?.inflate(R.layout.item_card_delete_preferred_layout, null)
        val popupWindow =
            PopupWindow(popupView, 400, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindow.showAsDropDown(imageThreeDots, 0, 0, Gravity.END)

        // Access views inside the inflated layout using findViewById
        val relPreferred = popupView?.findViewById<RelativeLayout>(R.id.relPreferred)
        val relDelete = popupView?.findViewById<RelativeLayout>(R.id.relDelete)

        if (status == 1) {
            relPreferred?.visibility = View.GONE
        } else {
            relPreferred?.visibility = View.VISIBLE
        }

        relDelete?.setOnClickListener {
            popupWindow.dismiss()
            onCardBankListener.itemSelect(position, itemList?.get(position)?.id.toString(), "delete")
        }

        relPreferred?.setOnClickListener {
            popupWindow.dismiss()
            onCardBankListener.itemSelect(position, itemList?.get(position)?.id.toString(), "preferred")

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            AdapterBankNameLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        holder.bind(itemList!![position], position)
    }


    @SuppressLint("NotifyDataSetChanged")
    fun upDateList(item: MutableList<GetCardMealMeModelData>) {
        itemList = item
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = itemList!!.size

}