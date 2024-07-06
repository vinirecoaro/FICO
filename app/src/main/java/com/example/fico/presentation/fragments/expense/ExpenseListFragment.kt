package com.example.fico.presentation.fragments.expense

import SwipeToDeleteCallback
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.R
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.databinding.FragmentExpenseListBinding
import com.example.fico.model.Expense
import com.example.fico.util.constants.AppConstants
import com.example.fico.presentation.activities.expense.EditExpenseActivity
import com.example.fico.presentation.adapters.ExpenseListAdapter
import com.example.fico.presentation.interfaces.OnListItemClick
import com.example.fico.presentation.interfaces.XLSInterface
import com.example.fico.presentation.viewmodel.ExpenseListViewModel
import com.example.fico.presentation.viewmodel.shared.ExpensesViewModel
import com.example.fico.util.constants.DateFunctions
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File

class ExpenseListFragment : Fragment(), XLSInterface {

    private var _binding: FragmentExpenseListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ExpenseListViewModel by inject()
    private val expensesViewModel : ExpensesViewModel by inject()
    private val expenseListAdapter = ExpenseListAdapter(emptyList())
    private var expenseMonthsList = arrayOf<String>()
    private val permissionRequestCode = 123
    private val permissions = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private companion object {
        private const val STORAGE_PERMISSION_CODE = 100
        const val TAG = "PERMISSION_TAG"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpenseListBinding.inflate(inflater, container, false)
        val rootView = binding.root

        binding.rvExpenseList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenseList.adapter = expenseListAdapter

        val swipeToDeleteCallback =
            SwipeToDeleteCallback(binding.rvExpenseList, viewModel, expenseListAdapter, viewLifecycleOwner)
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvExpenseList)

        binding.actvDate.setText(DateFunctions().getCurrentlyDateForFilter())
        viewModel.updateFilter(DateFunctions().getCurrentlyDateForFilter())

        setUpListeners()
        setColorBasedOnTheme()

        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getExpenseList(binding.actvDate.text.toString())
        viewModel.getExpenseMonths()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.expense_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //TODO Change function and instructions to export expenses to file
            /*R.id.expense_list_menu_generate_excel_file -> {
                if (checkPermission()){
                    generateFileAndShare()
                }else{
                    lifecycleScope.launch {
                        requestPermission()
                    }
                }
                return true
            }*/

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun getExpenseList(): MutableList<Expense> {
        val expenseList: MutableList<Expense> = ArrayList()
        viewModel.expensesLiveData.observe(viewLifecycleOwner, Observer { expenses ->
            for (expense in expenses) {
                var modifiedExpense = Expense(
                    "",
                    FormatValuesFromDatabase().priceToFile(expense.price),
                    expense.description,
                    expense.category,
                    expense.paymentDate,
                    expense.purchaseDate,
                    expense.inputDateTime
                )
                expenseList.add(modifiedExpense)
            }
        })
        return expenseList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setUpListeners() {
        binding.actvDate.setOnClickListener {
            binding.actvDate.showDropDown()
        }
        binding.actvDate.setOnItemClickListener { parent, view, position, id ->
            val selectedOption = parent.getItemAtPosition(position).toString()
            viewModel.getExpenseList(selectedOption)
        }
        binding.ivClearFilter.setOnClickListener {
            binding.actvDate.setText("")
            viewModel.getExpenseList("")
        }

        viewModel.expensesLiveData.observe(viewLifecycleOwner, Observer { expenses ->
            expenseListAdapter.updateExpenses(expenses)
            expenseListAdapter.setOnItemClickListener(object : OnListItemClick {
                override fun onListItemClick(position: Int) {
                    val selectItem = expenses[position]
                    editExpense(selectItem)
                }
            })
        })

        viewModel.expenseMonthsLiveData.observe(viewLifecycleOwner, Observer { expenseMonths ->
            expenseMonthsList = expenseMonths.toTypedArray()
            actvConfig()
        })

        binding.actvDate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateFilter(binding.actvDate.text.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        viewModel.deleteExpenseResult.observe(viewLifecycleOwner){
            if(it){
                //Show snackbar to undo the action
                val snackbar = Snackbar.make(binding.rvExpenseList, "Item excluido", Snackbar.LENGTH_LONG)
                snackbar.setAction("Desfazer") {
                    lifecycleScope.launch {
                        viewModel.undoDeleteExpense(viewModel.deletedItem!!, false, 1).await()
                    }

                }.show()
            }else{
                //Show snackbar with failure message
                Snackbar.make(binding.rvExpenseList, "Falha ao excluir item", Snackbar.LENGTH_LONG).show()
            }
        }

    }

    private fun actvConfig() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            expenseMonthsList
        )
        binding.actvDate.setAdapter(adapter)
    }

