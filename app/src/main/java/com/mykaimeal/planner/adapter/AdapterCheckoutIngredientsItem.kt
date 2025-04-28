package com.mykaimeal.planner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.databinding.AdapterCheckoutIngredientsItemBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Ingredient
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.checkoutscreen.model.IngredientList

class AdapterCheckoutIngredientsItem(private var ingredientsData: MutableList<IngredientList>?,
                                     private var requireActivity: FragmentActivity
):
    RecyclerView.Adapter<AdapterCheckoutIngredientsItem.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterCheckoutIngredientsItemBinding = AdapterCheckoutIngredientsItemBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data= ingredientsData?.get(position)

        if (data != null) {
            if (data.sch_id!=null){
                holder.binding.tvIngQuantity.text=data.sch_id.toString()
            }

            if (data.pro_price!=null){
                if (data.pro_price!="Not available"){
                    holder.binding.tvFoodPrice.text=data.pro_price.toString()
                }else{
                    holder.binding.tvFoodPrice.text="$00"
                }
            }

            if (data.pro_name!=null){
                val foodName = data.pro_name
                val result = foodName.mapIndexed { index, c ->
                    if (index == 0 || c.isUpperCase()) c.uppercaseChar() else c
                }.joinToString("")
                holder.binding.tvFoodName.text=result
            }
        }
    }

    override fun getItemCount(): Int {
        return ingredientsData!!.size
    }

    fun updateList(list:MutableList<IngredientList>){
        ingredientsData=list
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: AdapterCheckoutIngredientsItemBinding) : RecyclerView.ViewHolder(binding.root){
    }
}