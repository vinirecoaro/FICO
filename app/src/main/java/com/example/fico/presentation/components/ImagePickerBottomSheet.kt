package com.example.fico.presentation.components

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fico.R
import com.example.fico.databinding.FragmentImagePickerBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ImagePickerBottomSheet(private val onOptionSelected: (Boolean) -> Unit) :
    BottomSheetDialogFragment() {

    private lateinit var binding: FragmentImagePickerBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImagePickerBottomSheetBinding.inflate(inflater, container, false)

        setUpListeners()
        setColorBasedOnTheme()

        return binding.root

    }

    private fun setUpListeners(){
        binding.llBottomSheetCamera.setOnClickListener {
            onOptionSelected(true)
            dismiss()
        }

        binding.llBottomSheetGallery.setOnClickListener {
            onOptionSelected(false)
            dismiss()
        }
    }

    private fun setColorBasedOnTheme() {
        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.ivBottomSheetCamera.setImageResource(R.drawable.photo_light)
                binding.ivBottomSheetGallery.setImageResource(R.drawable.gallery_light)

            }

            Configuration.UI_MODE_NIGHT_NO -> {
                binding.ivBottomSheetCamera.setImageResource(R.drawable.photo_dark)
                binding.ivBottomSheetGallery.setImageResource(R.drawable.gallery_dark)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }


}
