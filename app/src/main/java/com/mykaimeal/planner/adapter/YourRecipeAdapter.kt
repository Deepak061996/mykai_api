package com.mykaimeal.planner.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterLayoutYourRecipeItemBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketyourrecipe.model.Dinner

class YourRecipeAdapter(private var yourRecipesData: MutableList<Dinner>,
                        private var requireActivity: FragmentActivity,
                        private var onItemSelectListener: OnItemSelectListener, private var type:String):
    RecyclerView.Adapter<YourRecipeAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterLayoutYourRecipeItemBinding = AdapterLayoutYourRecipeItemBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data= yourRecipesData.get(position)

        if (data != null) {

            if (data.data!=null){
                if (data.data.recipe!=null){
                    if (data.data.recipe.label!=null){
                        holder.binding.tvFoodName.text=data.data.recipe?.label
                    }
                }
            }


            if (data.data?.recipe?.images?.SMALL?.url!=null){
                Glide.with(requireActivity)
                    .load(data.data.recipe.images.SMALL.url)
                    .error(R.drawable.no_image)
                    .placeholder(R.drawable.no_image)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            holder.binding.layProgess.root.visibility= View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            holder.binding.layProgess.root.visibility= View.GONE
                            return false
                        }
                    })
                    .into(holder.binding.imageFood)
            }else{
                holder.binding.layProgess.root.visibility= View.GONE
            }

            if (data.serving!=null){
                holder.binding.tvServes.text="Serves "+data.serving.toString()
            }
        }

        holder.binding.imageMinusItem.setOnClickListener{
            if (yourRecipesData.get(position).serving.toString().toInt() > 1) {
                onItemSelectListener.itemSelect(position,"Minus",type)
            }else{
                Toast.makeText(requireActivity,"Minimum serving at least value is one", Toast.LENGTH_LONG).show()
            }
        }


        holder.binding.imagePlusItem.setOnClickListener{
            if (yourRecipesData.get(position).serving.toString().toInt() < 99) {
                onItemSelectListener.itemSelect(position,"Plus",type)
            }
        }

        holder.binding.imageCross.setOnClickListener{
            onItemSelectListener.itemSelect(position, data.basket_id.toString(),type)
        }

    }

    fun removeItem(position: Int) {
        yourRecipesData.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, yourRecipesData.size) // Optional, updates the positions of remaining items
    }

    override fun getItemCount(): Int {
        return yourRecipesData.size
    }

    fun updateList(mealList: MutableList<Dinner>) {
        yourRecipesData=mealList
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: AdapterLayoutYourRecipeItemBinding) : RecyclerView.ViewHolder(binding.root){
    }

}