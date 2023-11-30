package com.example.fico.ui.fragments

 import SwipeToDeleteCallback
 import android.content.Intent
 import android.content.pm.PackageManager
 import android.content.pm.ResolveInfo
 import android.content.res.Configuration
 import android.net.Uri
 import android.os.Build
 import android.os.Bundle
 import android.provider.Settings
 import android.text.Editable
 import android.text.TextWatcher
 import android.util.Log
 import android.view.*
 import android.widget.ArrayAdapter
 import androidx.annotation.RequiresApi
 import androidx.core.app.ActivityCompat
 import androidx.core.content.ContextCompat
 import androidx.core.content.FileProvider
 import androidx.core.widget.addTextChangedListener
 import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
 import androidx.recyclerview.widget.ItemTouchHelper
 import androidx.recyclerview.widget.LinearLayoutManager
 import com.example.fico.Manifest
 import com.example.fico.R
 import com.example.fico.databinding.FragmentExpenseListBinding
import com.example.fico.model.Expense
 import com.example.fico.service.constants.AppConstants
 import com.example.fico.ui.EditExpenseActivity
import com.example.fico.ui.adapters.ExpenseListAdapter
import com.example.fico.ui.interfaces.OnListItemClick
 import com.example.fico.ui.interfaces.XLSInterface
 import com.example.fico.ui.viewmodel.ExpenseListViewModel
 import com.google.gson.Gson
 import kotlinx.coroutines.delay
 import kotlinx.coroutines.launch
 import org.apache.poi.hssf.usermodel.HSSFWorkbook
 import org.apache.poi.ss.usermodel.WorkbookFactory
 import java.io.File
 import java.io.FileInputStream

class ExpenseListFragment : Fragment(), XLSInterface{

    private var _binding : FragmentExpenseListBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<ExpenseListViewModel>()
    private val expenseListAdapter = ExpenseListAdapter(emptyList())
    private var expenseMonthsList = arrayOf<String>()
    private val permissionRequestCode = 123
    private val permissions = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private companion object{
        private const val STORAGE_PERMISSION_CODE = 100
        private const val TAG = "PERMISSION_TAG"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentExpenseListBinding.inflate(inflater,container,false)
        val rootView = binding.root

        binding.rvExpenseList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenseList.adapter = expenseListAdapter

        val swipeToDeleteCallback = SwipeToDeleteCallback(binding.rvExpenseList,viewModel, expenseListAdapter)
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvExpenseList)

        viewModel.updateFilter("")

        setUpListeners()
        setColorBasedOnTheme()

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.expense_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.expense_list_menu_generate_excel_file -> {
                if (arePermissionsGranted()){
                    val expenseList = getExpenseList()
                    val gson = Gson()
                    var jsonArray =gson.toJsonTree(expenseList).asJsonArray

                    var file = generateXlsFile(requireActivity(), AppConstants.XLS.TITLES,
                        AppConstants.XLS.INDEX_NAME, jsonArray, HashMap(), AppConstants.XLS.SHEET_NAME,
                        AppConstants.XLS.FILE_NAME, 0)

                    if (file != null) {
                        shareFile(file)
                    }
                    return true
                }else{
                    requestPermissions()
                    return true
                }

            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun getExpenseList() : MutableList<Expense> {
        val expenseList : MutableList<Expense> = ArrayList()
        viewModel.expensesLiveData.observe(viewLifecycleOwner, Observer { expenses ->
            for(expense in expenses){
                var modifiedExpense = Expense("",expense.price.replace("R$ ",""),expense.description,expense.category, expense.date)
                expenseList.add(modifiedExpense)
            }
        })
        return expenseList
    }

    fun setUpListeners(){
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
        lifecycleScope.launch {
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

            binding.actvDate.addTextChangedListener (object : TextWatcher{
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

        }

    }

    private fun actvConfig() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, expenseMonthsList)
        binding.actvDate.setAdapter(adapter)
    }
    fun editExpense(expense : Expense){
        val intent = Intent(requireContext(), EditExpenseActivity::class.java)
        intent.putExtra("expense", expense)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
            viewModel.getExpenseList(binding.actvDate.text.toString())
            viewModel.getExpenseMonths()
    }

    private fun setColorBasedOnTheme(){
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
            requireActivity().packageManager.queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY)

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


    private fun arePermissionsGranted(): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(requireActivity(), permissions, permissionRequestCode)
    }

    private fun requestPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try {
                Log.d(TAG, "requestPermission: try")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
            }catch (e : java.lang.Exception){
                Log.d(TAG, "RequestPermission: ", e)
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            }
        }
        else{
            ActivityCompat.requestPermissions(requireActivity(), permissions,
                STORAGE_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionRequestCode) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Todas as permissões foram concedidas, você pode executar sua lógica aqui
            } else {
                // Algumas ou todas as permissões foram negadas, lide com isso conforme necessário
            }
        }
    }

}

