package org.clarkecollective.raderie.ui.results

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.orhanobut.logger.Logger
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import org.clarkecollective.raderie.models.HumanValue
import org.clarkecollective.raderie.R
import org.clarkecollective.raderie.daos.ValueDao
import org.clarkecollective.raderie.databases.MyValuesDatabase
import org.clarkecollective.raderie.databinding.ActivityResultsBinding
import org.clarkecollective.raderie.log

class ResultsActivity : AppCompatActivity() {

  companion object {
    fun newIntent(context: Context, deck: ArrayList<HumanValue>): Intent {
      val intent = Intent(context, ResultsActivity::class.java)
      intent.putExtra(context.getString(R.string.deckExtra), deck)
      return intent
     }
  }

  private lateinit var roomDb : MyValuesDatabase
  private lateinit var valueDao: ValueDao

  private val resultsActivityViewModel: ResultsActivityViewModel by viewModels()

  private val compositeDisposable = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    roomDb = Room.databaseBuilder(baseContext, MyValuesDatabase::class.java, getString(R.string.databaseInUse)).fallbackToDestructiveMigration().build()
    valueDao = roomDb.valueDao()

    valueDao.getAllValues().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : DisposableSingleObserver<List<HumanValue>>() {
      override fun onSuccess(t: List<HumanValue>) {
        resultsActivityViewModel.setDeck(t as ArrayList<HumanValue>)
      }

      override fun onError(e: Throwable) {
        e.log()
      }

    }).addTo(compositeDisposable)

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