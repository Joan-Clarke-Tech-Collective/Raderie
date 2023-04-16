package org.clarkecollective.raderie.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import org.clarkecollective.raderie.BR
import org.clarkecollective.raderie.models.HumanValue
import org.clarkecollective.raderie.ui.results.ResultsActivityViewModel

class ResultsRecyclerAdapter(private val vM: ResultsActivityViewModel, @LayoutRes private val layoutRes: Int): RecyclerView.Adapter<ResultsRecyclerAdapter.ResultsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsViewHolder {
        Log.d("VR", "Creating Results Recycler View")
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, viewType, parent, false)
        return ResultsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultsViewHolder, position: Int) {
        holder.bind(getObjFromPosition(position), position)
    }

    private fun getObjFromPosition(position: Int): HumanValue {
        return vM.meaningfulDeck.value?.sortedBy { it.rating }?.asReversed()?.get(position) ?: HumanValue()
    }

    override fun getItemCount(): Int {
        return vM.meaningfulDeck.value?.size ?: 0
    }

    private fun getLayoutIdForPosition(): Int {
        return layoutRes
    }

    override fun getItemViewType(position: Int): Int {
        return getLayoutIdForPosition()
    }

    class ResultsViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(humanValue: HumanValue, position: Int) {
            Log.d("VR", "Binding View Holder")
            binding.setVariable(BR.humanValue, humanValue)
            binding.setVariable(BR.position, (position + 1).toString())
        }
    }
}