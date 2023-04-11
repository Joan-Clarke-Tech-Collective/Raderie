package org.clarkecollective.raderie.ui.share

import org.clarkecollective.raderie.models.Friend

interface FriendClickInterface {
  fun onFriendClicked(friend: Friend)
}