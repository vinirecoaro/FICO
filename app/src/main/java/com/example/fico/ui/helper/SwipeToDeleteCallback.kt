import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.model.Expense
import com.example.fico.ui.adapters.ExpenseListAdapter
import com.example.fico.ui.viewmodel.ExpenseListViewModel

class SwipeToDeleteCallback(private val viewModel: ExpenseListViewModel, private val adapter: ExpenseListAdapter) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val deleteItem = adapter.getDataAtPosition(position)
        val day = deleteItem.date.substring(0, 2)
        val month = deleteItem.date.substring(3, 5)
        val year = deleteItem.date.substring(6, 10)
        val modifiedDate = "$year-$month-$day"
        val expencePrice = "-${deleteItem.price.replace("R$ ","").replace(",",".")}"
        val deleteItemFormatted = Expense(deleteItem.id, expencePrice, deleteItem.description,deleteItem.category,modifiedDate)
        viewModel.deleteExpense(deleteItemFormatted)
    }
}
