package com.mykaimeal.planner.fragment.mainfragment.commonscreen.shoppinglistscreen

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.BasketYourRecipeAdapter
import com.mykaimeal.planner.adapter.IngredientsAdapterItem
import com.mykaimeal.planner.adapter.IngredientsShoppingAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.databinding.FragmentShoppingListBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.ingredientDislikes.model.DislikedIngredientsModel
import com.mykaimeal.planner.fragment.commonfragmentscreen.ingredientDislikes.model.DislikedIngredientsModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Ingredient
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Recipes
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.shoppinglistscreen.model.ShoppingListModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.shoppinglistscreen.model.ShoppingListModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.shoppinglistscreen.viewmodel.ShoppingListViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@AndroidEntryPoint
class ShoppingListFragment : Fragment(), OnItemClickListener, OnItemSelectListener {
    private lateinit var binding: FragmentShoppingListBinding
    private lateinit var shoppingListViewModel: ShoppingListViewModel
    private lateinit var adapterShoppingAdapter: IngredientsShoppingAdapter
    private var adapterRecipe: BasketYourRecipeAdapter? = null
    private var tvCounter: TextView? = null
    private var recipe: MutableList<Recipes> = mutableListOf()
    private var ingredientList: MutableList<Ingredient> = mutableListOf()
    private lateinit var commonWorkUtils: CommonWorkUtils
    private lateinit var textListener: TextWatcher
    private lateinit var rlWriteNameHere: RelativeLayout
    private var textChangedJob: Job? = null
    private var popupWindow: PopupWindow? = null
    private var tvLabel:EditText?=null
    private var ingredientsAdapterItem: IngredientsAdapterItem? = null
    // TextWatcher with debounce
    var searchFor = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentShoppingListBinding.inflate(layoutInflater, container, false)

        shoppingListViewModel = ViewModelProvider(requireActivity())[ShoppingListViewModel::class.java]
        commonWorkUtils = CommonWorkUtils(requireActivity())

        adapterRecipe = BasketYourRecipeAdapter(recipe, requireActivity(), this)
        binding.rcvYourRecipes.adapter = adapterRecipe

        adapterShoppingAdapter = IngredientsShoppingAdapter(ingredientList, requireActivity(), this)
        binding.rcvIngredients.adapter = adapterShoppingAdapter

        backButton()

        initialize()

        lunchApi()

