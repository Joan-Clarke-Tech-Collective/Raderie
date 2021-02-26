package me.paxana.valuesrank.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import me.paxana.valuesrank.BR
import me.paxana.valuesrank.HumanValue
import me.paxana.valuesrank.ui.MainActivityViewModel

class ResultsRecyclerAdapter(private val vM: MainActivityViewModel, @LayoutRes private val layoutRes: Int): RecyclerView.Adapter<ResultsRecyclerAdapter.ResultsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, viewType, parent, false)

        return ResultsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultsViewHolder, position: Int) {
        holder.bind(getObjFromPosition(position), position)
    }
    private fun getObjFromPosition(position: Int): HumanValue{
        return vM.deck.value!!.sortedBy { it.rating }.asReversed()[position]
    }

    override fun getItemCount(): Int {
        return if (vM.deck.value == null) {
            0
        }
        else {
            vM.deck.value!!.size
        }
    }
    private fun getLayoutIdForPosition(): Int {
        return layoutRes
    }

    override fun getItemViewType(position: Int): Int {
        return getLayoutIdForPosition()
    }

    class ResultsViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(humanValue: HumanValue, position: Int) {
            binding.setVariable(BR.humanValue, humanValue)
            binding.setVariable(BR.position, (position + 1).toString())
        }
    }

}