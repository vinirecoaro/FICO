package com.example.fico.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.databinding.FragmentConfigurationBinding
import com.example.fico.ui.adapters.ConfigurationListAdapter
import com.example.fico.ui.adapters.ExpenseListAdapter
import com.example.fico.ui.viewmodel.ConfigurationViewModel
import com.google.android.material.snackbar.Snackbar

class ConfigurationFragment : Fragment() {

    private var _binding : FragmentConfigurationBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<ConfigurationViewModel>()
    private lateinit var configuratonListAdapter: ConfigurationListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentConfigurationBinding.inflate(inflater,container,false)
        val rootView = binding.root

        binding.rvConfigurationList.layoutManager = LinearLayoutManager(requireContext())
        configuratonListAdapter = ConfigurationListAdapter(viewModel.configurationList)
        binding.rvConfigurationList.adapter = configuratonListAdapter

        setUpListeners()

        return rootView
    }


    private fun setUpListeners(){
        /*binding.ivInfoMoney.setOnClickListener {
            val snackbar = Snackbar.make(it, "Definir qual será o limite de gasto para o mês atual e os meses seguintes", Snackbar.LENGTH_LONG)
            snackbar.show()
        }*/

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}