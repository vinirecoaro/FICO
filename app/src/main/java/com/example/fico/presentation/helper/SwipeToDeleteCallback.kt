import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.model.Expense
import com.example.fico.presentation.adapters.ExpenseListAdapter
import com.example.fico.presentation.viewmodel.TransactionListViewModel

class SwipeToDeleteCallback(
    private val recyclerView: RecyclerView,
    private val viewModel: TransactionListViewModel,
    private val adapter: ExpenseListAdapter,
    private val lifecycleOwner: LifecycleOwner
) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {

        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        //Format data to delete item
        val position = viewHolder.adapterPosition
        val deleteItem = adapter.getDataAtPosition(position)

        //Check if expense in not installment
        if (deleteItem.id.length < 41) {
            val formattedPaymentDate = FormatValuesToDatabase().expenseDate(deleteItem.paymentDate)
            val formattedPurchaseDate =
                FormatValuesToDatabase().expenseDate(deleteItem.purchaseDate)
            val formattedInputDateTime =
                FormatValuesToDatabase().expenseDate(deleteItem.inputDateTime)
            val expencePrice = "-${deleteItem.price.replace("R$ ", "").replace(",", ".")}"
            val deleteItemFormatted = Expense(
                deleteItem.id,
                expencePrice,
                deleteItem.description,
                deleteItem.category,
                formattedPaymentDate,
                formattedPurchaseDate,
                formattedInputDateTime
            )
            //Delete Item and update expense list
            viewModel.deleteExpense(deleteItemFormatted)
        }else{
            viewModel.onInstallmentExpenseSwiped()
        }


    }
}
