package com.mykaimeal.planner.fragment.mainfragment.commonscreen

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.adapter.AddMoreItemsAdapter
import com.mykaimeal.planner.databinding.FragmentAddMoreItemsBinding
import com.mykaimeal.planner.model.DataModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddMoreItemsFragment : Fragment(), OnItemClickListener {

    private lateinit var binding: FragmentAddMoreItemsBinding
    private lateinit var addMoreItemsAdapter: AddMoreItemsAdapter
    private var dataList1: MutableList<DataModel> = mutableListOf()
    private var tvCounter:TextView?=null
    private var quantity:Int=1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAddMoreItemsBinding.inflate(layoutInflater, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })

        planBreakFastModel()

        initialize()

        return binding.root
    }

    private fun initialize() {

        binding.rlAddPlanButton.setOnClickListener {
            addItemDialog()
        }

        binding.imageBackIcon.setOnClickListener {
            findNavController().navigateUp()
        }

    }

    private fun addItemDialog() {
        val dialogRemoveDay: Dialog = context?.let { Dialog(it) }!!
        dialogRemoveDay.setContentView(R.layout.alert_dialog_add_new_item)
        dialogRemoveDay.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogRemoveDay.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val tvDialogCancelBtn = dialogRemoveDay.findViewById<TextView>(R.id.tvDialogCancelBtn)
        val imageCross = dialogRemoveDay.findViewById<ImageView>(R.id.imageCross)
        val imageMinus = dialogRemoveDay.findViewById<ImageView>(R.id.imageMinus)
        val imagePlus = dialogRemoveDay.findViewById<ImageView>(R.id.imagePlus)
        tvCounter = dialogRemoveDay.findViewById(R.id.tvCounter)
        val tvDialogAddBtn = dialogRemoveDay.findViewById<TextView>(R.id.tvDialogAddBtn)
        dialogRemoveDay.show()
        dialogRemoveDay.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvDialogCancelBtn.setOnClickListener {
            dialogRemoveDay.dismiss()
        }

        imageMinus.setOnClickListener{
            if (quantity > 1) {
                quantity--
                updateValue()
            }else{
                Toast.makeText(requireActivity(),"Minimum serving atleast value is one", Toast.LENGTH_LONG).show()
            }
        }

        imagePlus.setOnClickListener{
            if (quantity < 99) {
                quantity++
                updateValue()
            }
        }

        imageCross.setOnClickListener{
            dialogRemoveDay.dismiss()
        }

        tvDialogAddBtn.setOnClickListener {
            dialogRemoveDay.dismiss()
        }
    }

    private fun updateValue() {
        tvCounter!!.text = String.format("%02d", quantity)

    }

    private fun planBreakFastModel() {
        val data1 = DataModel()
        val data2 = DataModel()

        data1.title = "Potato"
        data1.description = "2"
        data1.value=1

        data2.title = "Potato"
        data2.description = "2"
        data1.value=1

        dataList1.add(data1)
        dataList1.add(data2)

        addMoreItemsAdapter = AddMoreItemsAdapter(requireActivity(), dataList1, this)
        binding.rcvAddItems.adapter = addMoreItemsAdapter
    }

    private fun addSuperMarketDialog() {
        val dialogAddItem: Dialog = context?.let { Dialog(it) }!!
        dialogAddItem.setContentView(R.layout.alert_dialog_remove_item_list)
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
            dialogAddItem.dismiss()
        }
    }

    override fun itemClick(position: Int?, status: String?, type: String?) {

        if (status == "2") {
            addSuperMarketDialog()
        }
    }
}