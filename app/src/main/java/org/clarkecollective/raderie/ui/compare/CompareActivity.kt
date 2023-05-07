package org.clarkecollective.raderie.ui.compare

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.orhanobut.logger.Logger
import org.clarkecollective.raderie.R
import org.clarkecollective.raderie.databinding.ActivityCompareBinding

class CompareActivity : AppCompatActivity() {

    private val viewModel: CompareViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare)
        setBinding()
        Logger.d("Creating compare activity")
        viewModel.startComparison(intent.getStringExtra("selectedFriendID") ?: "Error", intent.getStringExtra("selectedFriendName") ?: "Error" )
    }
    private fun setBinding() {
        val binding: ActivityCompareBinding = DataBindingUtil.setContentView(this, R.layout.activity_compare)
        binding.vm = viewModel
        binding.lifecycleOwner = this
        setListeners()
    }
    private fun setListeners() {
        viewModel.selectedItemPosition.observe(this) {
            Logger.d("Selected item position changed: $it")
        }
    }
}

@BindingAdapter("setCompareAdapter")
fun bindShareRV(recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>) {
    recyclerView.setHasFixedSize(true)
    recyclerView.isNestedScrollingEnabled = false
    recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, LinearLayoutManager.VERTICAL, false)
    recyclerView.adapter = adapter
}