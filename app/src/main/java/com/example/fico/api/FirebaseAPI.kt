package com.example.fico.api

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fico.interfaces.AuthInterface
import com.example.fico.interfaces.CreditCardInterface
import com.example.fico.interfaces.TransactionsInterface
import com.example.fico.interfaces.UserDataInterface
import com.example.fico.model.CreditCard
import com.example.fico.model.Earning
import com.example.fico.model.Expense
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.model.RecurringTransaction
import com.example.fico.model.UpdateFromFileExpenseList
import com.example.fico.model.User
import com.example.fico.utils.constants.StringConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class FirebaseAPI(
    private val auth : FirebaseAuth,
    private val database : FirebaseDatabase
) : AuthInterface, UserDataInterface, TransactionsInterface, CreditCardInterface {
    private val rootRef = database.getReference(StringConstants.DATABASE.USERS)
    private lateinit var user_root : DatabaseReference
    private lateinit var user_info : DatabaseReference
    private lateinit var expenses : DatabaseReference
    private lateinit var transactions : DatabaseReference
    private lateinit var total_expenses_price : DatabaseReference
    private lateinit var expenses_information_per_month : DatabaseReference
    private lateinit var expense_list : DatabaseReference
    private lateinit var recurring_transactions_list : DatabaseReference
    private lateinit var default_expense_values : DatabaseReference
    private lateinit var earnings : DatabaseReference
    private lateinit var earningsList : DatabaseReference
    private lateinit var credit_card_list : DatabaseReference

    fun updateReferences() {
        user_root = rootRef.child(auth.currentUser?.uid.toString())
        user_info = user_root.child(StringConstants.DATABASE.USER_INFO)
        expenses = user_root.child(StringConstants.DATABASE.EXPENSES)
        transactions = user_root.child(StringConstants.DATABASE.TRANSACTIONS)
        total_expenses_price = expenses.child(StringConstants.DATABASE.TOTAL_EXPENSE)
        expenses_information_per_month = expenses.child(StringConstants.DATABASE.INFORMATION_PER_MONTH)
        expense_list = expenses.child(StringConstants.DATABASE.EXPENSES_LIST)
        recurring_transactions_list = transactions.child(StringConstants.DATABASE.RECURRING_TRANSACTIONS_LIST)
        default_expense_values = expenses.child(StringConstants.DATABASE.DEFAULT_VALUES)
        earnings = user_root.child(StringConstants.DATABASE.EARNINGS)
        earningsList = earnings.child(StringConstants.DATABASE.EARNINGS_LIST)
        credit_card_list = expenses.child(StringConstants.DATABASE.CREDIT_CARD_LIST)
    }

    suspend fun updateExpensesPath(){
        updateExpensePerListInformationPath()
        updateDefaultValuesPath()
        updateInformationPerMonthPath()
        updateTotalExpensePath()
    }

    suspend fun currentUser(): FirebaseUser? = withContext(Dispatchers.IO) {
        return@withContext auth.currentUser
    }

    override suspend fun isLogged() : Result<Boolean> {
        return try{
            val currentUser = currentUser()
            if(currentUser != null){
                updateReferences()
                Result.success(true)
            }else{
                Result.success(false)
            }
        }catch (e : Exception){
            Result.failure(e)
        }
    }

    override suspend fun register(user: User, password: String): Result<User> {
        return try{
            val result = auth.createUserWithEmailAndPassword(user.email, password).await()
            val firebaseUser = result.user
            if (firebaseUser != null) {
                val newUser = User(user.name, user.email, firebaseUser.uid)
                updateReferences()
                addNewUserOnDatabase().await()
                setUserName(user.name).await()
                Result.success(newUser)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override suspend fun login(user: User, password: String): Result<Boolean> {
        return try {
            val result = auth.signInWithEmailAndPassword(user.email, password).await()
            if(result.user != null){
                updateReferences()
                if(!verifyExistsExpensesPath()){
                    updateExpensePerListInformationPath()
                    updateDefaultValuesPath()
                    updateInformationPerMonthPath()
                    updateTotalExpensePath()
                }
                val currentUser = currentUser()
                if(currentUser?.isEmailVerified == true){
                    Result.success(true)
                }else{
                    Result.success(false)
                }
            }else{
                Result.failure(Exception(StringConstants.MESSAGES.USER_NOT_FOUND))
            }
        }catch (e: FirebaseAuthInvalidUserException){
            Result.failure(Exception(StringConstants.MESSAGES.INVALID_CREDENTIALS))
        }
        catch (e: FirebaseAuthInvalidCredentialsException){
            Result.failure(Exception(StringConstants.MESSAGES.INVALID_CREDENTIALS))
        }
        catch (e: Exception){
            Result.failure(Exception(StringConstants.MESSAGES.LOGIN_ERROR))
        }
    }

    override suspend fun sendVerificationEmail() : Result<Boolean>{
        return try{
            auth.currentUser?.sendEmailVerification()?.await()
            Result.success(true)
        }catch (e : Exception){
            Result.failure(e)
        }
    }

    suspend fun stateListener() = withContext(Dispatchers.IO) {
        return@withContext auth.addAuthStateListener { }
    }

    suspend fun logoff() = withContext(Dispatchers.IO) {
        return@withContext auth.signOut()
    }

    suspend fun resetPassword(email: String) = withContext(Dispatchers.IO) {
        return@withContext auth.sendPasswordResetEmail(email)
    }

    suspend fun addNewUserOnDatabase() = withContext(Dispatchers.IO) {
        expenses.child(StringConstants.DATABASE.EXPENSES_LIST).setValue("")
        expenses.child(StringConstants.DATABASE.INFORMATION_PER_MONTH).setValue("")
        expenses.child(StringConstants.DATABASE.TOTAL_EXPENSE).setValue("0.00")
    }

    override suspend fun getUserEmail() : Result<String> = withContext(Dispatchers.IO) {
        try{
            val email = currentUser()?.email.toString()
            Result.success(email)
        }catch (e : Exception){
            Result.failure(e)
        }

    }

    suspend fun editUserName(name: String) : Result<Unit> = withContext(Dispatchers.IO) {
        val result = CompletableDeferred<Boolean>()
        try {
            user_info.child(StringConstants.DATABASE.NAME).setValue(name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setUserName(name: String) = withContext(Dispatchers.IO) {
        user_info.child(StringConstants.DATABASE.NAME).setValue(name)
    }

    override suspend fun getUserName(): Result<String> = withContext(Dispatchers.IO) {
        return@withContext suspendCancellableCoroutine { continuation ->
            try {
                user_info.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val username = snapshot.child(StringConstants.DATABASE.NAME).value?.toString() ?: "User"
                        continuation.resume(Result.success(username))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Result.success("User"))
                    }
                })
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }
        }
    }

    suspend fun deleteExpense(oldExpense: Expense) : Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try{
            updateTotalExpense(oldExpense.price)
            updateInformationPerMonthPath(oldExpense)
            val oldExpenseReference = expense_list.child(oldExpense.id)
            oldExpenseReference.removeValue()
            Result.success(Unit)
        }catch (e : Exception){
            Result.failure(e)
        }
    }

    suspend fun deleteEarning(earning: Earning) : Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try{
            val reference = earningsList.child(earning.id)
            reference.removeValue()
            Result.success(Unit)
        }catch (e : Exception){
            Result.failure(e)
        }
    }

    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction) : Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try{
            val reference = recurring_transactions_list.child(recurringTransaction.id)
            reference.removeValue()
            Result.success(Unit)
        }catch (e : Exception){
            Result.failure(e)
        }
    }

    suspend fun deleteInstallmentExpense(
        removeFromExpenseList: MutableList<String>,
        expenseNOfInstallment: Int,
        updatedTotalExpense: String,
        updatedInformationPerMonth: MutableList<InformationPerMonthExpense>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val updates = mutableMapOf<String, Any?>()

        return@withContext try {
            // Remove from expense list
            updates.putAll(
                generateMapToRemoveUserExpenses(
                    removeFromExpenseList, expenseNOfInstallment
                )
            )

            // Add Updated Total Expense
            updates.putAll(generateMapToUpdateUserTotalExpense(updatedTotalExpense))

            // Add Information per Month
            updates.putAll(generateMapToUpdateInformationPerMonth(updatedInformationPerMonth))

            expenses.updateChildren(updates)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setDefaultBudget(budget: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val bigNum = BigDecimal(budget)
            val formattedBudget = bigNum.setScale(8, RoundingMode.HALF_UP).toString()
            default_expense_values.child(StringConstants.DATABASE.DEFAULT_BUDGET)
                .setValue(formattedBudget)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editBudget(date: String, newBudget: String, newAvailableNow: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val updatedInfoPerMonth =
                    generateMapToUpdateMonthBudget(date, newBudget, newAvailableNow)
                expenses.updateChildren(updatedInfoPerMonth)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun generateMapToUpdateMonthBudget(
        date: String, newBudget: String, newAvailableNow: String
    ): MutableMap<String, Any> {
        val updetedInfoPerMonth = mutableMapOf<String, Any>()

        updetedInfoPerMonth["${StringConstants.DATABASE.INFORMATION_PER_MONTH}/$date/${StringConstants.DATABASE.BUDGET}"] =
            newBudget
        updetedInfoPerMonth["${StringConstants.DATABASE.INFORMATION_PER_MONTH}/$date/${StringConstants.DATABASE.AVAILABLE_NOW}"] =
            newAvailableNow

        return updetedInfoPerMonth
    }

    override suspend fun getDefaultBudget(): Result<String> = withContext(Dispatchers.IO) {
        suspendCoroutine{ continuation ->
            try{
                default_expense_values.child(StringConstants.DATABASE.DEFAULT_BUDGET)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val value = snapshot.value.toString()
                            continuation.resume(Result.success(value))
                        }

                        override fun onCancelled(error: DatabaseError) {
                            continuation.resume(Result.failure(Exception(error.message)))
                        }
                    })
            }catch (error : Exception){
                continuation.resume(Result.failure(error))
            }
        }
    }

    suspend fun checkIfExistsOnDatabse(reference: DatabaseReference): Boolean =
        withContext(Dispatchers.IO) {
            val futureResult = CompletableDeferred<Boolean>()

            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    futureResult.complete(snapshot.exists())
                }

                override fun onCancelled(error: DatabaseError) {
                    futureResult.complete(false)
                }
            })

            return@withContext futureResult.await()
        }

    suspend fun checkIfExistDefaultBudget(): Boolean {
        return checkIfExistsOnDatabse(default_expense_values.child(StringConstants.DATABASE.DEFAULT_BUDGET))
    }

    private suspend fun updateTotalExpense(value: String) = withContext(Dispatchers.IO) {
        total_expenses_price.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentTotalExpense = BigDecimal(snapshot.value.toString())
                val addValue = BigDecimal(value)
                val newValue = currentTotalExpense.add(addValue)
                val newBigNum = BigDecimal(newValue.toString())
                val newFormatted = newBigNum.setScale(8, RoundingMode.HALF_UP)
                total_expenses_price.setValue(newFormatted.toString())
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    suspend fun addExpenseFromFile(masterExpenseList: UpdateFromFileExpenseList): Boolean =
        withContext(Dispatchers.IO) {
            val updates = mutableMapOf<String, Any>()
            val result = CompletableDeferred<Boolean>()

            try {
                // Add Expense List
                updates.putAll(generateMapToUpdateUserExpenses(masterExpenseList.expenseList))

                // Add Updated Total Expense
                updates.putAll(generateMapToUpdateUserTotalExpense(masterExpenseList.updatedTotalExpense))

                // Add Information per Month
                updates.putAll(generateMapToUpdateInformationPerMonth(masterExpenseList.updatedInformationPerMonth))

                expenses.updateChildren(updates)

                result.complete(true)
            } catch (e: Exception) {
                result.complete(false)
            }
        }

    suspend fun addExpense(
        expenseList: MutableList<Expense>,
        updatedTotalExpense: String,
        updatedInformationPerMonth: MutableList<InformationPerMonthExpense>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val updates = mutableMapOf<String, Any>()

        return@withContext try {
            // Add Expense List
            updates.putAll(generateMapToUpdateUserExpenses(expenseList))

            // Add Updated Total Expense
            updates.putAll(generateMapToUpdateUserTotalExpense(updatedTotalExpense))

            // Add Information per Month
            updates.putAll(generateMapToUpdateInformationPerMonth(updatedInformationPerMonth))

            expenses.updateChildren(updates)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addEarning(
        earning : Earning,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val updates = mutableMapOf<String, Any>()

        return@withContext try {
            // Add Expense List
            updates.putAll(generateMapToUpdateUserEarnings(earning))

            earnings.updateChildren(updates)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addRecurringTransaction(
        recurringExpense : RecurringTransaction
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val updates = mutableMapOf<String, Any>()

        return@withContext try {
            updates.putAll(generateMapToUpdateUserRecurringExpenses(recurringExpense))

            recurring_transactions_list.updateChildren(updates)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editExpense(
        expenseList: MutableList<Expense>,
        updatedTotalExpense: String,
        updatedInformationPerMonth: MutableList<InformationPerMonthExpense>,
        removeFromExpenseList: MutableList<String>,
        oldExpenseNOfInstallment: Int
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val updates = mutableMapOf<String, Any?>()

        try {
            // Remove from Expense List
            updates.putAll(
                generateMapToRemoveUserExpenses(
                    removeFromExpenseList, oldExpenseNOfInstallment
                )
            )

            expenses.updateChildren(updates)

            //Clear updates list
            updates.clear()

            // Add Expense List
            updates.putAll(generateMapToUpdateUserExpenses(expenseList))

            // Add Updated Total Expense
            updates.putAll(generateMapToUpdateUserTotalExpense(updatedTotalExpense))

            // Add Information per Month
            updates.putAll(generateMapToUpdateInformationPerMonth(updatedInformationPerMonth))

            expenses.updateChildren(updates)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editEarning(
        earning: Earning,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val updates = mutableMapOf<String, Any?>()

        try {

            // Add Earning List
            updates.putAll(generateMapToUpdateUserEarnings(earning))

            earnings.updateChildren(updates)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun editRecurringExpense(
        recurringExpense: RecurringTransaction,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val updates = mutableMapOf<String, Any?>()
        try {
            updates.putAll(generateMapToUpdateUserRecurringExpenses(recurringExpense))
            recurring_transactions_list.updateChildren(updates)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateInfoPerMonthAndTotalExpense(
        updatedTotalExpense: String,
        updatedInformationPerMonth: MutableList<InformationPerMonthExpense>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val updates = mutableMapOf<String, Any>()

        return@withContext try {
            // Add Updated Total Expense
            updates.putAll(generateMapToUpdateUserTotalExpense(updatedTotalExpense))

            // Add Information per Month
            updates.putAll(generateMapToUpdateInformationPerMonth(updatedInformationPerMonth))

            expenses.updateChildren(updates)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateMapToUpdateUserExpenses(expenseList: MutableList<Expense>): MutableMap<String, Any> {
        val updatesOfExpenseList = mutableMapOf<String, Any>()

        for (expense in expenseList) {
            updatesOfExpenseList["${StringConstants.DATABASE.EXPENSES_LIST}/${expense.id}/${StringConstants.DATABASE.PRICE}"] =
                expense.price
            updatesOfExpenseList["${StringConstants.DATABASE.EXPENSES_LIST}/${expense.id}/${StringConstants.DATABASE.DESCRIPTION}"] =
                expense.description
            updatesOfExpenseList["${StringConstants.DATABASE.EXPENSES_LIST}/${expense.id}/${StringConstants.DATABASE.PAYMENT_DATE}"] =
                expense.paymentDate
            updatesOfExpenseList["${StringConstants.DATABASE.EXPENSES_LIST}/${expense.id}/${StringConstants.DATABASE.PURCHASE_DATE}"] =
                expense.purchaseDate
            updatesOfExpenseList["${StringConstants.DATABASE.EXPENSES_LIST}/${expense.id}/${StringConstants.DATABASE.INPUT_DATE_TIME}"] =
                expense.inputDateTime
            updatesOfExpenseList["${StringConstants.DATABASE.EXPENSES_LIST}/${expense.id}/${StringConstants.DATABASE.CATEGORY}"] =
                expense.category
        }

        return updatesOfExpenseList
    }

    private fun generateMapToUpdateUserEarnings(earning : Earning): MutableMap<String, Any> {
        val updatesOfEarningList = mutableMapOf<String, Any>()

        updatesOfEarningList["${StringConstants.DATABASE.EARNINGS_LIST}/${earning.id}/${StringConstants.DATABASE.VALUE}"] =
            earning.value
        updatesOfEarningList["${StringConstants.DATABASE.EARNINGS_LIST}/${earning.id}/${StringConstants.DATABASE.DESCRIPTION}"] =
            earning.description
        updatesOfEarningList["${StringConstants.DATABASE.EARNINGS_LIST}/${earning.id}/${StringConstants.DATABASE.CATEGORY}"] =
            earning.category
        updatesOfEarningList["${StringConstants.DATABASE.EARNINGS_LIST}/${earning.id}/${StringConstants.DATABASE.DATE}"] =
            earning.date
        updatesOfEarningList["${StringConstants.DATABASE.EARNINGS_LIST}/${earning.id}/${StringConstants.DATABASE.INPUT_DATE_TIME}"] =
            earning.inputDateTime

        return updatesOfEarningList
    }

    private fun generateMapToUpdateCreditCardList(creditCard: CreditCard): MutableMap<String, Any> {
        val updatesOfEarningList = mutableMapOf<String, Any>()
        val creditCardId = credit_card_list.push()

        updatesOfEarningList["${creditCardId}/${StringConstants.DATABASE.NAME}"] = creditCard.nickName
        updatesOfEarningList["${creditCardId}/${StringConstants.DATABASE.EXPIRATION_DAY}"] = creditCard.expirationDay
        updatesOfEarningList["${creditCardId}/${StringConstants.DATABASE.CLOSING_DAY}"] = creditCard.closingDay
        updatesOfEarningList["${creditCardId}/${StringConstants.DATABASE.BACKGROUND_COLOR_NAME_RES}"] = creditCard.colors.backgroundColorNameRes
        updatesOfEarningList["${creditCardId}/${StringConstants.DATABASE.BACKGROUND_COLOR}"] = creditCard.colors.backgroundColor
        updatesOfEarningList["${creditCardId}/${StringConstants.DATABASE.TEXT_COLOR}"] = creditCard.colors.textColor

        return updatesOfEarningList
    }

    private fun generateMapToUpdateUserRecurringExpenses(recurringExpense : RecurringTransaction): MutableMap<String, Any> {
        val updatesOfRecurringExpensesList = mutableMapOf<String, Any>()

        updatesOfRecurringExpensesList["${recurringExpense.id}/${StringConstants.DATABASE.PRICE}"] =
            recurringExpense.price
        updatesOfRecurringExpensesList["${recurringExpense.id}/${StringConstants.DATABASE.DESCRIPTION}"] =
            recurringExpense.description
        updatesOfRecurringExpensesList["${recurringExpense.id}/${StringConstants.DATABASE.CATEGORY}"] =
            recurringExpense.category
        updatesOfRecurringExpensesList["${recurringExpense.id}/${StringConstants.DATABASE.DAY}"] =
            recurringExpense.day
        updatesOfRecurringExpensesList["${recurringExpense.id}/${StringConstants.DATABASE.INPUT_DATE_TIME}"] =
            recurringExpense.inputDateTime
        updatesOfRecurringExpensesList["${recurringExpense.id}/${StringConstants.DATABASE.TYPE}"] =
            recurringExpense.type

        return updatesOfRecurringExpensesList
    }

    private fun generateMapToRemoveUserExpenses(
        expenseIdList: MutableList<String>, nOfInstallments: Int
    ): MutableMap<String, Any?> {
        val removeFromExpenseList = mutableMapOf<String, Any?>()

        for (i in 0 until nOfInstallments) {
            removeFromExpenseList["${StringConstants.DATABASE.EXPENSES_LIST}/${expenseIdList[i]}"] =
                null
        }

        return removeFromExpenseList
    }

    private fun generateMapToUpdateInformationPerMonth(informationPerMonthList: MutableList<InformationPerMonthExpense>): MutableMap<String, Any> {
        val updatesOfInformationPerMonth = mutableMapOf<String, Any>()

        // Add expenseList on updateList for installment expense
        for (monthInfo in informationPerMonthList) {
            updatesOfInformationPerMonth["${StringConstants.DATABASE.INFORMATION_PER_MONTH}/${monthInfo.date}/${StringConstants.DATABASE.AVAILABLE_NOW}"] =
                monthInfo.availableNow
            updatesOfInformationPerMonth["${StringConstants.DATABASE.INFORMATION_PER_MONTH}/${monthInfo.date}/${StringConstants.DATABASE.BUDGET}"] =
                monthInfo.budget
            updatesOfInformationPerMonth["${StringConstants.DATABASE.INFORMATION_PER_MONTH}/${monthInfo.date}/${StringConstants.DATABASE.EXPENSE}"] =
                monthInfo.monthExpense
        }

        return updatesOfInformationPerMonth
    }

    private fun generateMapToUpdateUserTotalExpense(updatedTotalExpense: String): MutableMap<String, Any> {
        val updatedTotalExpenseMap = mutableMapOf<String, Any>()
        updatedTotalExpenseMap[StringConstants.DATABASE.TOTAL_EXPENSE] = updatedTotalExpense

        return updatedTotalExpenseMap
    }

    private suspend fun updateInformationPerMonthPath(expense: Expense) =
        withContext(Dispatchers.IO) {
            expenses_information_per_month.child(expense.paymentDate.substring(0, 7))
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val updatedExpense =
                            sumOldAndNewValue(expense, snapshot, StringConstants.DATABASE.EXPENSE)
                        expenses_information_per_month.child(expense.paymentDate.substring(0, 7))
                            .child(StringConstants.DATABASE.EXPENSE).setValue(updatedExpense)
                        val updatedAvailable = subOldAndNewValue(
                            expense, snapshot, StringConstants.DATABASE.AVAILABLE_NOW
                        )
                        expenses_information_per_month.child(expense.paymentDate.substring(0, 7))
                            .child(StringConstants.DATABASE.AVAILABLE_NOW).setValue(updatedAvailable)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

    override suspend fun getTotalExpense(): Result<String> = withContext(Dispatchers.IO) {
        suspendCoroutine{ continuation ->
            try {
                total_expenses_price.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val value = snapshot.value.toString()
                        continuation.resume(Result.success(value))
                    }
                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Result.failure(Exception(error.message)))
                    }
                })
            }catch (error : Exception){
                continuation.resume(Result.failure(error))
            }
        }
    }

    fun sumOldAndNewValue(expense: Expense, snapshot: DataSnapshot, child: String): String {
        val current = BigDecimal(snapshot.child(child).value.toString())
        val add = BigDecimal(expense.price)
        val new = current.add(add)
        val newBigDecimalNumber = BigDecimal(new.toString())
        val newFormatted = newBigDecimalNumber.setScale(8, RoundingMode.HALF_UP)
        return newFormatted.toString()
    }

    fun subOldAndNewValue(expense: Expense, snapshot: DataSnapshot, child: String): String {
        val current = BigDecimal(snapshot.child(child).value.toString())
        val sub = BigDecimal(expense.price)
        val new = current.subtract(sub)
        val newBigDecimalNumber = BigDecimal(new.toString())
        val newFormatted = newBigDecimalNumber.setScale(8, RoundingMode.HALF_UP)
        return newFormatted.toString()
    }

    override suspend fun getExpenseList(): Result<List<Expense>> =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                val expenseList = mutableListOf<Expense>()
                expense_list.orderByKey().addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (childSnapshot in snapshot.children) {
                                val expense = FormatValuesFromDatabase().dataSnapshotToExpense(childSnapshot)
                                expenseList.add(expense)
                            }
                            continuation.resume(Result.success(expenseList))
                        }else{
                            continuation.resume(Result.success(emptyList()))
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Result.failure(Exception(error.message)))
                    }
                })
            }
        }

    override suspend fun getEarningList(): Result<List<Earning>> = withContext(Dispatchers.IO){
        suspendCoroutine{ continuation ->
            try{
                val earningList = mutableListOf<Earning>()
                earningsList.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            for(earning in snapshot.children){
                                val earningFormatted = FormatValuesFromDatabase().dataSnapshotToEarning(earning)
                                earningList.add(earningFormatted)
                            }
                            continuation.resume(Result.success(earningList))
                        }else{
                            continuation.resume(Result.success(emptyList()))
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Result.failure(Exception(error.message)))
                    }
                })
            }catch (error : Exception){
                continuation.resume(Result.failure(error))
            }
        }
    }

    override suspend fun getRecurringExpensesList(): Result<List<RecurringTransaction>> = withContext(Dispatchers.IO){
        suspendCoroutine{ continuation ->
            try{
                val recurringExpensesList = mutableListOf<RecurringTransaction>()
                recurring_transactions_list.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists()){
                            for(recurringExpenseSnapshot in snapshot.children){
                                val recurringExpense = FormatValuesFromDatabase().dataSnapshotToRecurringExpense(recurringExpenseSnapshot)
                                recurringExpensesList.add(recurringExpense)
                            }
                            continuation.resume(Result.success(recurringExpensesList))
                        }else{
                            continuation.resume(Result.success(emptyList()))
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Result.failure(Exception(error.message)))
                    }
                })
            }catch(error : Exception){
                continuation.resume(Result.failure(error))
            }
        }
    }

    override suspend fun getExpenseMonths(): Result<List<String>> =
        suspendCoroutine { continuation ->
            try{
                val expenseMonths = mutableListOf<String>()
                var isCompleted = false
                expenses_information_per_month.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (month in snapshot.children) {
                            val date: String = month.key.toString()
                            if (month.child(StringConstants.DATABASE.BUDGET).value != month.child(
                                    StringConstants.DATABASE.AVAILABLE_NOW
                                ).value
                            ) {
                                expenseMonths.add(date)
                            }
                        }
                        if (!isCompleted) { // Verifica se já foi retomado
                            isCompleted = true
                            continuation.resume(Result.success(expenseMonths))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        if (!isCompleted) { // Verifica se já foi retomado
                            isCompleted = true
                            continuation.resume(Result.success(emptyList()))
                        }
                    }

                })
            }catch(error : Exception){
                continuation.resume(Result.failure(error))
            }
        }

    override suspend fun getExpenseInfoPerMonth(): Result<List<InformationPerMonthExpense>> =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                try {
                    val infoList = mutableListOf<InformationPerMonthExpense>()
                    expenses_information_per_month.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (month in snapshot.children) {
                                val monthInfo = FormatValuesFromDatabase().dataSnapshotToInfoPerMonthExpense(month)
                                infoList.add(monthInfo)
                            }
                            continuation.resume(Result.success(infoList))
                        }

                        override fun onCancelled(error: DatabaseError) {
                            continuation.resume(Result.failure(Exception(error.message)))
                        }
                    })
                }catch (error : Exception){
                    continuation.resume(Result.failure(error))
                }
            }
        }

    fun formatDateFromFilterToDatabaseForInfoPerMonth(date: String): String {
        var formattedDate = ""
        val stringParts = date.split(" ")
        val month = stringParts[0]
        val year = stringParts[2]

        if (month == "Janeiro") {
            formattedDate = "$year-01"
        } else if (month == "Fevereiro") {
            formattedDate = "$year-02"
        } else if (month == "Março") {
            formattedDate = "$year-03"
        } else if (month == "Abril") {
            formattedDate = "$year-04"
        } else if (month == "Maio") {
            formattedDate = "$year-05"
        } else if (month == "Junho") {
            formattedDate = "$year-06"
        } else if (month == "Julho") {
            formattedDate = "$year-07"
        } else if (month == "Agosto") {
            formattedDate = "$year-08"
        } else if (month == "Setembro") {
            formattedDate = "$year-09"
        } else if (month == "Outubro") {
            formattedDate = "$year-10"
        } else if (month == "Novembro") {
            formattedDate = "$year-11"
        } else if (month == "Dezembro") {
            formattedDate = "$year-12"
        }
        return formattedDate
    }

    //Function created to fix data that was updated before new information about expense
    private suspend fun updateExpensePerListInformationPath() = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine<Unit> { continuation ->
            rootRef.child(auth.currentUser?.uid.toString())
                .child(StringConstants.DATABASE.EXPENSES_LIST)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val expenseList = mutableListOf<Expense>()
                        for (expense in snapshot.children) {
                            val date = expense.child(StringConstants.DATABASE.DATE).value.toString()
                            val inputDateTime = "$date-${FormatValuesToDatabase().timeNow()}"
                            val newExpense = Expense(
                                expense.key.toString(),
                                expense.child(StringConstants.DATABASE.PRICE).value.toString(),
                                expense.child(StringConstants.DATABASE.DESCRIPTION).value.toString(),
                                expense.child(StringConstants.DATABASE.CATEGORY).value.toString(),
                                expense.child(StringConstants.DATABASE.DATE).value.toString(),
                                expense.child(StringConstants.DATABASE.DATE).value.toString(),
                                inputDateTime
                            )
                            expenseList.add(newExpense)
                        }
                        val expenseListMap = generateMapToUpdateUserExpenses(expenseList)
                        user_root.child(StringConstants.DATABASE.EXPENSES)
                            .updateChildren(expenseListMap)
                        continuation.resume(Unit) {}
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
        }
    }

    private suspend fun updateDefaultValuesPath() = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine<Unit> { continuation ->
            user_root.child(StringConstants.DATABASE.DEFAULT_VALUES)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            user_root.child(StringConstants.DATABASE.EXPENSES)
                                .child(StringConstants.DATABASE.DEFAULT_VALUES)
                                .child(StringConstants.DATABASE.DEFAULT_BUDGET).setValue(
                                    snapshot.child(StringConstants.DATABASE.DEFAULT_BUDGET).value.toString()
                                )
                        }
                        continuation.resume(Unit) {}
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
        }
    }

    private suspend fun updateInformationPerMonthPath() = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine<Unit> { continuation ->
            user_root.child(StringConstants.DATABASE.INFORMATION_PER_MONTH)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val infoPerMonthList = mutableListOf<InformationPerMonthExpense>()
                        for (infoPerMonth in snapshot.children) {
                            infoPerMonthList.add(
                                InformationPerMonthExpense(
                                    infoPerMonth.key.toString(),
                                    infoPerMonth.child(StringConstants.DATABASE.AVAILABLE_NOW).value.toString(),
                                    infoPerMonth.child(StringConstants.DATABASE.BUDGET).value.toString(),
                                    infoPerMonth.child(StringConstants.DATABASE.EXPENSE).value.toString(),
                                )
                            )
                        }
                        val infoPerMonthMap =
                            generateMapToUpdateInformationPerMonth(infoPerMonthList)
                        user_root.child(StringConstants.DATABASE.EXPENSES)
                            .updateChildren(infoPerMonthMap)
                        continuation.resume(Unit) {}
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
        }
    }

    private suspend fun updateTotalExpensePath() = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine<Unit> { continuation ->
            user_root.child(StringConstants.DATABASE.TOTAL_EXPENSE)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        user_root.child(StringConstants.DATABASE.EXPENSES)
                            .child(StringConstants.DATABASE.TOTAL_EXPENSE).setValue(
                                snapshot.value.toString()
                            )
                        continuation.resume(Unit) {}
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
        }
    }

    suspend fun verifyExistsExpensesPath(): Boolean {
        return checkIfExistsOnDatabse(expenses)
    }

    suspend fun setDefaultPaymentDay(paymentDay: String, daysForClosingBill : String): Result<Unit> = withContext(Dispatchers.IO){
        try{
            default_expense_values.child(StringConstants.DATABASE.PAYMENT_DAY).setValue(paymentDay)
            default_expense_values.child(StringConstants.DATABASE.DAYS_FOR_CLOSING_BILL).setValue(daysForClosingBill)
            Result.success(Unit)
        }catch (e : Exception){
            Result.failure(e)
        }
    }

    //TODO do with addSingleListener
    override suspend fun getDefaultPaymentDay(): Result<String> = withContext(Dispatchers.IO){
        suspendCoroutine{ continuation ->
            try {
                default_expense_values.child(StringConstants.DATABASE.PAYMENT_DAY)
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.value != null){
                            val day = snapshot.value.toString()
                            continuation.resume(Result.success(day))
                        }else{
                            continuation.resume(Result.success(""))
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resume(Result.failure(Exception(error.message)))
                    }

                })

            }catch (error:Exception){
                continuation.resume(Result.failure(error))
            }
        }
    }

    override suspend fun getDaysForClosingBill(): Result<String> = withContext(Dispatchers.IO){
        suspendCoroutine{ continuation ->
            try {
                default_expense_values.child(StringConstants.DATABASE.DAYS_FOR_CLOSING_BILL)
                    .addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if(snapshot.value != null){
                                val daysForClosingBill = snapshot.value.toString()
                                continuation.resume(Result.success(daysForClosingBill))
                            }else{
                                continuation.resume(Result.success(""))
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            continuation.resume(Result.failure(Exception(error.message)))
                        }

                    })

            }catch (error : Exception){
                continuation.resume(Result.failure(error))
            }
        }
    }

    override suspend fun addCreditCard(
        creditCard: CreditCard
    ): Result<CreditCard> = withContext(Dispatchers.IO) {
        val updates = mutableMapOf<String, Any>()
        return@withContext try {
            updates.putAll(generateMapToUpdateCreditCardList(creditCard))

            credit_card_list.updateChildren(updates)

            Result.success(creditCard)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}