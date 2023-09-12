package com.example.fico.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.databinding.FragmentConfigurationBinding
import com.example.fico.service.constants.AppConstants
import com.example.fico.ui.BudgetConfigurationListActivity
import com.example.fico.ui.adapters.ConfigurationListAdapter
import com.example.fico.ui.interfaces.OnConfigurationItemClick
import com.example.fico.ui.viewmodel.ConfigurationViewModel

class ConfigurationFragment : Fragment(), OnConfigurationItemClick {

    private var _binding : FragmentConfigurationBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<ConfigurationViewModel>()
    private lateinit var configuratonListAdapter: ConfigurationListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentConfigurationBinding.inflate(inflater,container,false)
        val rootView = binding.root

        binding.rvConfigurationList.layoutManager = LinearLayoutManager(requireContext())
        configuratonListAdapter = ConfigurationListAdapter(viewModel.configurationList)
        configuratonListAdapter.setOnItemClickListener(this)
        binding.rvConfigurationList.adapter = configuratonListAdapter

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onConfigurationItemClick(position: Int) {
        val item = viewModel.configurationList[position]
        if(item == AppConstants.CONFIGURATION_LIST.BUDGET){
            startActivity(Intent(requireContext(), BudgetConfigurationListActivity::class.java))
        }
    }

}