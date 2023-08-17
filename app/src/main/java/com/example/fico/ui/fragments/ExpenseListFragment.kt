package com.example.fico.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.databinding.FragmentExpenseListBinding
import com.example.fico.ui.adapters.ExpenseListAdapter
import com.example.fico.ui.viewmodel.ExpenseListViewModel

class ExpenseListFragment : Fragment() {

    private var _binding : FragmentExpenseListBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<ExpenseListViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExpenseListBinding.inflate(inflater,container,false)
        val rootView = binding.root
        viewModel.getExpenseList(binding.rvExpenseList)
        binding.rvExpenseList.layoutManager = LinearLayoutManager(requireContext())
        val adapter = ExpenseListAdapter(emptyList())
        binding.rvExpenseList.adapter = adapter
        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}