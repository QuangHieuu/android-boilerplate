package boilerplate.widget.chatBox.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import boilerplate.databinding.ItemLoadMoreBinding
import boilerplate.model.file.AttachedFile
import boilerplate.model.message.Quote
import boilerplate.utils.SystemUtil
import boilerplate.widget.holder.LoadingVH


class BoxAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val _list = arrayListOf<Any>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return LoadingVH(ItemLoadMoreBinding.inflate(layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return _list.size
    }

    fun hasFile(): Boolean {
        for (ob in _list) {
            if (ob is AttachedFile) {
                return true
            }
        }
        return false
    }

    fun hasQuote(): Boolean {
        for (ob in _list) {
            if (ob is Quote) {
                return true
            }
        }
        return false
    }

    fun getFileUpload(): ArrayList<AttachedFile.Conversation> {
        val files = arrayListOf<AttachedFile.Conversation>()
        for (ob in _list) {
            if (ob is AttachedFile.Conversation) {
                if (ob.isUpload) {
                    files.add(ob)
                }
            }
        }
        return files
    }

    fun getCurrentFile(): ArrayList<AttachedFile.Conversation> {
        val files = arrayListOf<AttachedFile.Conversation>()
        for (ob in _list) {
            if (ob is AttachedFile.Conversation) {
                if (!ob.isUpload) {
                    files.add(ob)
                }
            }
        }
        return files
    }

    fun getSurveyFile(): ArrayList<AttachedFile.SurveyFile> {
        val files = arrayListOf<AttachedFile.SurveyFile>()
        for (ob in _list) {
            if (ob is AttachedFile.SurveyFile) {
                files.add(ob)
            }
        }
        return files
    }

    fun getListLinkMessage(): ArrayList<String> {
        val list = arrayListOf<String>()
        for (ob in _list) {
            if (ob is String) {
                list.add(ob)
            }
        }
        return list
    }

    fun getListMessage(): ArrayList<Quote> {
        val list = arrayListOf<Quote>()
        for (ob in _list) {
            if (ob is Quote) {
                list.add(ob)
            }
        }
        return list
    }

    fun checkFileSizeLimit(context: Context): Boolean {
        val maximum = 25f
        var totalSize = 0f
        for (ob in _list) {
            if (ob is AttachedFile) {
                if (ob.isUpload) {
                    totalSize = totalSize.plus(SystemUtil.getFileSize(context, ob.uri))
                }
            }
        }
        return totalSize > maximum
    }

    fun clearAll() {
        val size: Int = _list.size
        _list.clear()
        notifyItemRangeRemoved(0, size)
    }
}