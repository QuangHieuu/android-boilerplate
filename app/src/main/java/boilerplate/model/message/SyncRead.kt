package boilerplate.model.message

import com.google.gson.annotations.SerializedName

class SyncRead {
	@SerializedName("hoi_thoai_id")
	var conversationId: String? = null
		get() = field ?: "".also { field = it }

	@SerializedName("lan_doc_cuoi")
	var lastRead: String? = null
		get() = field ?: "".also { field = it }

	@SerializedName("so_tin_da_doc")
	var readCount: Int = 0
		private set


	constructor()

	constructor(conversationId: String?, lastRead: String?, readCount: Int) {
		this.conversationId = conversationId
		this.lastRead = lastRead
		this.readCount = readCount
	}
}