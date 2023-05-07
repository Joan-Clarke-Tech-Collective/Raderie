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
import org.clarkecollective.raderie.ui.compare.CompareViewModel
import org.clarkecollective.raderie.ui.compare.Comparison

class CompareRecyclerAdapter(private val vM: CompareViewModel, @LayoutRes private val layoutRes: Int): RecyclerView.Adapter<CompareRecyclerAdapter.CompareViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompareViewHolder {
    Log.d("VR", "Creating Compare Recycler View")
    val layoutInflater = LayoutInflater.from(parent.context)
    val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, viewType, parent, false)
    return CompareViewHolder(binding)
  }

  override fun onBindViewHolder(holder: CompareViewHolder, position: Int) {
    holder.bind(getObjFromPosition(position))
  }

  private fun getObjFromPosition(position: Int): Comparison {
    return vM.compareLV.value?.get(position) ?: Comparison(-1, HumanValue(), HumanValue())
  }

  override fun getItemCount(): Int {
    return vM.compareLV.value?.size ?: 0
  }

  private fun getLayoutIdForPosition(): Int {
    return layoutRes
  }

  override fun getItemViewType(position: Int): Int {
    return getLayoutIdForPosition()
  }

  class CompareViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(comparison: Comparison) {
      Log.d("VR", "Binding Comparison: $comparison")
      binding.setVariable(BR.comparison, comparison)
    }
  }
}