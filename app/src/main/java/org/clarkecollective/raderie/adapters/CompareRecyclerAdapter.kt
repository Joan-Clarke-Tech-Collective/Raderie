package org.clarkecollective.raderie.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.orhanobut.logger.Logger
import org.clarkecollective.raderie.BR
import org.clarkecollective.raderie.models.HumanValue
import org.clarkecollective.raderie.ui.compare.CompareViewModel
import org.clarkecollective.raderie.ui.results.ResultsActivityViewModel

class CompareRecyclerAdapter(private val vM: CompareViewModel, @LayoutRes private val layoutRes: Int): RecyclerView.Adapter<CompareRecyclerAdapter.CompareViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompareViewHolder {
    Log.d("VR", "Creating Compare Recycler View")
    val layoutInflater = LayoutInflater.from(parent.context)
    val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, viewType, parent, false)
    return CompareViewHolder(binding)
  }

  override fun onBindViewHolder(holder: CompareViewHolder, position: Int) {
    holder.bind(getObjFromPosition(position), position)
  }

  private fun getObjFromPosition(position: Int): HumanValue? {
    return vM.friendDeckLV.value?.sortedBy { it.rating }?.asReversed()?.get(position)
  }

  override fun getItemCount(): Int {
    return vM.friendDeckLV.value?.size ?: 0
  }

  private fun getLayoutIdForPosition(): Int {
    return layoutRes
  }

  override fun getItemViewType(position: Int): Int {
    return getLayoutIdForPosition()
  }

  class CompareViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(humanValue: HumanValue?, position: Int) {
      Log.d("VR", "Binding Compare View Holder: $humanValue")
      binding.setVariable(BR.humanValue, humanValue)
      binding.setVariable(BR.position, (position + 1).toString())
    }
  }
}