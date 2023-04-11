package org.clarkecollective.raderie.ui.share

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.orhanobut.logger.Logger
import org.clarkecollective.raderie.BR
import org.clarkecollective.raderie.models.Friend

  class FriendListRVAdapter(private val vM: ShareActivityViewModel, @LayoutRes private val layoutRes: Int, val clickInterface: FriendClickInterface): RecyclerView.Adapter<FriendListRVAdapter.FriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
      Log.d("VR", "Creating Friend Recycler View")
      val layoutInflater = LayoutInflater.from(parent.context)
      val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, viewType, parent, false)
      return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
      Logger.d(position)
      holder.bind(getObjFromPosition(position), clickInterface)
    }

    private fun getObjFromPosition(position: Int): Friend {
      return vM.friendListLD.value?.get(position) ?: Friend()
    }

    override fun getItemCount(): Int {
      val size = vM.friendListLD.value?.size
      Logger.d(size)
      return size ?: 0
    }

    private fun getLayoutIdForPosition(): Int {
      return layoutRes
    }

    override fun getItemViewType(position: Int): Int {
      return getLayoutIdForPosition()
    }

    class FriendViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
      fun bind(friend: Friend, clickInterface: FriendClickInterface) {
        Log.d("VR", "Binding View Holder")
        binding.setVariable(BR.friend, friend)
        binding.setVariable(BR.friendClick, clickInterface)
      }
    }
}