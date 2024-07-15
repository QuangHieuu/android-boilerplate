package boilerplate.ui.conversation

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.base.BaseFragment
import boilerplate.databinding.FragmentConversationBinding
import boilerplate.model.conversation.Conversation
import boilerplate.ui.conversation.adapter.ConversationAdapter
import boilerplate.ui.main.MainVM
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.show
import boilerplate.widget.recyclerview.EndlessListener
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class ConversationFragment : BaseFragment<FragmentConversationBinding, MainVM>() {
    companion object {
        fun newInstance() = ConversationFragment().apply {
        }
    }

    private lateinit var _adapter: ConversationAdapter
    private lateinit var _endLessListener: EndlessListener
    private lateinit var _layoutManager: LinearLayoutManager

    private var _isOnReload = false
    private var _isOnTop = true
    private var _isLoadMore = false
    private var _currentSize = 0
    private var _isDisable = false

    private val _isUnRead = false
    private val _isImportant = false
    private val _currentFilter = 0

    override val mViewModel: MainVM by activityViewModel()

    override fun initialize() {
        _adapter = ConversationAdapter(object : ConversationAdapter.SimpleEvent() {
            override fun onItemClick(con: Conversation) {
                _adapter.closeAll()
            }

            override fun onMarkAsImportant(conversation: Conversation) {

            }

            override fun onDelete(conversation: Conversation) {
            }

            override fun onNotify(item: Conversation, isNotify: Boolean) {
            }
        })

        val divider = DividerItemDecoration(
            context,
            DividerItemDecoration.VERTICAL
        )
        val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.divider_1dp)
        if (drawable != null) {
            divider.setDrawable(drawable)
        }

        _layoutManager = binding.rcvMessage.layoutManager as LinearLayoutManager
        _endLessListener =
            object : EndlessListener(_layoutManager) {
                override fun onLoadMore(page: Int, totalItemsCount: Int) {
                    if (_currentSize > 0 && !_isLoadMore && !_isOnReload) {
                        _isLoadMore = true
                        val id: String = _adapter.lastConversationId
                        run {
                            _adapter.loadMore()
                            loadMore(id)
                        }
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    recyclerView.post {
                        _isOnTop = !recyclerView.canScrollVertically(-1) ||
                                _layoutManager.findFirstCompletelyVisibleItemPosition() == 0
                    }
                }
            }

        binding.rcvMessage.apply {
            adapter = _adapter
            addItemDecoration(divider)
            setItemAnimator(null)
            setHasFixedSize(true)
            setItemViewCacheSize(0)

            addOnScrollListener(_endLessListener)
        }
    }

    override fun onSubscribeObserver() {
        with(mViewModel) {
            loadConversation.observe(this@ConversationFragment) {
                if (it.isEmpty() && lastId.isEmpty()) {
                    disableLoading()
                    binding.viewNoData.lnNoData.show()
                    binding.rcvMessage.gone()
                    return@observe
                }
                addData(it, lastId.isNotEmpty())
            }
        }
    }

    override fun registerEvent() {
        with(binding) {
            swipeLayout.setOnRefreshListener {
                _endLessListener.refreshPage()
                loadMore("")
            }
        }
    }

    override fun callApi() {
        loadMore("")
    }

    private fun loadMore(last: String) {
        mViewModel.apiGetConversation(last, 10, _isUnRead, _isImportant, null);
    }

    private fun disableLoading() {
        if (_adapter.itemCount == 0) {
            binding.viewNoData.lnNoData.show()
        }
        _isLoadMore = false
        _isOnReload = false
        binding.swipeLayout.isRefreshing = false
    }

    private fun addData(items: ArrayList<Conversation>, loadMore: Boolean) {
        _currentSize = items.size
        _isLoadMore = false
        _isOnReload = false
        if (binding.swipeLayout.isRefreshing) {
            binding.swipeLayout.isRefreshing = false
        }
        if (loadMore) {
            _adapter.cancelLoadMore()
            _adapter.addMore(items)
        } else {
            if (!binding.rcvMessage.isShown()) {
                binding.viewNoData.lnNoData.gone()
                binding.rcvMessage.show()
            }
            _adapter.insertData(items)
        }
    }
}