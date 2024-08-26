package com.example.fico.api

import junit.framework.TestCase.assertEquals
import org.junit.Test

class ArrangeDataToUpdateToDatabaseTest{

    class calculateUpdatedTotalExpense{
        @Test
        fun `add a common expense`(){
            //Given
            val currentTotalExpense = "0.00"
            val expensePrice = "25.00"
            val expenseNOfInstallment = 1

            //When
            val updatedTotalExpense = ArrangeDataToUpdateToDatabase().calculateUpdatedTotalExpense(
                currentTotalExpense,
                expensePrice,
                expenseNOfInstallment
            )

            //Then
            assertEquals("25.00000000",updatedTotalExpense)
        }
    }
}