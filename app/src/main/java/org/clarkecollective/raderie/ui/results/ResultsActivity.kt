package org.clarkecollective.raderie.ui.results

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.orhanobut.logger.Logger
import org.clarkecollective.raderie.models.HumanValue
import org.clarkecollective.raderie.R
import org.clarkecollective.raderie.databinding.ActivityResultsBinding

class ResultsActivity : AppCompatActivity() {

  private val resultsActivityViewModel: ResultsActivityViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    resultsActivityViewModel.setDeck(intent.extras?.get(getString(R.string.deckExtra)) as ArrayList<HumanValue>)
    Log.d("VR", "Creating Results Activity")
    Log.d("VR", "Deck size: " + resultsActivityViewModel.deck.value?.size)
    setBinding()
  }

  private fun setBinding() {
    val binding: ActivityResultsBinding = DataBindingUtil.setContentView(this, R.layout.activity_results)
    binding.vm = resultsActivityViewModel
    binding.lifecycleOwner = this
  }
}

@BindingAdapter("setResultsAdapter")
  fun bindRecyclerViewAdapter(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
  Logger.d("Inside result binding")
  recyclerView.setHasFixedSize(true)
  recyclerView.isNestedScrollingEnabled = false
  recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, LinearLayoutManager.VERTICAL, false)
  recyclerView.adapter = adapter
  }