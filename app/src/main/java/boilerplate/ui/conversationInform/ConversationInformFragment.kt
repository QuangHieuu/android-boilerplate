package boilerplate.ui.conversationInform

import android.app.Activity
import android.content.Intent
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import boilerplate.R
import boilerplate.base.BaseFragment
import boilerplate.databinding.DialogBaseBinding
import boilerplate.databinding.DialogConversationInformBinding
import boilerplate.databinding.DialogLeaveGroupBinding
import boilerplate.databinding.FragmentConversationInformBinding
import boilerplate.databinding.ItemConversationInformBinding
import boilerplate.model.conversation.Conversation
import boilerplate.model.conversation.ConversationRole
import boilerplate.model.conversation.ConversationUser
import boilerplate.service.signalr.SignalRImpl
import boilerplate.service.signalr.SignalRManager
import boilerplate.ui.conversationDetail.ConversationDetailFragment
import boilerplate.ui.conversationDetail.ConversationVM
import boilerplate.ui.conversationMessage.ConversationMessageFragment
import boilerplate.ui.conversationSetting.ConversationSettingFragment
import boilerplate.utils.ClickUtil
import boilerplate.utils.ImageUtil
import boilerplate.utils.extension.PERMISSION_STORAGE
import boilerplate.utils.extension.addFile
import boilerplate.utils.extension.addLink
import boilerplate.utils.extension.addListener
import boilerplate.utils.extension.click
import boilerplate.utils.extension.findOwner
import boilerplate.utils.extension.gone
import boilerplate.utils.extension.loadAvatar
import boilerplate.utils.extension.notNull
import boilerplate.utils.extension.open
import boilerplate.utils.extension.show
import boilerplate.utils.extension.showDialog
import boilerplate.utils.extension.showSnackBarFail
import boilerplate.utils.extension.showSnackBarSuccess
import boilerplate.widget.customText.TextViewFont
import boilerplate.widget.gridImage.GridImageAdapter
import boilerplate.widget.image.RoundedImageView
import boilerplate.widget.recyclerview.PhotoSpaceDecoration
import com.bumptech.glide.Glide
import okio.FileNotFoundException
import org.koin.androidx.viewmodel.ext.android.viewModel

