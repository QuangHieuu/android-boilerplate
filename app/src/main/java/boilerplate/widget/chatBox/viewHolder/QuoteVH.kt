package boilerplate.widget.chatBox.viewHolder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import boilerplate.R
import boilerplate.databinding.ItemMessageQuoteBinding
import boilerplate.model.message.Quote
import boilerplate.utils.DateTimeUtil
import boilerplate.utils.StringUtil
import boilerplate.utils.extension.addFile
import boilerplate.utils.extension.addSurvey
import boilerplate.utils.extension.click
import boilerplate.utils.extension.show

class QuoteVH(
	val binding: ItemMessageQuoteBinding,
	val block: (quote: Quote) -> Unit,
	context: Context = binding.root.context
) : RecyclerView.ViewHolder(binding.root) {
	private val padding = context.resources.getDimension(R.dimen.dp_10).toInt()

	fun setData(quote: Quote) {
		with(binding) {

			root.apply {
				setBackgroundResource(R.drawable.bg_message_quote_in_list)
				setPadding(padding, padding, padding, padding)
			}

			val text: String = StringUtil.getHtml(quote.getContent()).toString()
			tvMessageQuote.apply {
				show(text.isNotEmpty())
				setText(text)
			}

			tvPersonSend.text = quote.personSend.name

			val last: String = DateTimeUtil.convertWithSuitableFormat(
				quote.dateCreate,
				DateTimeUtil.FORMAT_NORMAL_WITH_TIME_REVERT
			)
			tvLastActive.text = last

			val department = String.format(
				"%s - %s",
				quote.personSend.mainDepartment.shortName,
				quote.personSend.mainCompany.shortName
			)
			tvDepartment.text = department

			lnFile.apply {
				removeAllViews()
				for (survey in quote.surveyFiles) {
					addSurvey(survey) {}
				}
				for (file in quote.attachedFiles) {
					addFile(file) {}
				}
				show(quote.surveyFiles.isNotEmpty() || quote.attachedFiles.isNotEmpty())
			}

			imgClose.apply {
				show()
				click { block(quote) }
			}
		}
	}
}