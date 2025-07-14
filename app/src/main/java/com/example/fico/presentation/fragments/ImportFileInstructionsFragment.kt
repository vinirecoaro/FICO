package com.example.fico.presentation.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fico.R
import com.example.fico.databinding.FragmentImportFileInstructionsBinding
import com.example.fico.model.ImportFileInstructionsComponents
import com.example.fico.utils.constants.StringConstants
import com.example.fico.utils.file_functions.FileFunctions

class ImportFileInstructionsFragment : Fragment() {

    private var _binding : FragmentImportFileInstructionsBinding? = null
    private val binding get() = _binding!!
    private val TITLE = "title"
    private val IMAGE = "image"
    private val DESCRIPTION = "description"
    private val BUTTON_STATE = "button_state"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentImportFileInstructionsBinding.inflate(inflater, container, false)
        var rootView = binding.root

        arguments?.let {
            val title = it.getString(getString(R.string.title))
            binding.tvTitle.text = title
            binding.ivIlustration.setImageResource(it.getInt(getString(R.string.image)))
            binding.tvDescription.text = it.getString(getString(R.string.description))
            if(it.getBoolean(getString(R.string.button_state))){
                binding.btFinish.visibility = View.VISIBLE
            }else{
                binding.btFinish.visibility = View.GONE
            }

            //Define button state
            if(title == getString(R.string.file_extension)){
                binding.btFinish.text = getString(R.string.download_base_workbook)
            }else if(title == getString(R.string.final_line_identificator)){
                binding.btFinish.text = getString(R.string.understood)
            }
        }

        setUpListeners()

        return rootView
    }

    companion object {
        @JvmStatic
        fun newInstance(instructionsComponents: ImportFileInstructionsComponents) =
            ImportFileInstructionsFragment().apply {
                arguments = Bundle().apply {
                    putString(TITLE, instructionsComponents.title)
                    putInt(IMAGE, instructionsComponents.drawableres)
                    putString(DESCRIPTION, instructionsComponents.description)
                    putBoolean(BUTTON_STATE, instructionsComponents.buttonState)
                }
            }
    }

    private fun setUpListeners(){
        binding.btFinish.setOnClickListener {
            if(binding.btFinish.text == getString(R.string.understood)){
                requireActivity().finish()
            }else if(binding.btFinish.text == getString(R.string.download_base_workbook)){
                FileFunctions.copyFromAssetsToDownloadDeviceFolder(
                    StringConstants.GENERAL.FILES,
                    StringConstants.ASSETS.WORKSHEET_IMPORT_TRANSACTION_FILE_NAME,
                    StringConstants.ASSETS.WORKSHEET_IMPORT_TRANSACTION_FILE_EXTENSION,
                    binding.root
                )
            }
        }
    }
}