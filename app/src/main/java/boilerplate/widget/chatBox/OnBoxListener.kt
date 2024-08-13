package boilerplate.widget.chatBox

import boilerplate.model.file.AttachedFile
import boilerplate.model.message.Message

interface OnBoxListener {
	fun onSendMessage(
		content: String,
		uploadFile: ArrayList<AttachedFile>,
		currentFile: ArrayList<AttachedFile>,
		surveyFile: ArrayList<AttachedFile>,
		isSms: Boolean,
		isEmail: Boolean
	)

	fun onEditMessage(
		lastMessage: Message,
		content: String,
		uploadFile: ArrayList<AttachedFile>,
		currentFile: ArrayList<AttachedFile>,
		surveyFile: ArrayList<AttachedFile>,
		isSms: Boolean,
		isEmail: Boolean
	)

	fun onAttachedClick()

	fun onCameraClick()

	fun onPickImageClick()

	fun onEditFocus(focus: Boolean)

	fun onOpenRecord()

	fun onStartRecord(modeSpeech: Boolean)

	fun onRemoveFileOffline(file: String)
}

abstract class SimpleBoxListener : OnBoxListener {
	override fun onSendMessage(
		content: String,
		uploadFile: ArrayList<AttachedFile>,
		currentFile: ArrayList<AttachedFile>,
		surveyFile: ArrayList<AttachedFile>,
		isSms: Boolean,
		isEmail: Boolean
	) {
	}

	override fun onEditMessage(
		lastMessage: Message, content: String,
		uploadFile: ArrayList<AttachedFile>,
		currentFile: ArrayList<AttachedFile>,
		surveyFile: ArrayList<AttachedFile>,
		isSms: Boolean, isEmail: Boolean
	) {
	}

	override fun onAttachedClick() {
	}

	override fun onCameraClick() {
	}

	override fun onPickImageClick() {
	}

	override fun onEditFocus(focus: Boolean) {
	}

	override fun onOpenRecord() {
	}

	override fun onStartRecord(modeSpeech: Boolean) {
	}

	override fun onRemoveFileOffline(file: String) {
	}
}