package me.paxana.valuesrank.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import me.paxana.valuesrank.R
import me.paxana.valuesrank.adapters.ResultsRecyclerAdapter
import me.paxana.valuesrank.databinding.ActivityResultsBinding
import me.paxana.valuesrank.getViewModel

class ResultsActivity : AppCompatActivity() {

  private val mainActivityViewModel by lazy {
    getViewModel { MainActivityViewModel() }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setBinding()
  }

  private fun setBinding() {
    val binding: ActivityResultsBinding = DataBindingUtil.setContentView(this, R.layout.activity_results)
    binding.vm = mainActivityViewModel
    binding.lifecycleOwner = this
  }
}

@BindingAdapter("setResultsAdapter")
  fun bindRecyclerViewAdapter(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
  recyclerView.setHasFixedSize(true)
  recyclerView.isNestedScrollingEnabled = false
  recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, LinearLayoutManager.VERTICAL, false)
  recyclerView.adapter = adapter
  }