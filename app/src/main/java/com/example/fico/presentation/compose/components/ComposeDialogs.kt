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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.fico.R
import com.example.fico.model.CreditCard
import com.example.fico.presentation.compose.components.ItemForLazyColumn.Companion.CreditCardItem
import com.example.fico.presentation.compose.theme.Theme

class ComposeDialogs {
    companion object{

        @Composable
        private fun CreditCardDialog(
            items: List<CreditCard>,
            contextView: View,
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
                            text = contextView.context.getString(R.string.cards),
                            style = MaterialTheme.typography.titleMedium,
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
                                        paymentDayLabel = contextView.context.getString(R.string.expiration_day),
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
                                        paymentDayLabel = contextView.context.getString(R.string.expiration_day),
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

        fun showComposeDialog(
            composeView: ComposeView,
            items: List<CreditCard>,
            defaultCreditCardId : String,
            contextView: View,
            onItemSelected: (CreditCard) -> Unit
        ) {
            composeView.setContent {
                Theme {
                    CreditCardDialog(
                        items = items,
                        contextView = contextView,
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
    }
}