class ConversationInformFragment :
    BaseFragment<FragmentConversationInformBinding, ConversationVM>() {
    companion object {
        const val MAX_AVATAR: Int = 3

        fun newInstance(): ConversationInformFragment {
            return ConversationInformFragment()
        }
    }

    override val viewModel: ConversationVM by viewModel(ownerProducer = {
        findOwner(ConversationDetailFragment::class)
    })

    private lateinit var _imageAdapter: GridImageAdapter

    private lateinit var picker: ActivityResultLauncher<Intent>
    private lateinit var dialogInform: DialogConversationInformBinding

    private var _leaveInSilent: Boolean = false

    override fun initialize() {
        picker =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    handleChangeAvatar(result.data!!)
                }
            }

        _imageAdapter = GridImageAdapter()

        binding.rvFileImage.apply {
            adapter = _imageAdapter

            addItemDecoration(PhotoSpaceDecoration(context, R.dimen.dp_6))
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        SignalRManager.removeController(javaClass.simpleName)
    }

    override fun onSubscribeObserver() {
        listenerToSignalR()
        with(viewModel) {
            conversation.observe(this@ConversationInformFragment) {
                if (it.isGroup()) {
                    validate(it)
                } else {
                    binding.imgEditName.gone()
                }

                handleAvatar(it)
                handleMenu(it)
            }
            fileImage.observe(this@ConversationInformFragment) {
                binding.viewLoadingImage.gone()
                with(binding) {
                    if (it.isEmpty()) {
                        tvNoImage.show()
                        rvFileImage.gone()
                        tvMoreImage.gone()
                    } else {
                        tvNoImage.gone()
                        rvFileImage.show()
                        tvMoreImage.show((it.size > 1))
                        _imageAdapter.addImage(it)
                    }
                }

            }
            fileAttach.observe(this@ConversationInformFragment) {
                binding.viewLoadingFile.gone()
                with(binding) {
                    lnFile.removeAllViews()
                    if (it.isEmpty()) {
                        tvNoFile.show()
                        lnFile.gone()
                        tvMoreFile.gone()
                    } else {
                        tvNoFile.gone()
                        lnFile.show()
                        tvMoreFile.show(it.size > 1)
                        for (file in it) {
                            lnFile.addFile(file) {

                            }
                        }
                    }
                }
            }
            linkAttach.observe(this@ConversationInformFragment) {
                binding.viewLoadingLink.gone()
                with(binding) {
                    if (it.isEmpty()) {
                        tvNoLink.show()
                        lnLink.gone()
                        tvMoreLink.gone()
                    } else {
                        tvNoLink.gone()
                        lnLink.show()
                        tvMoreLink.show(it.size > 1)
                        for (message in it) {
                            lnLink.addLink(message) {

                            }
                        }
                    }
                }
            }
            changeConfig.observe(this@ConversationInformFragment) {
                if (it != null) {
                    viewModel.conversation.postValue(it)
                    binding.root.showSnackBarSuccess(R.string.success_update)
                }
            }
        }
    }

    override fun registerEvent() {
        with(binding) {
            toolbar.setNavigationOnClickListener { popFragment() }

            imgDropDown.setOnClickListener { tvInform.performClick() }
            tvInform.click { v ->
                imgDropDown.rotation = (if (lnInform.isShown()) 270 else 0).toFloat()
                lnInform.show(!lnInform.isShown)
            }

            imgSettingDropDown.setOnClickListener { tvSetting.performClick() }
            tvSetting.click { v ->
                imgSettingDropDown.rotation = (if (lnSetting.isShown()) 270 else 0).toFloat()
                lnSetting.show(!lnSetting.isShown)
            }

            imgFileImageDrop.setOnClickListener { tvImg.performClick() }
            tvImg.click {
                imgFileImageDrop.rotation = (if (frameContentImage.isShown()) 270 else 0).toFloat()
                frameContentImage.show(!frameContentImage.isShown())
                tvMoreImage.show(
                    (viewModel.fileImage.value?.size ?: 0) > 1 && frameContentImage.isShown()
                )
            }
            imgFileDrop.setOnClickListener { tvFile.performClick() }
            tvFile.click {
                imgFileImageDrop.rotation = (if (frameContentFile.isShown()) 270 else 0).toFloat()
                frameContentFile.show(!frameContentFile.isShown())
                tvMoreFile.show(
                    (viewModel.fileAttach.value?.size ?: 0) > 1 && frameContentFile.isShown()
                )
            }
            imgLinkDrop.setOnClickListener { tvLink.performClick() }
            tvLink.click {
                imgLinkDrop.rotation = (if (frameContentLink.isShown()) 270 else 0).toFloat()
                frameContentLink.show(!frameContentLink.isShown())
                tvMoreLink.show(
                    (viewModel.linkAttach.value?.size ?: 0) > 1 && frameContentLink.isShown()
                )
            }

            imgEditName.click { changeInform() }
        }
    }

    override fun callApi() {
        viewModel.getConversationFile()
    }

    private fun validate(conversation: Conversation) {
        if (conversation.isChangeInform) {
            binding.imgEditName.show()
        } else {
            var isAllowChangeInform = true
            for (item in conversation.getConversationUsers()) {
                if (item.user.id == viewModel.user.id) {
                    val role: Int = item.getVaiTro()
                    if (ConversationRole.fromType(role) === ConversationRole.MEMBER) {
                        isAllowChangeInform = item.isAllowSendMessage
                    }
                    break
                }
            }
            binding.imgEditName.show(isAllowChangeInform)
        }
    }

    private fun handleAvatar(con: Conversation) {
        with(binding) {
            if (con.isMyCloud) {
                addSingleAvatar().apply { setImageResource(R.drawable.ic_my_cloud) }
                tvConversationName.setText(R.string.my_cloud)
            } else {
                val isGroup = con.isGroup()
                val thumb = con.thumb
                val size = con.tongSoNhanVien
                val userSize: Int = con.conversationUsers.size
                val isOneUser = size == 1

                frameUserOnline.show(isGroup)

                rlAvatarGroup.removeAllViews()
                val builder = StringBuilder(if (isGroup) con.conversationName else "")

                var needCreateName = builder.toString().isEmpty()
                if (!needCreateName) {
                    tvConversationName.text = builder
                }
                if (isGroup && builder.toString().isEmpty() && isOneUser) {
                    builder.append(getString(R.string.conversation_name))
                }
                if (isGroup) {
                    if (thumb != null) {
                        addSingleAvatar().loadAvatar(con.thumb)
                    } else {
                        if (size == 1) {
                            addAvatar(1, size, null)
                        }
                        if (size > 4) {
                            addAvatar(3, size, null)
                        }
                    }
                }
                var onlyContainMe = !isGroup
                var countName = 0
                for (user in con.conversationUsers) {
                    val index = con.conversationUsers.indexOf(user)
                    if (isGroup) {
                        if (!(user.user.id.equals(con.creatorId) && size > 3)) {
                            if (builder.toString().isNotEmpty() && needCreateName) {
                                builder.append(", ")
                            }
                            if (countName > 2 && needCreateName) {
                                needCreateName = false
                                builder.append("…")
                            }
                            if (needCreateName) {
                                countName += 1
                                builder.append(user.user.name)
                            }
                        }
                        if (thumb == null) {
                            if (size == 1) {
                                addAvatar(index, size, user)
                                break
                            }
                            if ((index < MAX_AVATAR) || size <= 4) {
                                addAvatar(index, size, user)
                            }
                        }
                    } else {
                        if (userSize > 0 && !user.user.id.equals(viewModel.user.id)
                        ) {
                            onlyContainMe = false
                            builder.append(user.user.name)
                            addSingleAvatar().loadAvatar(user.user.avatar)
                            frameUserOnline.setEnabled(user.user.isOnline())
                        }
                    }
                }
                if (onlyContainMe) {
                    addSingleAvatar().loadAvatar(viewModel.user.avatar)
                    builder.append(viewModel.user.name)
                    frameUserOnline.gone()
                }
                tvConversationName.text = builder
            }
        }
    }


    private fun addSingleAvatar(): AppCompatImageView {
        val size = resources.getDimension(R.dimen.dp_68).toInt()
        val params = RelativeLayout.LayoutParams(size, size)
            .apply { addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE) }
        return RoundedImageView(requireContext()).apply {
            setId(View.generateViewId())
            setRadius(size / 2)
            adjustViewBounds = false
            scaleType = ImageView.ScaleType.CENTER_CROP
        }.let {
            binding.rlAvatarGroup.addView(it, params)
            it
        }
    }

    private fun addAvatar(index: Int, size: Int, user: ConversationUser?) {
        val imgSize = resources.getDimension(R.dimen.dp_36).toInt()
        val params = RelativeLayout.LayoutParams(imgSize, imgSize)
        RoundedImageView(requireContext()).apply {
            id = View.generateViewId()
            translationZ = index.toFloat()
            setRadius(imgSize / 2)
            adjustViewBounds = false
            scaleType = ImageView.ScaleType.CENTER_CROP
            when (index) {
                0 -> when (size) {
                    1, 2 -> {
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                        params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
                    }

                    3 -> {
                        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE)
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
                    }

                    else -> {
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
                        params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
                    }
                }

                1 -> if (size == 3) {
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                    params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
                } else {
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE)
                    params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
                }

                2 -> if (size == 3) {
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                    params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
                } else {
                    params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                }

                3 -> {
                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                    params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE)
                }
            }
        }.let {
            binding.rlAvatarGroup.addView(it, params)

            if (user != null) {
                it.loadAvatar(user.user.avatar)
            } else {
                if ((size == 1 && index == 1)) {
                    it.setTextBackground("")
                } else {
                    it.setTextBackground(size.toString())
                }
            }
        }
    }

    private fun handleMenu(conversation: Conversation) {
        val size: Int = conversation.tongSoNhanVien

        with(binding) {
            lnInform.removeAllViews()
            if (conversation.isGroup()) {
                lnInform.addView(initRowInform(
                    R.drawable.ic_inform_member,
                    getString(R.string.member_count, size),
                    R.color.colorBlack,
                    R.drawable.bg_circle,
                    ClickUtil.onClick { }
                ))
                lnInform.addView(initRowInform(
                    R.drawable.ic_inform_setting,
                    getString(R.string.conversation_setting),
                    R.color.colorBlack,
                    R.drawable.bg_circle,
                    ClickUtil.onClick {
                        open(split = true, fragment = ConversationSettingFragment.newInstance())
                    }
                ))
            }
            lnInform.addView(initRowInform(
                R.drawable.ic_pin,
                getString(R.string.pin_message),
                R.color.colorBlack,
                R.drawable.bg_circle,
                ClickUtil.onClick {
                    open(split = true, fragment = ConversationMessageFragment.messagePin())
                }
            ))
            lnInform.addView(initRowInform(
                R.drawable.ic_inform_favorite,
                getString(R.string.important_message),
                R.color.colorBlack,
                R.drawable.bg_circle,
                ClickUtil.onClick {
                    open(
                        split = true,
                        fragment = ConversationMessageFragment.messageImportant()
                    )
                }
            ))
            if (conversation.isMyCloud) {
                lnSetting.gone()
            } else {
                lnSetting.show()
                lnSetting.removeAllViews()

                for (item in conversation.conversationUsers) {
                    if (item.user.id.equals(viewModel.user.id)) {
                        val notifyString = getString(
                            if (item.isOffNotify) R.string.turn_on_notify
                            else R.string.turn_off_notify
                        )

                        val icon = if (!item.isOffNotify) R.drawable.ic_notification_on_black
                        else R.drawable.ic_notification_off_black

                        lnSetting.addView(initRowInform(
                            icon,
                            notifyString,
                            R.color.colorBlack,
                            R.drawable.bg_circle,
                            ClickUtil.onClick { changeNotify(item.isOffNotify) }
                        ))
                        break
                    }
                }
                //TODO: tạm thời tắt
//        mLnSetting.addView(initRowInform(
//            R.drawable.ic_delete,
//            getResources().getString(R.string.text_delete_all_message_conversation),
//            R.color.color_E80808,
//            R.drawable.bg_circle_red_opacity,
//            v -> mListener.onDeleteAllMessage()
//        ));
                if (conversation.isGroup() && viewModel.config.isAllowLeftGroup) {
                    lnSetting.addView(initRowInform(
                        R.drawable.ic_leave,
                        getString(R.string.leave_group),
                        R.color.color_E80808,
                        R.drawable.bg_circle_red_opacity,
                        ClickUtil.onClick { leaveGroup(conversation) }
                    ))
                }
            }
        }

    }

    private fun initRowInform(
        icon: Int,
        name: String,
        textColor: Int,
        background: Int,
        listener: OnClickListener
    ): View {
        val binding =
            ItemConversationInformBinding.inflate(layoutInflater, view as ViewGroup, false)
        with(binding) {
            imgIcon.apply {
                setImageResource(icon)
                setBackgroundResource(background)
            }
            tvName.apply {
                text = name
                setTextColor(ContextCompat.getColor(context, textColor))
            }

        }

        return binding.root.apply { setOnClickListener(listener) }
    }

    private fun listenerToSignalR() {
        SignalRManager.addController(javaClass.simpleName)
            .setListener(object : SignalRImpl() {
                override fun onOffNotify(conversationId: String) {
                    handleNotifyOnOff(conversationId)
                }
            })
    }

    private fun handleNotifyOnOff(conversationId: String) {
        checkCurrentConversation(conversationId) {
            viewModel.conversation.value?.apply {
                val listIterator = conversationUsers.listIterator()
                while (listIterator.hasNext()) {
                    val user = listIterator.next()
                    if (user.user.id.equals(viewModel.user.id)) {
                        user.isOffNotify = !user.isOffNotify

                        val notifyString = getString(
                            if (user.isOffNotify) R.string.turn_on_notify
                            else R.string.turn_off_notify
                        )

                        val icon =
                            if (!user.isOffNotify) R.drawable.ic_notification_on_black
                            else R.drawable.ic_notification_off_black

                        binding.lnSetting.getChildAt(0).apply {
                            findViewById<TextViewFont>(R.id.tv_name).text =
                                notifyString

                            findViewById<AppCompatImageView>(R.id.img_icon).setImageResource(icon)
                        }

                        listIterator.set(user)
                        break
                    }
                }
            }

        }
    }

    private fun checkCurrentConversation(id: String, block: () -> Unit) {
        if (viewModel.conversationId == id) {
            block()
        }
    }

    private fun leaveGroup(conversation: Conversation) {
        _leaveInSilent = false
        var isMain = false
        for (item in conversation.conversationUsers) {
            if (item.user.id.equals(viewModel.user.id)) {
                val role = item.getVaiTro()
                isMain = ConversationRole.fromType(role).type == ConversationRole.MAIN.type
                break
            }
        }
        showDialog(DialogLeaveGroupBinding.inflate(layoutInflater)) { vb, dialog ->
            with(vb) {
                tvTitle.setText(R.string.leave_group)
                tvDescription.setText(
                    if (isMain) R.string.warning_main_leave_conversation
                    else R.string.warning_member_leave_conversation
                )

                swSilent.isChecked = _leaveInSilent
                swSilent.setOnCheckedChangeListener { buttonView, isChecked ->
                    _leaveInSilent = isChecked
                }

                btnCancel.click { dialog.dismiss() }
                btnConfirm.apply {
                    setText(R.string.leave_group)
                    click {
                        viewModel.setLoading(true)
                        SignalRManager.leaveGroup(viewModel.conversationId, /*_leaveInSilent*/true)
                    }
                }

            }
        }
    }

    private fun changeNotify(offNotify: Boolean) {
        if (offNotify) {
            SignalRManager.turnNotifyConversation(viewModel.conversationId, true)
        } else {
            showDialog(DialogBaseBinding.inflate(layoutInflater)) { vb, dialog ->
                with(vb) {
                    tvTitle.setText(R.string.turn_off_notify)
                    tvDescription.setText(R.string.warning_turn_off_notify)

                    btnCancel.click { dialog.dismiss() }

                    btnConfirm.apply {
                        setText(R.string.off)
                        setBackgroundResource(R.drawable.bg_button_red)
                        click {
                            dialog.dismiss()
                            SignalRManager.turnNotifyConversation(
                                viewModel.conversationId,
                                false
                            )
                        }
                    }
                }
            }
        }
    }

    private fun changeInform() {
        showDialog(DialogConversationInformBinding.inflate(layoutInflater)) { vb, dialog ->
            dialogInform = vb
            with(vb) {
                frameChosenImage.click {
                    permission(PERMISSION_STORAGE) {
                        val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
                        chooseFile.setType("image/*")
                        val intentOpenOthers = Intent.createChooser(chooseFile, "")
                        picker.launch(intentOpenOthers)
                    }
                }
                if (!viewModel.conversation.value?.conversationAvatar.isNullOrEmpty()) {
                    val size = resources.getDimension(R.dimen.dp_21).toInt()
                    val padding = resources.getDimension(R.dimen.dp_5).toInt()
                    (imgPick.layoutParams as FrameLayout.LayoutParams).let { pick ->
                        pick.height = size
                        pick.width = size
                        pick.gravity = Gravity.BOTTOM or Gravity.END

                        imgPick.layoutParams = pick
                    }
                    imgPick.setPadding(padding, padding, padding, padding)
                    imgAvatar.loadAvatar(viewModel.conversation.value?.thumb)
                }

                edtName.apply {
                    setText(viewModel.conversation.value?.conversationName)
                    addListener(change = { viewModel.changeConversationName = it.toString() })
                }
                btnCancel.click {
                    viewModel.cancelChangeConversationInform()
                    dialog.dismiss()
                }
                btnConfirm.click {
                    dialog.dismiss()
                    viewModel.changeConversationInform()
                }
            }
        }
    }

    private fun handleChangeAvatar(result: Intent) {
        dialogInform.notNull {
            with(it) {
                val uri = result.data
                if (uri != null) {
                    try {
                        viewModel.changeConversationAvatar = uri

                        val size = resources.getDimension(R.dimen.dp_21).toInt()
                        val padding = resources.getDimension(R.dimen.dp_5).toInt()

                        (imgPick.layoutParams as FrameLayout.LayoutParams).let { pick ->
                            pick.height = size
                            pick.width = size
                            pick.gravity = Gravity.BOTTOM or Gravity.END

                            imgPick.layoutParams = pick
                        }
                        imgPick.setPadding(padding, padding, padding, padding)

                        Glide
                            .with(requireContext())
                            .asBitmap()
                            .load(ImageUtil.decodeBitmap(requireContext(), uri))
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
}