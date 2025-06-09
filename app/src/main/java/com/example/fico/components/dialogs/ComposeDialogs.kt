package com.example.fico.components.dialogs

import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fico.R
import com.example.fico.model.CreditCard

class ComposeDialogs {
    companion object{

        @Composable
        fun CreditCardItem(
            modifier: Modifier = Modifier,
            backgroundColor : Int,
            textColor: Int,
            cardName: String,
            paymentDayLabel: String,
            paymentDayValue: String,
            chipIconResId: Int
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .height(60.dp)
                        .background(Color(backgroundColor))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = chipIconResId),
                        contentDescription = "Chip",
                        modifier = Modifier
                            .size(30.dp)
                            .padding(start = 6.dp)
                    )

                    Text(
                        text = cardName,
                        color = Color(textColor),
                        fontSize = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp, end = 12.dp)
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = paymentDayLabel,
                            color = Color(textColor),
                            fontSize = 11.sp
                        )
                        Text(
                            text = paymentDayValue,
                            color = Color(textColor),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
            }
        }

        @Composable
        private fun CreditCardDialog(
            items: List<CreditCard>,
            onDismissRequest: () -> Unit,
            onItemClick: (CreditCard) -> Unit
        ) {
            AlertDialog(
                onDismissRequest = onDismissRequest,
                title = {
                    Text("Selecione o Cartão", style = MaterialTheme.typography.titleMedium)
                },
                text = {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        items(items) { item ->
                            CreditCardItem(
                                backgroundColor = item.colors.backgroundColor,
                                textColor = item.colors.textColor,
                                cardName = item.nickName,
                                paymentDayLabel = "Dia de Vencimento",
                                paymentDayValue = item.expirationDay.toString(), // Exemplo fixo, pode ser dinâmico
                                chipIconResId = R.drawable.chip, // Substitua pelo seu drawable
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .clickable { onItemClick(item) }
                            )
                        }
                    }
                },
                confirmButton = {}
            )
        }


        fun showComposeDialog(
            composeView: ComposeView,
            items: List<CreditCard>,
            contextView: View,
            onItemSelected: (CreditCard) -> Unit
        ) {
            composeView.setContent {
                MaterialTheme {
                    CreditCardDialog(
                        items = items,
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