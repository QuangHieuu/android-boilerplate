package boilerplate.ui.contact.listener

import boilerplate.model.conversation.Conversation
import boilerplate.model.user.Company
import boilerplate.model.user.Department
import boilerplate.model.user.User

interface OnContactListener {

	fun onExpandCompany(company: Company)

	fun onDepartmentSelected(department: Department)

	fun onCompanySelected(company: Company)

	fun onExpandDepartment(department: Department)

	fun onOpenInform(user: User)

	fun onChatWith(item: User)

	fun onPhone(phoneNumber: String)

	fun onUserSelect(user: User)

	fun onRegularContact(conversation: Conversation)

	fun onRegularMenu(conversation: Conversation)

	fun onRegularSelected(conversation: Conversation)

	fun removeUser(user: User)

	fun removeConversation(conversation: Conversation)

	fun onGroup(conversationId: String)
}

abstract class SimpleListener : OnContactListener {
	override fun onExpandCompany(company: Company) {

	}

	override fun onDepartmentSelected(department: Department) {

	}

	override fun onCompanySelected(company: Company) {

	}

	override fun onExpandDepartment(department: Department) {

	}

	override fun onOpenInform(user: User) {

	}

	override fun onChatWith(item: User) {

	}

	override fun onPhone(phoneNumber: String) {

	}

	override fun onUserSelect(user: User) {

	}

	override fun onRegularContact(conversation: Conversation) {

	}

	override fun onRegularMenu(conversation: Conversation) {

	}

	override fun onRegularSelected(conversation: Conversation) {

	}

	override fun removeUser(user: User) {

	}

	override fun removeConversation(conversation: Conversation) {

	}

	override fun onGroup(conversationId: String) {

	}
}