package com.mykaimeal.planner.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterSearchRecipeBinding

class SearchRecipeAdapter(
    private var ingredientsList: MutableList<com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.apiresponse.Ingredient>,
    private var requireActivity: FragmentActivity
) : RecyclerView.Adapter<SearchRecipeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterSearchRecipeBinding =
            AdapterSearchRecipeBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (ingredientsList[position].name!=null){
            holder.binding.tvRecipeName.text = ingredientsList[position].name
        }

        if (ingredientsList[position].image !=null){
            Glide.with(requireActivity)
                .load(ingredientsList[position].image)
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
                .into(holder.binding.imgRecipe)
        }else{
            holder.binding.layProgess.root.visibility= View.GONE
        }

    }

    override fun getItemCount(): Int {
        return ingredientsList.size
    }

    fun filterList(filteredList: MutableList<com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.apiresponse.Ingredient>) {
        this.ingredientsList=filteredList
        notifyDataSetChanged()

    }

    fun submitList(ingredient: MutableList<com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.apiresponse.Ingredient>) {
        this.ingredientsList=ingredient
        notifyDataSetChanged()
    }


    class ViewHolder(var binding: AdapterSearchRecipeBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

}