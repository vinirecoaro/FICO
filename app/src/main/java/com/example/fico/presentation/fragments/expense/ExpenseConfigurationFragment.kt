package com.example.fico.presentation.fragments.expense

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.R
import com.example.fico.databinding.FragmentConfigurationBinding
import com.example.fico.shared.constants.StringConstants
import com.example.fico.presentation.activities.expense.BudgetConfigurationListActivity
import com.example.fico.presentation.adapters.ExpenseConfigurationListAdapter
import com.example.fico.presentation.interfaces.OnListItemClick
import com.example.fico.presentation.viewmodel.ExpenseConfigurationViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import org.koin.android.ext.android.inject

class ExpenseConfigurationFragment : Fragment(),
    OnListItemClick {

    private var _binding : FragmentConfigurationBinding? = null
    private val binding get() = _binding!!
    private val viewModel : ExpenseConfigurationViewModel by inject()
    private lateinit var configuratonListAdapter: ExpenseConfigurationListAdapter
    private val sharedPref : SharedPreferences by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentConfigurationBinding.inflate(inflater, container, false)
        val rootView = binding.root

        binding.rvConfigurationList.layoutManager = LinearLayoutManager(requireContext())
        configuratonListAdapter = ExpenseConfigurationListAdapter(viewModel.configurationList)
        configuratonListAdapter.setOnItemClickListener(this)
        binding.rvConfigurationList.adapter = configuratonListAdapter

        setUpListeners()

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpListeners(){
        viewModel.setDefaultBudgetLiveData.observe(viewLifecycleOwner) { result ->
            if (result) {
                Snackbar.make(
                    binding.rvConfigurationList,
                    getString(R.string.payment_day_definition_success_message),
                    Snackbar.LENGTH_LONG
                ).show()
            }else{
                Snackbar.make(
                    binding.rvConfigurationList,
                    getString(R.string.payment_day_definition_fail_message),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onListItemClick(position: Int) {
        val item = viewModel.configurationList[position]
        if(item == StringConstants.EXPENSE_CONFIGURATION_LIST.BUDGET){
            startActivity(Intent(requireContext(), BudgetConfigurationListActivity::class.java))
        }else if(item == StringConstants.EXPENSE_CONFIGURATION_LIST.DEFAULT_PAYMENT_DATE){
            setDefaultPaymentDateAlertDialog()
        }
    }

    private fun setDefaultPaymentDateAlertDialog(){
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.default_payment_day))

        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.select_date_for_alert_dialog, null)

        val etDate = dialogView.findViewById<TextInputEditText>(R.id.et_payment_day_ad)
        val tvPaymentDay = dialogView.findViewById<TextView>(R.id.tv_payment_day_al)
        val paymentDay = sharedPref.getString(StringConstants.DATABASE.PAYMENT_DAY, null)

        if(paymentDay != null){
            val text = "${getString(R.string.default_day)} $paymentDay"
            tvPaymentDay.text = text
        }else{
            val text = getString(R.string.default_day_default_message)
            tvPaymentDay.text = text
        }

        builder.setView(dialogView)

        builder.setPositiveButton(getString(R.string.save)){dialog, which ->

            if(etDate.text.isNullOrEmpty()){
                Snackbar.make(binding.rvConfigurationList, getString(R.string.type_the_day), Snackbar.LENGTH_LONG).show()
            }else if (etDate.text.toString().toInt() > 31 ||etDate.text.toString().toInt() <= 0){
                Snackbar.make(binding.rvConfigurationList, getString(R.string.invalid_day), Snackbar.LENGTH_LONG).show()
            }else{
                with(sharedPref.edit()){
                    putString(StringConstants.DATABASE.PAYMENT_DAY, etDate.text.toString())
                    commit()
                }
                viewModel.setDefaultPaymentDay(etDate.text.toString())
            }
        }
        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(Dialog.BUTTON_POSITIVE).setTextColor(getAlertDialogTextButtonColor())
        }

        dialog.show()
    }

    private fun getAlertDialogTextButtonColor() : Int{
        val typedValue = TypedValue()
        val theme: Resources.Theme = requireContext().theme
        theme.resolveAttribute(R.attr.alertDialogTextButtonColor, typedValue, true)
        val colorOnSurfaceVariant = ContextCompat.getColor(requireContext(), typedValue.resourceId)
        return colorOnSurfaceVariant
    }

}