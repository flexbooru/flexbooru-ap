package onlymash.flexbooru.ap.ui.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.image.glide.GlideImagesPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import onlymash.flexbooru.ap.common.Settings
import onlymash.flexbooru.ap.common.USER_TOKEN_KEY
import onlymash.flexbooru.ap.data.api.Api
import onlymash.flexbooru.ap.data.isLoading
import onlymash.flexbooru.ap.data.repository.comment.CommentAllRepositoryImpl
import onlymash.flexbooru.ap.databinding.FragmentListRefreshableBinding
import onlymash.flexbooru.ap.extension.getViewModel
import onlymash.flexbooru.ap.glide.GlideApp
import onlymash.flexbooru.ap.ui.adapter.CommentAllAdapter
import onlymash.flexbooru.ap.ui.base.KodeinFragment
import onlymash.flexbooru.ap.ui.viewmodel.CommentAllViewModel
import onlymash.flexbooru.ap.widget.ListListener
import org.kodein.di.erased.instance

class CommentAllFragment : KodeinFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val sp by instance<SharedPreferences>()
    private val api by instance<Api>()

    private var _binding: FragmentListRefreshableBinding? = null
    private val binding get() = _binding!!

    private val list get() = binding.layoutList.layoutRv.list
    private val refresh get() = binding.layoutList.refresh
    private val progressBar get() = binding.layoutProgress.progressBar

    private lateinit var token: String
    private lateinit var commentAllViewModel: CommentAllViewModel
    private lateinit var commentAllAdapter: CommentAllAdapter
    private val repo by lazy { CommentAllRepositoryImpl(api = api) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        token = Settings.userToken
        sp.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        commentAllViewModel = getViewModel(CommentAllViewModel(repo = repo))
        _binding = FragmentListRefreshableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val glide = GlideApp.with(this)
        val markdown = Markwon.builder(requireContext())
            .usePlugin(GlideImagesPlugin.create(glide))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(LinkifyPlugin.create(Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS))
            .build()
        commentAllAdapter = CommentAllAdapter(glide, markdown) { commentAllViewModel.retry() }
        list.apply {
            setOnApplyWindowInsetsListener(ListListener)
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
            adapter = commentAllAdapter
        }
        commentAllViewModel.comments.observe(this.viewLifecycleOwner, Observer { comments ->
            if (comments != null) {
                if (comments.size > 0) {
                    progressBar.isVisible = false
                }
                commentAllAdapter.submitList(comments)
            }
        })
        commentAllViewModel.networkState.observe(this.viewLifecycleOwner, Observer {
            progressBar.isVisible = it.isLoading() && commentAllAdapter.itemCount == 0
            commentAllAdapter.setNetworkState(it)
        })
        commentAllViewModel.refreshState.observe(this.viewLifecycleOwner, Observer {
            if (!it.isLoading()) {
                refresh.isRefreshing = false
            }
        })
        commentAllViewModel.show(token)
        refresh.setOnRefreshListener {
            commentAllViewModel.refresh()
        }
    }

    override fun onDestroyView() {
        _binding = null
        sp.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroyView()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == USER_TOKEN_KEY) {
            commentAllViewModel.show(Settings.userToken)
            commentAllViewModel.refresh()
        }
    }
}