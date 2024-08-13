package boilerplate.ui.contactDetail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import boilerplate.R
import boilerplate.base.BaseDialogFragment
import boilerplate.databinding.FragmentContactEditBinding
import boilerplate.model.user.User
import boilerplate.utils.ImageUtil
import boilerplate.utils.extension.PERMISSION_STORAGE
import boilerplate.utils.extension.click
import boilerplate.utils.extension.findOwner
import boilerplate.utils.extension.isTablet
import boilerplate.utils.extension.loadAvatar
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.showSnackBarFail
import com.bumptech.glide.Glide
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.FileNotFoundException

class ContactEditFragment : BaseDialogFragment<FragmentContactEditBinding, ContactEditVM>() {
	companion object {
		fun newInstance(): ContactEditFragment {
			return ContactEditFragment()
		}
	}

	override val viewModel: ContactEditVM by viewModel(ownerProducer = {
		findOwner(ContactDetailFragment::class)
	})

	private lateinit var picker: ActivityResultLauncher<Intent>
	private var avatar: Uri? = null

	override fun initialize() {
		picker =
			registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
				if (result.resultCode == Activity.RESULT_OK && result.data != null) {
					handleChangeAvatar(result.data!!)
				}
			}

		with(binding) {
			toolbarEdit.apply {
				setNavigationIcon(
					if (context.isTablet()) R.drawable.ic_close
					else R.drawable.ic_arrow_previous_white
				)
				click { handleBack() }
			}

		}
	}

	override fun onSubscribeObserver() {
		with(viewModel) {
			userDetail.observe(this@ContactEditFragment) { user ->
				handleUserDetail(user)
			}
			updateSuccess.observe(this@ContactEditFragment) {
				if (it != null && it) {
					handleBack()
				}
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		viewModel.updateSuccess.postValue(null)
	}

	override fun registerEvent() {
		with(binding) {
			btnUpdate.click {
				viewModel.patchUser(
					edtNumberPhone.editableText.toString(),
					edtOtherNumberPhone.editableText.toString(),
					edtStatus.editableText.toString(),
					avatar
				)
			}
			btnCancel.click { handleBack() }
			imgPick.click {
				permission(PERMISSION_STORAGE) {
					val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
					chooseFile.setType("image/*")
					val intentOpenOthers = Intent.createChooser(chooseFile, "")
					picker.launch(intentOpenOthers)
				}
			}
		}
	}

	override fun callApi() {
	}

	private fun handleUserDetail(user: User) {
		user.notNull {
			with(binding) {
				imgAvatar.loadAvatar(user.avatar)
				tvName.text = user.name

				edtNumberPhone.setText(user.phoneNumber)
				edtOtherNumberPhone.setText(user.diffPhoneNumber)
				edtStatus.setText(user.mood)
			}
		}
	}

	private fun handleChangeAvatar(result: Intent) {
		with(binding) {
			avatar = result.data
			if (avatar != null) {
				try {
					Glide
						.with(requireContext())
						.asBitmap()
						.load(ImageUtil.decodeBitmap(requireContext(), avatar!!))
						.dontAnimate()
						.error(R.drawable.ic_avatar)
						.into(imgAvatar)
				} catch (e: FileNotFoundException) {
					binding.root.showSnackBarFail(R.string.error_unacceptable_file)
				}
			} else {
				binding.root.showSnackBarFail(R.string.error_unacceptable_file)
			}
		}
	}
}