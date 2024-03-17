package com.example.fico.presentation.fragments.expense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fico.R
import com.example.fico.databinding.FragmentImportFileInstructionsBinding
import com.example.fico.domain.model.ImportFileInstructionsComponents

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
            binding.tvTitle.text = it.getString(getString(R.string.title))
            binding.ivIlustration.setImageResource(it.getInt(getString(R.string.image)))
            binding.tvDescription.text = it.getString(getString(R.string.description))
            if(it.getBoolean(getString(R.string.button_state))){
                binding.btFinish.visibility = View.VISIBLE
            }else{
                binding.btFinish.visibility = View.GONE
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
            requireActivity().finish()
        }
    }
}