        return binding.root
    }

    private fun lunchApi(){
        if (BaseApplication.isOnline(requireContext())) {
            getShoppingList()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }


    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun updateValue(value:Int) {
        tvCounter!!.text = ""+value
    }

    private fun initialize() {

        binding.textCheckoutTesco.isClickable=false

        binding.imageBackIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.rlAddMore.setOnClickListener {
            addItemDialog()
        }

        binding.textCheckoutTesco.setOnClickListener{
            if (binding.textCheckoutTesco.isClickable){
                if (BaseApplication.isOnline(requireContext())) {
                    addToCartUrlApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }

    }

    private fun addItemDialog() {
        val context = requireContext()
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.alert_dialog_add_new_item)
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        val tvDialogCancelBtn = dialog.findViewById<TextView>(R.id.tvDialogCancelBtn)
        val imageCross = dialog.findViewById<ImageView>(R.id.imageCross)
        val imageMinus = dialog.findViewById<ImageView>(R.id.imageMinus)
        val imagePlus = dialog.findViewById<ImageView>(R.id.imagePlus)
        tvCounter = dialog.findViewById(R.id.tvCounter)
        tvLabel = dialog.findViewById(R.id.tvLabel)
        val tvDialogAddBtn = dialog.findViewById<TextView>(R.id.tvDialogAddBtn)
        rlWriteNameHere = dialog.findViewById(R.id.rlWriteNameHere)
        textListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                if (!searchText.equals(searchFor,true)) {
                    searchFor = searchText
                    textChangedJob?.cancel()
                    textChangedJob = lifecycleScope.launch {
                        delay(1000)
                        if (searchText == searchFor) {
                            searchable(searchText)
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        tvLabel?.addTextChangedListener(textListener)

        dialog.setOnDismissListener {
            tvLabel?.removeTextChangedListener(textListener)
        }

        imageMinus.setOnClickListener {
            if (tvCounter!!.text.toString().toInt() > 1) {
                var data = tvCounter!!.text.toString().toInt()
                data-- // Decrement the value
                updateValue(data)
            } else {
                Toast.makeText(requireActivity(), ErrorMessage.servingError, Toast.LENGTH_LONG).show()
            }
        }

        imagePlus.setOnClickListener {
            if (tvCounter!!.text.toString().toInt() < 99) {
                var data = tvCounter!!.text.toString().toInt()
                data++ // Decrement the value
                updateValue(data)
            }
        }

        imageCross.setOnClickListener {
            dialog.dismiss()
        }

        tvDialogCancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        tvDialogAddBtn.setOnClickListener {
            val inputName = tvLabel?.text.toString().trim()
            val quantityText = tvCounter?.text.toString()
            val schId = quantityText.toIntOrNull()

            if (inputName.isEmpty()) {
                commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.enterIngName, false)
                return@setOnClickListener
            }


            val newIngredient = Ingredient(
                created_at = null,
                deleted_at = "",
                food_id = null,
                id = Random.nextInt(10000000, 99999999),
                market_id = "",
                name = inputName,
                price = "",
                pro_id = null,
                pro_img = null,
                pro_name = inputName,
                pro_price = "Not available",
                product_id = null,
                quantity = quantityText,
                sch_id = schId,
                status = null,
                newStatus=true,
                updated_at = null,
                user_id = null,
                null
            )

            ingredientList.let {
                it.add(newIngredient)
                adapterShoppingAdapter.notifyItemInserted(it.size - 1)
            }
            searchFor=""
            buttonColor()

            dialog.dismiss()

            // Optional: Add API logic here
        }

        dialog.show()


    }


    private fun buttonColor(){
        val count = ingredientList.count { it.newStatus == true }
        if (count==0){
            binding.textCheckoutTesco.isClickable=false
            binding.textCheckoutTesco.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        }else{
            binding.textCheckoutTesco.isClickable=true
            binding.textCheckoutTesco.setBackgroundResource(R.drawable.gray_btn_select_background)
        }
    }

    private fun searchable(editText: String) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            shoppingListViewModel.getDislikeSearchIngredients({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        val gson = Gson()
                        try {
                            val dietaryModel = gson.fromJson(it.data, DislikedIngredientsModel::class.java)
                            if (dietaryModel.code == 200 && dietaryModel.success) {
                                if (dietaryModel.data != null) {
                                    showDataInUi(dietaryModel.data)
                                }
                            } else {
                                popupWindow?.dismiss()
                               handleError(dietaryModel.code,dietaryModel.message)
                            }
                        } catch (e: Exception) {
                            popupWindow?.dismiss()
                            Log.d("IngredientDislike@@@@", "message:--" + e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        popupWindow?.dismiss()
                        showAlertFunction(it.message, false)
                    }
                    else -> {
                        popupWindow?.dismiss()
                        showAlertFunction(it.message, false)
                    }
                }
            }, editText, "Shopping")
        }
    }

    private fun showDataInUi(searchModelData: MutableList<DislikedIngredientsModelData>?) {
        try {
            val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val popupView: View? = inflater?.inflate(R.layout.item_select_layoutdrop, null)
            // Allows dismissing the popup when touching outside
            popupWindow?.isOutsideTouchable = true
            popupWindow = PopupWindow(popupView, rlWriteNameHere.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
            popupWindow?.showAsDropDown(rlWriteNameHere, 0, 0, Gravity.CENTER)
            val rcyData = popupView?.findViewById<RecyclerView>(R.id.rcy_data)
            searchModelData?.let {
                ingredientsAdapterItem = IngredientsAdapterItem(it, requireActivity(), this)
                rcyData!!.adapter = ingredientsAdapterItem
            }

        } catch (e: Exception) {
            popupWindow?.dismiss()
            Log.d("AddMeal", "message:--" + e.message)
        }
    }


    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }


    private fun addToCartUrlApi() {
        BaseApplication.showMe(requireContext())
         val foodIds = mutableListOf<String>()
         val schIds = mutableListOf<String>()
         val foodName = mutableListOf<String>()
         val statusType = mutableListOf<String>()

         foodIds.clear()
         schIds.clear()
         foodName.clear()
         statusType.clear()

            ingredientList.forEach {
                if (it.newStatus==true){
                    foodIds.add(it.id.toString())
                    schIds.add(it.quantity.toString())
                    foodName.add(it.name.toString())
                    statusType.add("3")
                }
            }
        lifecycleScope.launch {
            shoppingListViewModel.addShoppingCartUrlApi({
                BaseApplication.dismissMe()
                handleCartApiResponse(it)
            }, foodIds, schIds, foodName, statusType)
        }
    }

    private fun handleCartApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessCartResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessCartResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Recipe Details ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_LONG).show()
                binding.textCheckoutTesco.isClickable=false
                binding.textCheckoutTesco.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
        lunchApi()
    }

    private fun getShoppingList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            shoppingListViewModel.getShoppingListUrl {
                BaseApplication.dismissMe()
                handleApiShoppingListResponse(it)
            }
        }
    }

    private fun handleApiShoppingListResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessShoppingResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessShoppingResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, ShoppingListModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {

                apiModel.data?.let {
                    showDataShoppingUI(apiModel.data)
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }

    private fun showDataShoppingUI(data: ShoppingListModelData) {


        recipe.clear()
        ingredientList.clear()

        data.recipe?.let {
            recipe.addAll(it)
        }

        data.ingredient?.let {
            ingredientList.addAll(it)
        }

        if (recipe.size > 0) {
            binding.rlYourRecipes.visibility = View.VISIBLE
            adapterRecipe?.updateList(recipe)
        } else {
            binding.rlYourRecipes.visibility = View.GONE
        }

        if (ingredientList.size > 0) {
            adapterShoppingAdapter.updateList(ingredientList)
        }
        buttonColor()
    }

    override fun itemClick(position: Int?, status: String?, type: String?) {
        searchFor=status.toString().trim()
        textChangedJob?.cancel()
        popupWindow?.dismiss()
        // Set text
        tvLabel?.setText(status.toString().trim())
        // Move cursor to the end
        tvLabel?.text?.let { tvLabel?.setSelection(it.length) }
        // Hide the keyboard
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(tvLabel?.windowToken, 0)
    }

    override fun itemSelect(position: Int?, status: String?, type: String?) {

        if (BaseApplication.isOnline(requireActivity())) {
            if (type.equals("YourRecipe",true)) {
                if (status.equals("remove",true)){
                    val data=recipe[position!!]
                    removeRecipeBasketDialog(data.id.toString(), position)
                }else{
                    removeAddRecipeServing(position, status.toString())
                }
            } else  {
                val data=ingredientList[position!!]
                if (data.newStatus==true){
                    ingredientList.removeAt(position)
                    if (ingredientList.size>0){
                        adapterShoppingAdapter.updateList(ingredientList)
                    }
                    buttonColor()
                }else{
                    removeAddIngServing(position, status.toString())
                }
            }
        }else{
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun removeAddIngServing(position: Int?, type: String) {
        val item = position?.let { ingredientList.get(it) }
        val foodId = item?.food_id
        val qty: String
        if (type.equals("plus", true) || type.equals("minus", true)) {
            var count = item?.sch_id
            count = when (type.lowercase()) {
                "plus" -> count!! + 1
                "minus" -> count!! - 1
                else -> count // No change if `apiType` doesn't match
            }
            qty= count.toString()
        } else {
            qty="0"
        }
        increaseIngRecipe(foodId, qty, item, position)
    }

    private fun increaseIngRecipe(
        foodId: String?,
        quantity: String,
        item: Ingredient?,
        position: Int?
    ) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            shoppingListViewModel.basketIngIncDescUrl({
                BaseApplication.dismissMe()
                handleApiIngResponse(it, item, quantity, position)
            }, foodId, quantity)
        }
    }

    private fun handleApiIngResponse(
        result: NetworkResult<String>,
        item: Ingredient?,
        quantity: String,
        position: Int?
    ) {
        when (result) {
            is NetworkResult.Success -> handleSuccessIngResponse(result.data.toString(), item, quantity, position)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessIngResponse(data: String, item: Ingredient?, quantity: String, position: Int?) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (!quantity.equals("0",true)) {
                    // Toggle the is_like value
                    item?.sch_id = quantity.toInt()
                    if (item != null) {
                        ingredientList[position!!] = item
                    }
                }else{
                    ingredientList.removeAt(position!!)
                }
                if (ingredientList.size>0){
                    adapterShoppingAdapter.updateList(ingredientList)
                    binding.rcvIngredients.visibility=View.VISIBLE
                }else{
                    binding.rcvIngredients.visibility=View.GONE
                }
                (activity as MainActivity?)?.upBasket()
                Toast.makeText(requireContext(),apiModel.message,Toast.LENGTH_LONG).show()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun removeAddRecipeServing(position: Int?, type: String) {
        val item = position?.let { recipe[it] }
        if (type.equals("plus", true) || type.equals("minus", true)) {
            var count = item?.serving?.toInt()
            val uri = item?.uri
            count = when (type.lowercase()) {
                "plus" -> count!! + 1
                "minus" -> count!! - 1
                else -> count // No change if `apiType` doesn't match
            }
            increaseQuantityRecipe(uri, count.toString(), item, position)
        }
    }

    private fun increaseQuantityRecipe(
        uri: String?,
        quantity: String,
        item: Recipes?,
        position: Int?
    ) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            shoppingListViewModel.basketYourRecipeIncDescUrl({
                BaseApplication.dismissMe()
                handleApiQuantityResponse(it, item, quantity, position)
            }, uri, quantity)
        }
    }

    private fun handleApiQuantityResponse(
        result: NetworkResult<String>,
        item: Recipes?,
        quantity: String,
        position: Int?
    ) {
        when (result) {
            is NetworkResult.Success -> handleSuccessQuantityResponse(result.data.toString(), item, quantity, position)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessQuantityResponse(data: String, item: Recipes?, quantity: String, position: Int?) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                // Toggle the is_like value
                item?.serving = quantity.toInt().toString()
                recipe.set(position!!, item!!)
                // Update the adapter
                adapterRecipe?.updateList(recipe)
                (activity as MainActivity?)?.upBasket()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun removeRecipeBasketDialog(recipeId: String?, position: Int?) {
        val dialogAddItem: Dialog = context?.let { Dialog(it) }!!
        dialogAddItem.setContentView(R.layout.alert_dialog_remove_recipe_basket)
        dialogAddItem.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogAddItem.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val tvDialogCancelBtn = dialogAddItem.findViewById<TextView>(R.id.tvDialogCancelBtn)
        val tvDialogRemoveBtn = dialogAddItem.findViewById<TextView>(R.id.tvDialogRemoveBtn)
        dialogAddItem.show()
        dialogAddItem.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvDialogCancelBtn.setOnClickListener {
            dialogAddItem.dismiss()
        }

        tvDialogRemoveBtn.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                removeBasketRecipeApi(recipeId.toString(), dialogAddItem, position)
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }

    private fun removeBasketRecipeApi(recipeId: String, dialogRemoveDay: Dialog, position: Int?) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            shoppingListViewModel.removeBasketUrlApi({
                BaseApplication.dismissMe()
                handleApiRemoveBasketResponse(it, position, dialogRemoveDay)
            }, recipeId)
        }
    }

    private fun handleApiRemoveBasketResponse(
        result: NetworkResult<String>,
        position: Int?,
        dialogRemoveDay: Dialog
    ) {
        when (result) {
            is NetworkResult.Success -> handleSuccessRemoveBasketResponse(result.data.toString(), position, dialogRemoveDay)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessRemoveBasketResponse(data: String, position: Int?, dialogRemoveDay: Dialog) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dialogRemoveDay.dismiss()
                recipe.removeAt(position!!)
                // Update the adapter
                if (recipe.size > 0) {
                    binding.rlYourRecipes.visibility = View.VISIBLE
                    adapterRecipe?.updateList(recipe)
                } else {
                    binding.rlYourRecipes.visibility = View.GONE
                }
                (activity as MainActivity?)?.upBasket()
                Toast.makeText(requireContext(),apiModel.message,Toast.LENGTH_LONG).show()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

}