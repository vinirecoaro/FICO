package com.example.fico.components

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fico.databinding.FragmentImagePickerBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ImagePickerBottomSheet(private val onOptionSelected: (Boolean) -> Unit) :
    BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentImagePickerBottomSheetBinding.inflate(inflater, container, false)

        binding.btnCamera.setOnClickListener {
            onOptionSelected(true)
            dismiss()
        }

        binding.btnGallery.setOnClickListener {
            onOptionSelected(false)
            dismiss()
        }

        return binding.root
    }
}
