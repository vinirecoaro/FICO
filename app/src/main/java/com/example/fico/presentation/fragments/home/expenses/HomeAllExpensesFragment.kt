package com.example.fico.presentation.fragments.home.expenses

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.fico.databinding.FragmentHomeAllExpensesBinding

class HomeAllExpensesFragment : Fragment() {

    private var _binding : FragmentHomeAllExpensesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeAllExpensesBinding.inflate(inflater, container, false)
        val rootView = binding.root



        return rootView
    }

}