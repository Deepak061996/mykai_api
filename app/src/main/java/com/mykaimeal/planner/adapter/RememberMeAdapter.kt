package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.databinding.SelectRememberItemBinding
import com.mykaimeal.planner.fragment.authfragment.login.model.RememberMe

class RememberMeAdapter(
    private var rememberList: List<RememberMe>,
    var requireContext: Context,
    var select: RememberSelect
) : RecyclerView.Adapter<RememberMeAdapter.Holder>() {

    private var selectedItemPos = -1
    private var lastItemSelectedPos = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: SelectRememberItemBinding =
            SelectRememberItemBinding.inflate(inflater, parent, false);
        return Holder(binding)
    }

    override fun getItemCount(): Int {
        return rememberList.size
    }

    override fun onBindViewHolder(holder: Holder, @SuppressLint("RecyclerView") position: Int) {

        holder.binding.checkEmail.isChecked = position == selectedItemPos

        holder.binding.checkEmail.setOnClickListener {
            selectedItemPos = holder.getAdapterPosition()
            if (lastItemSelectedPos == -1)
                lastItemSelectedPos = selectedItemPos
            else {
                notifyItemChanged(lastItemSelectedPos)
                lastItemSelectedPos = selectedItemPos
            }
            notifyItemChanged(selectedItemPos)
            select.selectRemember(rememberList.get(position))
        }
        holder.bind(rememberList[position])
    }

    class Holder(var binding: SelectRememberItemBinding) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(dataItem: RememberMe) {
            binding.checkEmail.text = dataItem.email
        }
    }

}

interface RememberSelect {
    fun selectRemember(remember: RememberMe)

}
