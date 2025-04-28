package com.mykaimeal.planner.fragment.mainfragment.hometab

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterPrepareCookItem
import com.mykaimeal.planner.databinding.FragmentDirectionStepsRecipeDetailsBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.RecipeDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DirectionSteps1RecipeDetailsFragment : Fragment() {

    private lateinit var binding: FragmentDirectionStepsRecipeDetailsBinding
    private var totalProgressValue:Int=0
    private var adapterPrepareCookItem: AdapterPrepareCookItem? = null
    private lateinit var viewModel: RecipeDetailsViewModel
    private var mealType: String = ""
    private var uri: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding=FragmentDirectionStepsRecipeDetailsBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[RecipeDetailsViewModel::class.java]

        mealType = arguments?.getString("mealType", "")?:""
        uri = arguments?.getString("uri", "")?:""

        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.GONE
            llBottomNavigation.visibility = View.GONE
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

        binding.progressBar.max=2
        totalProgressValue=2
        updateProgress(1)

        initialize()

        setData()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun setData() {

        if (viewModel.getRecipeData()?.get(0)!!.recipe?.images?.SMALL?.url != null) {
            Glide.with(requireContext())
                .load(viewModel.getRecipeData()?.get(0)!!.recipe?.images?.SMALL?.url)
                .error(R.drawable.no_image)
                .placeholder(R.drawable.no_image)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.layProgess.root.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.layProgess.root.visibility = View.GONE
                        return false
                    }
                })
                .into(binding.imageData)
        } else {
            binding.layProgess.root.visibility = View.GONE
        }

        if (viewModel.getRecipeData()?.get(0)!!.recipe?.label != null) {
            binding.tvTitle.text = "" + viewModel.getRecipeData()?.get(0)!!.recipe?.label
        }

        if (viewModel.getRecipeData()?.get(0)!!.recipe?.source != null) {
            binding.tvBy.text = "By " + viewModel.getRecipeData()?.get(0)!!.recipe?.source
        }

        if (viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients != null && viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients!!.size > 0) {
            adapterPrepareCookItem = AdapterPrepareCookItem(viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients, requireActivity())
            binding.rcyPrepareToCook.adapter = adapterPrepareCookItem
            binding.rcyPrepareToCook.visibility = View.VISIBLE
        } else {
            binding.rcyPrepareToCook.visibility = View.GONE
        }


    }

    private fun initialize() {

        binding.imgStep1RecipeDetails.setOnClickListener{
            findNavController().navigateUp()
        }

        binding.relNextStep.setOnClickListener{
            val bundle=Bundle()
            bundle.putString("uri",uri)
            bundle.putString("mealType",mealType)
            findNavController().navigate(R.id.directionSteps2RecipeDetailsFragmentFragment,bundle)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgress(progress: Int) {
        binding.progressBar.progress = progress
        binding.tvProgressText.text = "$progress /$totalProgressValue"
    }




}