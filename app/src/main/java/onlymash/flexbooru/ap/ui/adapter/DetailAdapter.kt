package onlymash.flexbooru.ap.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import onlymash.flexbooru.ap.data.model.Post
import onlymash.flexbooru.ap.ui.fragment.DetailFragment

class DetailAdapter(fm: FragmentManager,
                    lifecycle: Lifecycle) : FragmentStateAdapter(fm, lifecycle) {

    private var posts: List<Post> = mutableListOf()

    fun updateData(data: List<Post>) {
        posts = data
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): Fragment =
        DetailFragment.newInstance(posts[position].id)

    override fun getItemCount(): Int = posts.size
}