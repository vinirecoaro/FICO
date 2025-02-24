package com.example.fico.presentation.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.components.ImagePickerBottomSheet
import com.example.fico.databinding.ActivityUserDataBinding
import com.example.fico.model.Budget
import com.example.fico.presentation.viewmodel.UserDataViewModel
import com.example.fico.utils.constants.StringConstants
import com.example.fico.utils.internet.ConnectionFunctions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.NumberFormat

class UserDataActivity : AppCompatActivity() {

    private val binding by lazy {ActivityUserDataBinding.inflate(layoutInflater)}
    private val viewModel : UserDataViewModel by inject()
    private val imageFileName = StringConstants.USER_DATA_ACTIVITY.PROFILE_IMAGE_FILE_NAME
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val file = File(this.filesDir, imageFileName)
            binding.ivUserProfile.setImageURI(Uri.fromFile(file))
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            saveImageToInternalStorage(bitmap)
            binding.ivUserProfile.setImageBitmap(bitmap)
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Snackbar.make(binding.ivUserProfile, "Camera permission is required", Snackbar.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.userDataToolbar.setTitle("Dados Pessoais")
        binding.userDataToolbar.setTitleTextColor(Color.WHITE)

        setSupportActionBar(binding.userDataToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setColorBasedOnTheme()
        getUserEmail()
        getUserName()
        setUpListeners()
        loadProfileImage()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setUpListeners(){
        binding.ivEditName.setOnClickListener {
            editNameDialog()
        }

        binding.userDataToolbar.setNavigationOnClickListener {
            finish()
        }

        viewModel.editUserNameResult.observe(this){result ->
            if (result){
                Snackbar.make(binding.ivUserProfile, getString(R.string.edit_name_success_message), Snackbar.LENGTH_LONG).show()
                getUserName()
            }else{
                Snackbar.make(binding.ivUserProfile, getString(R.string.edit_name_failure_message), Snackbar.LENGTH_LONG).show()
            }
        }

        binding.ivUserProfile.setOnClickListener {
            val bottomSheet = ImagePickerBottomSheet{ isCamera ->
                if(isCamera){
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        openCamera()
                    } else {
                        requestCameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }
                }else{
                    galleryLauncher.launch("image/*")
                }
            }
            bottomSheet.show(supportFragmentManager, "ImagePickerBottomSheet")
        }
    }

    private fun setColorBasedOnTheme(){
        when (this.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)){
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.tvName.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.baseline_person_24_light,0, 0, 0)
                binding.ivEditName.setImageResource(R.drawable.baseline_edit_light)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.tvName.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.baseline_person_24_black,0, 0, 0)
                binding.ivEditName.setImageResource(R.drawable.baseline_edit_black)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }

    private fun getUserName(){
        lifecycleScope.launch(Dispatchers.Main){
            binding.tvNameValue.text = viewModel.getUserName().await()
        }
    }

    private fun getUserEmail(){
        lifecycleScope.launch(Dispatchers.Main){
            binding.tvEmailValue.text = viewModel.getUserEmail().await()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun editNameDialog() : CompletableDeferred<Boolean>{
        val result = CompletableDeferred<Boolean>()
        val builder = MaterialAlertDialogBuilder(this)

        builder.setTitle(getString(R.string.edit_name))

        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.month_budget_input_field_for_alert_dialog, null)

        val textInputLayout = dialogView.findViewById<TextInputLayout>(R.id.til_month_budget_ad)
        textInputLayout.hint = getString(R.string.name)
        val newName = dialogView.findViewById<TextInputEditText>(R.id.et_month_budget_ad)
        newName.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(dialogView)

        builder.setPositiveButton(getString(R.string.save)) { dialog, which ->
            val saveButton =  (dialog as androidx.appcompat.app.AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.isEnabled = false
            if(hasInternetConnection()){
                lifecycleScope.launch {
                    if(newName.text.toString() != ""){
                        viewModel.editUserName(newName.text.toString())
                    }
                }
            }else{
                noInternetConnectionSnackBar()
            }
            saveButton.isEnabled = true
        }

        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(Dialog.BUTTON_POSITIVE).setTextColor(getAlertDialogTextButtonColor())
            dialog.getButton(Dialog.BUTTON_NEGATIVE).setTextColor(getAlertDialogTextButtonColor())
        }

        dialog.show()
        return result
    }

    private fun getAlertDialogTextButtonColor() : Int{
        val typedValue = TypedValue()
        val theme: Resources.Theme = this.theme
        theme.resolveAttribute(R.attr.alertDialogTextButtonColor, typedValue, true)
        val colorOnSurfaceVariant = ContextCompat.getColor(this, typedValue.resourceId)
        return colorOnSurfaceVariant
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun noInternetConnectionSnackBar(){
        Snackbar.make(
            binding.ivUserProfile,
            getString(R.string.without_network_connection),
            Snackbar.LENGTH_LONG
        )
            .setBackgroundTint(resources.getColor(android.R.color.holo_red_dark, theme))
            .setActionTextColor(resources.getColor(android.R.color.white, theme))
            .show()
    }

    private fun hasInternetConnection() : Boolean{
        return ConnectionFunctions().internetConnectionVerification(this)
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap) {
        val file = File(this.filesDir, imageFileName)
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
        }
    }

    private fun loadProfileImage() {
        val file = File(this.filesDir, imageFileName)
        if (file.exists()) {
            binding.ivUserProfile.setImageURI(Uri.fromFile(file))
        }
    }

    private fun openCamera() {
        val file = File(this.filesDir, imageFileName)
        val uri = FileProvider.getUriForFile(this, "com.example.fico.fileprovider", file)
        cameraLauncher.launch(uri)
    }
}