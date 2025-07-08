package com.example.fico.presentation.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fico.R
import com.example.fico.presentation.compose.components.ItemForLazyColumn.Companion.CategoryFilterItem
import com.example.fico.presentation.compose.components.ItemForLazyColumn.Companion.CreditCardItem
import com.example.fico.presentation.compose.components.ItemForLazyColumn.Companion.UpdateFromFileItem
import com.example.fico.presentation.compose.theme.FICOTheme
import com.example.fico.presentation.compose.theme.LocalExtendedColors

class ItemForLazyColumn {
    companion object {

        @Composable
        fun CreditCardItem(
            modifier: Modifier = Modifier,
            backgroundColor : Int,
            textColor: Int,
            cardName: String,
            paymentDayLabel: String,
            paymentDayValue: String,
            chipIconResId: Int,
            isDefault: Boolean = false,
            defaultIconResId: Int? = null
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box{
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
                                modifier = Modifier.padding(top = 0.dp)
                            )
                        }
                    }
                    if (isDefault && defaultIconResId != null) {
                        Image(
                            painter = painterResource(id = defaultIconResId),
                            contentDescription = "Cartão padrão",
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.TopStart)
                                .offset(x = 4.dp, y = 4.dp)
                        )
                    }
                }
            }
        }

        @Composable
        fun CategoryFilterItem(
            modifier: Modifier = Modifier,
            categoryDescription : String,
            categoryIconResId : Int,
            selected : Boolean
        ){
            val extendedColors = LocalExtendedColors.current
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ){
                Row(
                    modifier = Modifier
                        .height(60.dp)
                        .background(extendedColors.customCardBackgroundColorSecondary)
                        .padding(8.dp)
                        .fillMaxWidth()
                    ,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    RadioButton(
                        selected = selected,
                        onClick = {},
                        enabled = false,
                        colors = RadioButtonDefaults.colors(
                            disabledSelectedColor = MaterialTheme.colorScheme.primary,
                            disabledUnselectedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                    Image(
                        painter = painterResource(id = categoryIconResId),
                        contentDescription = "Chip",
                        modifier = modifier
                            .size(40.dp)
                            .padding(start = 8.dp)
                    )
                    Text(
                        categoryDescription,
                        modifier = modifier.padding(start = 12.dp)
                    )
                }
            }
        }

        @Composable
        fun UpdateFromFileItem(
            date : String,
            nOfExpenses : String,
            nOfEarnings : String,
            nOfInstallmentExpenses : String,
            onDeleteIconClicked : () -> Unit
        ){
            val extendedColors = LocalExtendedColors.current
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(extendedColors.customCardBackgroundColor)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(date, fontSize = 25.sp)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { onDeleteIconClicked() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.delete_icon_24),
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(stringResource(R.string.expenses_2))
                            Text(nOfExpenses, fontSize = 20.sp)
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(stringResource(R.string.earnings_2))
                            Text(nOfEarnings, fontSize = 20.sp)
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(stringResource(R.string.installment_expense_short))
                            Text(nOfInstallmentExpenses, fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreditCardPreview(){
    FICOTheme {
        CreditCardItem(
            backgroundColor = Color(0xFF2196F3).toArgb(), // Azul
            textColor = Color.White.toArgb(),
            cardName = "Nubank Gold",
            paymentDayLabel = "Vencimento:",
            paymentDayValue = "10",
            chipIconResId = R.drawable.chip, // Substitua com seu recurso real
            isDefault = true,
            defaultIconResId = null // Substitua com seu recurso real
        )

    }
}

@Preview(showBackground = true)
@Composable
fun CategoryForFilterPreview(){
    FICOTheme {
        CategoryFilterItem(
            Modifier,
            "Categoria 2",
            R.drawable.category_icon_entertainment,
            true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateFromFileItemPreview() {
    FICOTheme {
        UpdateFromFileItem(
            "07/06/2025  07:20:20",
            "5", "10", "8",
            {}
        )
    }
}