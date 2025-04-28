package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
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
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Recipes
import com.mykaimeal.planner.messageclass.ErrorMessage

class BasketYourRecipeAdapter(private var yourRecipesData: MutableList<Recipes>?,
                              private var requireActivity: FragmentActivity,
                              private var onItemSelectListener: OnItemSelectListener):
    RecyclerView.Adapter<BasketYourRecipeAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterLayoutYourRecipeItemBinding = AdapterLayoutYourRecipeItemBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data= yourRecipesData?.get(position)

        if (data != null) {
            holder.binding.tvFoodName.text = data.data?.recipe?.label ?: ""

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
            if (yourRecipesData?.get(position)?.serving.toString().toInt() > 1) {
                onItemSelectListener.itemSelect(position,"Minus","YourRecipe")
            }else{
                Toast.makeText(requireActivity,ErrorMessage.servingError, Toast.LENGTH_LONG).show()
            }
        }


          holder.binding.imagePlusItem.setOnClickListener{
              if (yourRecipesData?.get(position)?.serving.toString().toInt() < 99) {
                  onItemSelectListener.itemSelect(position,"Plus","YourRecipe")
              }
        }

        holder.binding.imageCross.setOnClickListener{
            onItemSelectListener.itemSelect(position,"remove","YourRecipe")
        }

    }

    override fun getItemCount(): Int {
        return yourRecipesData!!.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(recipe: MutableList<Recipes>?) {
        yourRecipesData=recipe
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: AdapterLayoutYourRecipeItemBinding) : RecyclerView.ViewHolder(binding.root){
    }

}
