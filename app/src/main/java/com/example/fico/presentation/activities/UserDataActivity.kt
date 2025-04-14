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
import android.text.InputType
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.components.Dialogs
import com.example.fico.components.ImagePickerBottomSheet
import com.example.fico.components.PersonalizedSnackBars
import com.example.fico.databinding.ActivityUserDataBinding
import com.example.fico.presentation.viewmodel.UserDataViewModel
import com.example.fico.utils.constants.StringConstants
import com.example.fico.utils.internet.ConnectionFunctions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream

class UserDataActivity : AppCompatActivity() {

    private val binding by lazy {ActivityUserDataBinding.inflate(layoutInflater)}
    private val viewModel : UserDataViewModel by inject()
    private val imageFileName = StringConstants.USER_DATA_ACTIVITY.PROFILE_IMAGE_FILE_NAME
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            val file = File(this.filesDir, imageFileName)
            val uri = Uri.fromFile(file)
            binding.ivUserProfile.setImageURI(null)
            binding.ivUserProfile.setImageURI(uri)
            binding.ivUserProfile.invalidate()
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

        binding.cvEditProfileImage.setOnClickListener {
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
    private fun editNameDialog(){
        val dialog = Dialogs.dialogModelTwo(
            this,
            this,
            getString(R.string.edit_name),
            getString(R.string.name),
            InputType.TYPE_CLASS_TEXT,
            getString(R.string.save)
        ){ newName -> editName(newName)}

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun editName(newName : String){
        if(hasInternetConnection()){
            lifecycleScope.launch {
                if(newName != ""){
                    viewModel.editUserName(newName)
                }
            }
        }else{
            PersonalizedSnackBars.noInternetConnection(binding.ivEditName, this).show()
        }
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