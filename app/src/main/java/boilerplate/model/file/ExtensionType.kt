package boilerplate.model.file

import boilerplate.R
import java.util.Locale
import java.util.regex.Pattern

enum class ExtensionType(
	private val index: Int,
	private val icon: Int,
	private val extension: ArrayList<String>
) {
	OTHER(0, R.drawable.ic_file_other, ArrayList()),
	WORD(
		1,
		R.drawable.ic_file_doc,
		ArrayList(mutableListOf("doc", "docx", "msword", "wordprocessingml.document"))
	),
	EXCEL(
		2,
		R.drawable.ic_file_xls,
		ArrayList(mutableListOf("xlsx", "xls", "excel", "spreadsheetml.sheet", "vnd.ms-excel"))
	),
	PPT(
		3,
		R.drawable.ic_file_ppt,
		ArrayList(mutableListOf("ppt", "pptx", "ms-powerpoint", "presentationml.presentation"))
	),
	PDF(4, R.drawable.ic_file_pdf, ArrayList(mutableListOf("pdf"))),
	MP3(5, R.drawable.ic_file_audio, ArrayList(mutableListOf("mp3", "m4a"))),
	VIDEO(
		6,
		R.drawable.ic_file_video,
		ArrayList(mutableListOf("avi", "mov", "m4v", "mp4"))
	),
	ZIP(7, R.drawable.ic_file_zip, ArrayList(mutableListOf("zip", "rar"))),
	IMAGE(
		8,
		R.drawable.ic_file_image,
		ArrayList(mutableListOf("jpg", "png", "jpeg"))
	),
	GIF(9, R.drawable.ic_file_image, ArrayList(mutableListOf("gif"))),
	TEXT(10, R.drawable.ic_file_other, ArrayList(mutableListOf("txt", "plain")));

	companion object {
		private fun getExtension(nameOrType: String?): String {
			if (nameOrType == null) {
				return ""
			}
			var extension = ""
			if (nameOrType.lastIndexOf(".") != 0) {
				extension = nameOrType.substring(nameOrType.lastIndexOf(".") + 1)
			}
			if (nameOrType.lastIndexOf("/") != 0) {
				extension = nameOrType.substring(nameOrType.lastIndexOf("/") + 1)
			}
			return extension.lowercase(Locale.getDefault())
		}

		private fun checkListExtension(type: ExtensionType, extension: String): Boolean {
			for (s in type.extension) {
				if (Pattern.compile("\\b$s\\b").matcher(extension).find()) {
					return true
				}
			}
			return false
		}

		fun fromNameOrType(nameOrType: String?): ExtensionType {
			val extension = getExtension(nameOrType)
			if (checkListExtension(IMAGE, extension)) {
				return IMAGE
			}
			if (checkListExtension(WORD, extension)) {
				return WORD
			}
			if (checkListExtension(EXCEL, extension)) {
				return EXCEL
			}
			if (checkListExtension(PPT, extension)) {
				return PPT
			}
			if (checkListExtension(PDF, extension)) {
				return PDF
			}
			if (checkListExtension(GIF, extension)) {
				return GIF
			}
			if (checkListExtension(VIDEO, extension)) {
				return VIDEO
			}
			if (checkListExtension(MP3, extension)) {
				return MP3
			}
			if (checkListExtension(ZIP, extension)) {
				return ZIP
			}
			if (checkListExtension(TEXT, extension)) {
				return TEXT
			}
			return OTHER
		}

		/**
		 * @param nameOrType input name or type
		 * @return icon drawable source
		 */
		fun getFileIcon(nameOrType: String?): Int {
			return fromNameOrType(nameOrType).icon
		}

		fun isFilePDF(nameOrType: String): Boolean {
			val extension =
				nameOrType.substring(nameOrType.lastIndexOf(".") + 1, nameOrType.length - 1)
			return nameOrType.contains(MimeType.APPLICATION.type) && checkListExtension(
				PDF,
				extension
			)
		}

		fun isFileGIF(nameOrType: String?): Boolean {
			return checkListExtension(GIF, getExtension(nameOrType))
		}

		fun isFileImage(nameOrType: String?): Boolean {
			val extension = getExtension(nameOrType)
			return checkListExtension(IMAGE, extension) || checkListExtension(GIF, extension)
		}

		fun isFileWord(nameOrType: String?): Boolean {
			return checkListExtension(WORD, getExtension(nameOrType))
		}

		fun isFileAudio(nameOrType: String?): Boolean {
			return checkListExtension(MP3, getExtension(nameOrType))
		}

		fun removeFileType(input: String): String {
			val name = input.lowercase(Locale.getDefault())
			if (name.contains("png") ||
				name.contains("jpg") ||
				name.contains("jpeg") ||
				name.contains("gif")
			) {
				return name.replace(".png", "").replace(".jpg", "").replace(".gif", "")
			}
			if (name.contains("wordprocessingml.document") ||
				name.contains("msword") ||
				name.contains(".docx") ||
				name.contains(".doc")
			) {
				return name.replace(".doc", "").replace(".docx", "")
			}
			if (name.contains("spreadsheetml.sheet") ||
				name.contains("excel") ||
				name.contains(".xlsx") ||
				name.contains(".xls")
			) {
				return name.replace(".xlsx", "").replace(".xls", "")
			}
			if (name.contains("presentationml.presentation") ||
				name.contains("ms-powerpoint") ||
				name.contains(".ppt")
			) {
				return name.replace(".ppt", "")
			}
			if (name.contains("application/pdf") ||
				name.contains("pdf") ||
				name.contains(".pdf")
			) {
				return name.replace(".pdf", "")
			}
			return name
		}

		fun checkFileAccept(fileType: String?): Boolean {
			return when (fromNameOrType(fileType)) {
				WORD, EXCEL, PPT, PDF, MP3, VIDEO, ZIP, IMAGE, GIF -> true
				OTHER -> false
				else -> false
			}
		}

		fun checkFileForLetterAccept(fileType: String?): Boolean {
			return when (fromNameOrType(fileType)) {
				WORD, PDF -> true
				else -> false
			}
		}

		fun checkIfFileTxt(fileType: String?): Boolean {
			return fromNameOrType(fileType) == TEXT
		}
	}
}
