package boilerplate.ui.conversationDetail.adpater

import android.view.View
import boilerplate.model.file.AttachedFile
import boilerplate.model.message.Message
import boilerplate.model.user.User

private interface OnMessageListener {
	fun goToMessage(messageId: String, conversationId: String)
	fun longClick(message: Message, view: View, viewType: Int)
	fun openUser(person: User)
	fun openImage(file: AttachedFile)
	fun openSurvey(surveyFile: AttachedFile)
	fun openFile(file: AttachedFile)
	fun quoteMessage(message: Message)
	fun showReaction(message: Message, view: View)
	fun whoseReactions(messageId: String)
	fun mentionUser(userId: String)
	fun openPhone(number: String)
	fun playRecord(file: AttachedFile)
	fun downloadAudio(file: AttachedFile)
	fun closeMenu()
}

abstract class SimpleMessageEvent : OnMessageListener {
	override fun goToMessage(messageId: String, conversationId: String) {

	}

	override fun longClick(message: Message, view: View, viewType: Int) {

	}

	override fun openUser(person: User) {

	}

	override fun openImage(file: AttachedFile) {

	}

	override fun openSurvey(surveyFile: AttachedFile) {

	}

	override fun openFile(file: AttachedFile) {

	}

	override fun quoteMessage(message: Message) {

	}

	override fun showReaction(message: Message, view: View) {

	}

	override fun whoseReactions(messageId: String) {

	}

	override fun mentionUser(userId: String) {

	}

	override fun openPhone(number: String) {

	}

	override fun playRecord(file: AttachedFile) {

	}

	override fun downloadAudio(file: AttachedFile) {

	}

	override fun closeMenu() {

	}

}