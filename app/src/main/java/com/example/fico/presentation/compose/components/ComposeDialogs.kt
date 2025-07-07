package com.example.fico.presentation.compose.components

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.fico.R
import com.example.fico.model.CreditCard
import com.example.fico.model.CreditCardColors
import com.example.fico.model.TransactionsCategory
import com.example.fico.presentation.compose.components.ItemForLazyColumn.Companion.CategoryFilterItem
import com.example.fico.presentation.compose.components.ItemForLazyColumn.Companion.CreditCardItem
import com.example.fico.presentation.compose.theme.FICOTheme

class ComposeDialogs {
    companion object{

        @Composable
         fun CreditCardDialog(
            items: List<CreditCard>,
            title: String,
            paymentDayLabel : String,
            defaultCreditCardId : String,
            onDismissRequest: () -> Unit,
            onItemClick: (CreditCard) -> Unit
        ) {
            Dialog(
                onDismissRequest = onDismissRequest,
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn {
                            items(items) { item ->
                                if(defaultCreditCardId == item.id){
                                    CreditCardItem(
                                        backgroundColor = item.colors.background,
                                        textColor = item.colors.text,
                                        cardName = item.nickName,
                                        paymentDayLabel = paymentDayLabel,
                                        paymentDayValue = item.expirationDay.toString(),
                                        chipIconResId = R.drawable.chip,
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                            .clickable { onItemClick(item) },
                                        isDefault = true,
                                        defaultIconResId = R.drawable.spark
                                    )
                                }else{
                                    CreditCardItem(
                                        backgroundColor = item.colors.background,
                                        textColor = item.colors.text,
                                        cardName = item.nickName,
                                        paymentDayLabel = paymentDayLabel,
                                        paymentDayValue = item.expirationDay.toString(),
                                        chipIconResId = R.drawable.chip,
                                        modifier = Modifier
                                            .padding(vertical = 4.dp)
                                            .clickable { onItemClick(item) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        fun showCreditCardListDialog(
            composeView: ComposeView,
            items: List<CreditCard>,
            defaultCreditCardId : String,
            title: String,
            paymentDayLabel : String,
            onItemSelected: (CreditCard) -> Unit
        ) {
            composeView.setContent {
                FICOTheme {
                    CreditCardDialog(
                        items = items,
                        title = title,
                        paymentDayLabel = paymentDayLabel,
                        defaultCreditCardId = defaultCreditCardId,
                        onDismissRequest = {
                            composeView.setContent {}
                            composeView.visibility = View.GONE
                        },
                        onItemClick = { selected ->
                            composeView.setContent {}
                            composeView.visibility = View.GONE
                            onItemSelected(selected)
                        }
                    )
                }
            }
            composeView.visibility = View.VISIBLE
        }

        fun showCategoryListDialog(
            composeView: ComposeView,
            items: List<TransactionsCategory>,
            title: String,
            onFilterButtonClick: (List<Int>) -> Unit
        ) {
            composeView.setContent {
                FICOTheme {
                    CategoryListFilterDialog(
                        items = items,
                        title = title,
                        onDismissRequest = {
                            composeView.setContent {}
                            composeView.visibility = View.GONE
                        },
                        onFilterButtonClick = { descriptionResId ->
                            composeView.setContent {}
                            composeView.visibility = View.GONE
                            onFilterButtonClick(descriptionResId)
                        }
                    )
                }
            }
            composeView.visibility = View.VISIBLE
        }

        @Composable
        fun CategoryListFilterDialog(
            items: List<TransactionsCategory>,
            title: String,
            onDismissRequest: () -> Unit,
            onFilterButtonClick: (List<Int>) -> Unit
        ) {
            val selectedCategories = mutableListOf<Int>()
            Dialog(
                onDismissRequest = onDismissRequest,
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn {
                            items(items) { item ->
                                var selected by rememberSaveable { mutableStateOf(false) }
                                CategoryFilterItem(
                                    categoryDescription = stringResource(id = item.descriptionResId),
                                    categoryIconResId = item.iconResId,
                                    selected = selected,
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            selectedCategories.add(item.descriptionResId)
                                            selected = !selected
                                       }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onDismissRequest) {
                                Text("Cancelar")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                onFilterButtonClick(selectedCategories)
                            }) {
                                Text("Filtrar")
                            }
                        }
                    }
                }
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun CategoryListFilterDialogPreview(){
    FICOTheme {
        ComposeDialogs.CategoryListFilterDialog(
            items = listOf(
                TransactionsCategory.BET,
                TransactionsCategory.ELETRONICS_1,
                TransactionsCategory.CAR,
            ),
            title = "Categorias",
            onDismissRequest = {},
            onFilterButtonClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreditCardDialogPreview(){
    ComposeDialogs.CreditCardDialog(
        items = List(5){ CreditCard("Teste", "Teste", 1, 1, CreditCardColors.DARK_RED)},
        title = "Cartões",
        paymentDayLabel = "Dia de pagamento",
        defaultCreditCardId = "Test",
        onDismissRequest = {},
        onItemClick = {}
    )
}
