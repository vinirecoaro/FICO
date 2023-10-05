import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
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
        viewModel.deleteExpense(deleteItem)
        adapter.removeItem(position)
    }
}
