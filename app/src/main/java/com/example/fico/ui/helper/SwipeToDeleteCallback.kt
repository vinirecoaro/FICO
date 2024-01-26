
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.model.Expense
import com.example.fico.ui.adapters.ExpenseListAdapter
import com.example.fico.ui.viewmodel.ExpenseListViewModel
import com.google.android.material.snackbar.Snackbar

class SwipeToDeleteCallback(private val recyclerView: RecyclerView, private val viewModel: ExpenseListViewModel, private val adapter: ExpenseListAdapter) :
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
            val day = deleteItem.date.substring(0, 2)
            val month = deleteItem.date.substring(3, 5)
            val year = deleteItem.date.substring(6, 10)
            val modifiedDate = "$year-$month-$day"
            val expencePrice = "-${deleteItem.price.replace("R$ ", "").replace(",", ".")}"
            val deleteItemFormatted = Expense(
                deleteItem.id,
                expencePrice,
                deleteItem.description,
                deleteItem.category,
                modifiedDate
            )

            //Delete Item and update expense list
            viewModel.deleteExpense(deleteItemFormatted)

            //Show snackbar to undo the action
            val snackbar = Snackbar.make(recyclerView, "Item excluido", Snackbar.LENGTH_LONG)
            snackbar.setAction("Desfazer") {
                /*val reformattedExpensePrice = deleteItem.price.replace("R$ ", "").replace(",", ".")
                val expense = Expense(
                    deleteItem.id,
                    reformattedExpensePrice,
                    deleteItem.description,
                    deleteItem.category,
                    modifiedDate
                )*/
                viewModel.undoDeleteExpense(deleteItem, false, 1)
            }.show()
    }
}