    fun editExpense(expense: Expense) {
        val intent = Intent(requireContext(), EditExpenseActivity::class.java)
        val sureExpense = Expense(
            id = expense.id,
            price = expense.price,
            description = expense.description,
            category = expense.category,
            paymentDate = expense.paymentDate,
            purchaseDate = expense.purchaseDate,
            inputDateTime = expense.inputDateTime,
            nOfInstallment = expense.nOfInstallment
        )
        intent.putExtra("expense", sureExpense)
        startActivity(intent)
    }

    private fun setColorBasedOnTheme() {
        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.ivClearFilter.setImageResource(R.drawable.baseline_cancel_light)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                binding.ivClearFilter.setImageResource(R.drawable.baseline_cancel_dark)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }

    private fun shareFile(filePath: File) {
        val fileUri = FileProvider.getUriForFile(
            requireContext(),
            AppConstants.FILE_PROVIDER.AUTHORITY, // authorities deve corresponder ao valor definido no AndroidManifest.xml
            filePath
        )
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/vnd.ms-excel"
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val chooser = Intent.createChooser(intent, "Share File")

        val resInfoList: List<ResolveInfo> =
            requireActivity().packageManager.queryIntentActivities(
                chooser,
                PackageManager.MATCH_DEFAULT_ONLY
            )

        for (resolveInfo in resInfoList) {
            val packageName: String = resolveInfo.activityInfo.packageName
            requireActivity().grantUriPermission(
                packageName,
                fileUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        startActivity(chooser)
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Log.d(TAG, "requestPermission: try")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e: java.lang.Exception) {
                Log.d(TAG, "RequestPermission: ", e)
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), permissions,
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private val storageActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            Log.d(TAG, "storageActivityResultLauncher: ")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Log.d(TAG, "storageActivityResultLauncher: ")
                    lifecycleScope.launch {
                        delay(500)
                    }
                } else {
                    Log.d(TAG, "storageActivityResultLauncher: ")
                    Toast.makeText(
                        requireContext(),
                        "Manage External Storage Permission is denied ...",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {

            }
        }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val write = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val read = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionRequestCode) {
            if (grantResults.isNotEmpty()) {
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (write && read) {
                    Log.d(TAG, "onRequestPermissionResult: External Storage Permission granted")
                } else {
                    Log.d(TAG, "onRequestPermissionResult: External Storage Permission denied ...")
                    Toast.makeText(
                        requireContext(),
                        "Manage External Storage Permission is denied ...",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun generateFileAndShare() {
        val expenseList = getExpenseList()
        val gson = Gson()
        var jsonArray = gson.toJsonTree(expenseList).asJsonArray

        var file = generateXlsFile(
            requireActivity(), AppConstants.XLS.TITLES,
            AppConstants.XLS.INDEX_NAME, jsonArray, HashMap(), AppConstants.XLS.SHEET_NAME,
            AppConstants.XLS.FILE_NAME, 0
        )

        if (file != null) {
            shareFile(file)
        }
    }


}