package com.example.fico.api

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fico.model.Budget
import com.example.fico.model.Expense
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.model.UpdateFromFileExpenseList
import com.example.fico.model.User
import com.example.fico.util.constants.AppConstants
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.SignInMethodQueryResult
import com.google.firebase.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class FirebaseAPI private constructor() {

    private object HOLDER {
        val INSTANCE = FirebaseAPI()
        val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val mDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    }

    companion object {
        val instance: FirebaseAPI by lazy { HOLDER.INSTANCE }
        private val auth: FirebaseAuth by lazy { HOLDER.mAuth }
        private val database: FirebaseDatabase by lazy { HOLDER.mDatabase }
        private val rootRef = database.getReference(AppConstants.DATABASE.USERS)
        private var total_expenses_price =
            rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.EXPENSES)
                .child(
                    AppConstants.DATABASE.TOTAL_EXPENSE
                )
        private var expenses_information_per_month =
            rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.EXPENSES)
                .child(
                    AppConstants.DATABASE.INFORMATION_PER_MONTH
                )
        private var expense_list =
            rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.EXPENSES)
                .child(
                    AppConstants.DATABASE.EXPENSES_LIST
                )
        private var default_expense_values =
            rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.EXPENSES)
                .child(
                    AppConstants.DATABASE.DEFAULT_VALUES
                )
        private var expenses =
            rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.EXPENSES)
        private var user_info =
            rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.USER_INFO)
        private var user_root = rootRef.child(auth.currentUser?.uid.toString())

    }

    fun updateReferences() {
        expenses =
            rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.EXPENSES)
        total_expenses_price =
            rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.EXPENSES)
                .child(AppConstants.DATABASE.TOTAL_EXPENSE)
        expenses_information_per_month =
            rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.EXPENSES)
                .child(AppConstants.DATABASE.INFORMATION_PER_MONTH)
        expense_list =
            rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.EXPENSES)
                .child(AppConstants.DATABASE.EXPENSES_LIST)
        default_expense_values =
            rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.EXPENSES)
                .child(AppConstants.DATABASE.DEFAULT_VALUES)
        user_info =
            rootRef.child(auth.currentUser?.uid.toString()).child(AppConstants.DATABASE.USER_INFO)
        user_root = rootRef.child(auth.currentUser?.uid.toString())
    }

    suspend fun currentUser(): FirebaseUser? = withContext(Dispatchers.IO) {
        return@withContext auth.currentUser
    }

    suspend fun createUser(user: User): Task<AuthResult> = withContext(Dispatchers.IO) {
        return@withContext auth.createUserWithEmailAndPassword(user.email, user.password)
    }

    suspend fun login(user: User): Task<AuthResult> = withContext(Dispatchers.IO) {
        return@withContext auth.signInWithEmailAndPassword(user.email, user.password)
    }

    suspend fun sendEmailVerification(): Task<Void>? = withContext(Dispatchers.IO) {
        return@withContext auth.currentUser?.sendEmailVerification()
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

    suspend fun verifyIfUserExists(): Task<SignInMethodQueryResult> = withContext(Dispatchers.IO) {
        return@withContext auth.fetchSignInMethodsForEmail(currentUser()?.email.toString())
    }

    suspend fun addNewUserOnDatabase() = withContext(Dispatchers.IO) {
        expenses.child(AppConstants.DATABASE.EXPENSES_LIST).setValue("")
        expenses.child(AppConstants.DATABASE.INFORMATION_PER_MONTH).setValue("")
        expenses.child(AppConstants.DATABASE.TOTAL_EXPENSE).setValue("0.00")
    }

    suspend fun getUserEmail(): String = withContext(Dispatchers.IO) {
        val email = currentUser()?.email.toString()
        return@withContext email ?: ""
    }

    suspend fun editUserName(name: String): Boolean = withContext(Dispatchers.IO) {
        val result = CompletableDeferred<Boolean>()
        try {
            user_info.child(AppConstants.DATABASE.NAME).setValue(name)
            result.complete(true)
        } catch (e: Exception) {
            result.complete(false)
        }
        return@withContext result.await()
    }

    suspend fun setUserName(name: String) = withContext(Dispatchers.IO) {
        user_info.child(AppConstants.DATABASE.NAME).setValue(name)
    }

    suspend fun getUserName(): String = withContext(Dispatchers.IO) {
        val userName = CompletableDeferred<String>()
        user_info.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(AppConstants.DATABASE.NAME).exists()) {
                    val username2 = snapshot.child(AppConstants.DATABASE.NAME).value.toString()
                    userName.complete(username2)
                } else {
                    userName.complete("User")
                }

            }

            override fun onCancelled(error: DatabaseError) {
                userName.complete("User")
            }
        })
        return@withContext userName.await()
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
            default_expense_values.child(AppConstants.DATABASE.DEFAULT_BUDGET)
                .setValue(formattedBudget)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBudgetPerMonth(): List<Budget> = suspendCoroutine { continuation ->
        var isCompleted = false
        expenses_information_per_month.addValueEventListener(object : ValueEventListener {
            val budgetPerMonth = mutableListOf<Budget>()
            override fun onDataChange(snapshot: DataSnapshot) {
                for (month in snapshot.children) {
                    val date = month.key.toString()
                    val budget =
                        month.child(AppConstants.DATABASE.BUDGET).value.toString().toFloat()
                    val formattedBudget = "R$ %.2f".format(budget).replace(".", ",")
                    val budgetItem = Budget(formattedBudget, date)
                    budgetPerMonth.add(budgetItem)
                }
                if (!isCompleted) { // Verifica se já foi retomado
                    isCompleted = true
                    continuation.resume(budgetPerMonth)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!isCompleted) { // Verifica se já foi retomado
                    isCompleted = true
                    continuation.resume(emptyList())
                }
            }

        })
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

        updetedInfoPerMonth["${AppConstants.DATABASE.INFORMATION_PER_MONTH}/$date/${AppConstants.DATABASE.BUDGET}"] =
            newBudget
        updetedInfoPerMonth["${AppConstants.DATABASE.INFORMATION_PER_MONTH}/$date/${AppConstants.DATABASE.AVAILABLE_NOW}"] =
            newAvailableNow

        return updetedInfoPerMonth
    }

    suspend fun getDefaultBudget(): Deferred<String> = withContext(Dispatchers.IO) {
        val defaultBudget = CompletableDeferred<String>()
        default_expense_values.child(AppConstants.DATABASE.DEFAULT_BUDGET)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.value.toString()
                    defaultBudget.complete(value)
                }

                override fun onCancelled(error: DatabaseError) {
                    defaultBudget.complete("")
                }
            })
        return@withContext defaultBudget
    }

    @RequiresApi(Build.VERSION_CODES.N)
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

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun checkIfExistDefaultBudget(): Boolean {
        return checkIfExistsOnDatabse(default_expense_values.child(AppConstants.DATABASE.DEFAULT_BUDGET))
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

    private fun generateMapToUpdateUserExpenses(expenseList: MutableList<Expense>): MutableMap<String, Any> {
        val updatesOfExpenseList = mutableMapOf<String, Any>()

        for (expense in expenseList) {
            updatesOfExpenseList["${AppConstants.DATABASE.EXPENSES_LIST}/${expense.id}/${AppConstants.DATABASE.PRICE}"] =
                expense.price
            updatesOfExpenseList["${AppConstants.DATABASE.EXPENSES_LIST}/${expense.id}/${AppConstants.DATABASE.DESCRIPTION}"] =
                expense.description
            updatesOfExpenseList["${AppConstants.DATABASE.EXPENSES_LIST}/${expense.id}/${AppConstants.DATABASE.PAYMENT_DATE}"] =
                expense.paymentDate
            updatesOfExpenseList["${AppConstants.DATABASE.EXPENSES_LIST}/${expense.id}/${AppConstants.DATABASE.PURCHASE_DATE}"] =
                expense.purchaseDate
            updatesOfExpenseList["${AppConstants.DATABASE.EXPENSES_LIST}/${expense.id}/${AppConstants.DATABASE.INPUT_DATE_TIME}"] =
                expense.inputDateTime
            updatesOfExpenseList["${AppConstants.DATABASE.EXPENSES_LIST}/${expense.id}/${AppConstants.DATABASE.CATEGORY}"] =
                expense.category
        }

        return updatesOfExpenseList
    }

    private fun generateMapToRemoveUserExpenses(
        expenseIdList: MutableList<String>, nOfInstallments: Int
    ): MutableMap<String, Any?> {
        val removeFromExpenseList = mutableMapOf<String, Any?>()

        for (i in 0 until nOfInstallments) {
            removeFromExpenseList["${AppConstants.DATABASE.EXPENSES_LIST}/${expenseIdList[i]}"] =
                null
        }

        return removeFromExpenseList
    }

    private fun generateMapToUpdateInformationPerMonth(informationPerMonthList: MutableList<InformationPerMonthExpense>): MutableMap<String, Any> {
        val updatesOfInformationPerMonth = mutableMapOf<String, Any>()

        // Add expenseList on updateList for installment expense
        for (monthInfo in informationPerMonthList) {
            updatesOfInformationPerMonth["${AppConstants.DATABASE.INFORMATION_PER_MONTH}/${monthInfo.date}/${AppConstants.DATABASE.AVAILABLE_NOW}"] =
                monthInfo.availableNow
            updatesOfInformationPerMonth["${AppConstants.DATABASE.INFORMATION_PER_MONTH}/${monthInfo.date}/${AppConstants.DATABASE.BUDGET}"] =
                monthInfo.budget
            updatesOfInformationPerMonth["${AppConstants.DATABASE.INFORMATION_PER_MONTH}/${monthInfo.date}/${AppConstants.DATABASE.EXPENSE}"] =
                monthInfo.monthExpense
        }

        return updatesOfInformationPerMonth
    }

    private fun generateMapToUpdateUserTotalExpense(updatedTotalExpense: String): MutableMap<String, Any> {
        val updatedTotalExpenseMap = mutableMapOf<String, Any>()
        updatedTotalExpenseMap[AppConstants.DATABASE.TOTAL_EXPENSE] = updatedTotalExpense

        return updatedTotalExpenseMap
    }

    private suspend fun updateInformationPerMonthPath(expense: Expense) =
        withContext(Dispatchers.IO) {
            expenses_information_per_month.child(expense.paymentDate.substring(0, 7))
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val updatedExpense =
                            sumOldAndNewValue(expense, snapshot, AppConstants.DATABASE.EXPENSE)
                        expenses_information_per_month.child(expense.paymentDate.substring(0, 7))
                            .child(AppConstants.DATABASE.EXPENSE).setValue(updatedExpense)
                        val updatedAvailable = subOldAndNewValue(
                            expense, snapshot, AppConstants.DATABASE.AVAILABLE_NOW
                        )
                        expenses_information_per_month.child(expense.paymentDate.substring(0, 7))
                            .child(AppConstants.DATABASE.AVAILABLE_NOW).setValue(updatedAvailable)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun getAvailableNow(date: String): String = withContext(Dispatchers.IO) {
        val availableNow = CompletableDeferred<String>()
        val reference = expenses_information_per_month
        reference.child(date).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val availableValue = snapshot.child(AppConstants.DATABASE.AVAILABLE_NOW)
                        .getValue(String::class.java)
                    availableNow.complete(availableValue.toString())
                } else {
                    availableNow.complete("---")
                }

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        return@withContext availableNow.await()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun getAvailableNow2(date: String): Flow<String?> = callbackFlow {

        val reference = expenses_information_per_month.child(date)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    trySend(
                        snapshot.child(AppConstants.DATABASE.AVAILABLE_NOW)
                            .getValue(String::class.java)
                    )
                } else {
                    trySend("---")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        reference.addValueEventListener(valueEventListener)

        awaitClose {
            reference.removeEventListener(valueEventListener)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun getMonthExpense(date: String): String = withContext(Dispatchers.IO) {
        val deferredExpense = CompletableDeferred<String>()
        val reference = expenses_information_per_month
        reference.child(date).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val monthExpenseValue =
                        snapshot.child(AppConstants.DATABASE.EXPENSE).getValue(String::class.java)
                    deferredExpense.complete(monthExpenseValue.toString())
                } else {
                    deferredExpense.complete("---")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                deferredExpense.completeExceptionally(error.toException())
            }
        })

        return@withContext deferredExpense.await()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun getMonthExpense2(date: String): Flow<String?> = callbackFlow {

        val reference = expenses_information_per_month.child(date)

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    trySend(
                        snapshot.child(AppConstants.DATABASE.EXPENSE).getValue(String::class.java)
                    )
                } else {
                    trySend("---")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        reference.addValueEventListener(valueEventListener)

        awaitClose {
            reference.removeEventListener(valueEventListener)
        }
    }

    suspend fun getTotalExpense(): Deferred<String> = withContext(Dispatchers.IO) {
        val totalExpense = CompletableDeferred<String>()
        total_expenses_price.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.value.toString()
                totalExpense.complete(value)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
        return@withContext totalExpense
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun observeTotalExpense(): Flow<String?> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Quando os dados mudam, enviamos o valor para o fluxo
                trySend(dataSnapshot.getValue(String::class.java))
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Em caso de erro, fechamos o fluxo com erro
                close(databaseError.toException())
            }
        }
        total_expenses_price.addValueEventListener(valueEventListener)

        awaitClose {
            total_expenses_price.removeEventListener(valueEventListener)
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

    suspend fun getExpenseList(filter: String = ""): Deferred<List<Expense>> =
        withContext(Dispatchers.IO) {
            val expensesList = CompletableDeferred<MutableList<Expense>>()
            expense_list.orderByKey().addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expenseList = mutableListOf<Expense>()
                    if (snapshot.exists()) {
                        if (filter == "") {
                            for (childSnapshot in snapshot.children) {
                                val id = childSnapshot.key.toString()
                                val priceDatabase =
                                    BigDecimal(childSnapshot.child(AppConstants.DATABASE.PRICE).value.toString())
                                val priceFormatted =
                                    priceDatabase.setScale(8, RoundingMode.HALF_UP).toString()
                                val description =
                                    childSnapshot.child(AppConstants.DATABASE.DESCRIPTION).value.toString()
                                val category =
                                    childSnapshot.child(AppConstants.DATABASE.CATEGORY).value.toString()
                                val paymentDateDatabase =
                                    childSnapshot.child(AppConstants.DATABASE.PAYMENT_DATE).value.toString()
                                var paymentDateFormatted = "${
                                    paymentDateDatabase.substring(
                                        8, 10
                                    )
                                }/${
                                    paymentDateDatabase.substring(
                                        5, 7
                                    )
                                }/${paymentDateDatabase.substring(0, 4)}"
                                if ((!childSnapshot.child(AppConstants.DATABASE.PURCHASE_DATE)
                                        .exists()) || (!childSnapshot.child(AppConstants.DATABASE.INPUT_DATE_TIME)
                                        .exists())
                                ) {
                                    val expense = Expense(
                                        id,
                                        priceFormatted,
                                        description,
                                        category,
                                        paymentDateFormatted,
                                        "",
                                        ""
                                    )
                                    expenseList.add(expense)
                                } else {
                                    val purchaseDateDatabase =
                                        childSnapshot.child(AppConstants.DATABASE.PURCHASE_DATE).value.toString()
                                    val purchaseDateFormatted = "${
                                        purchaseDateDatabase.substring(
                                            8, 10
                                        )
                                    }/${
                                        purchaseDateDatabase.substring(
                                            5, 7
                                        )
                                    }/${purchaseDateDatabase.substring(0, 4)}"
                                    val inputDateTime =
                                        childSnapshot.child(AppConstants.DATABASE.INPUT_DATE_TIME).value.toString()
                                    val expense = Expense(
                                        id,
                                        priceFormatted,
                                        description,
                                        category,
                                        paymentDateFormatted,
                                        purchaseDateFormatted,
                                        inputDateTime
                                    )
                                    expenseList.add(expense)
                                }

                            }
                        } else {
                            for (childSnapshot in snapshot.children) {
                                val dateDatabase =
                                    childSnapshot.child(AppConstants.DATABASE.PAYMENT_DATE).value.toString()
                                val dateFromDatabase = "${dateDatabase.substring(0, 4)}-${
                                    dateDatabase.substring(
                                        5, 7
                                    )
                                }"
                                val dateFromFilter =
                                    formatDateFromFilterToDatabaseForInfoPerMonth(filter)
                                if (dateFromDatabase == dateFromFilter) {
                                    val id = childSnapshot.key.toString()
                                    val priceDatabase =
                                        BigDecimal(childSnapshot.child(AppConstants.DATABASE.PRICE).value.toString())
                                    val priceFormatted =
                                        priceDatabase.setScale(8, RoundingMode.HALF_UP).toString()
                                    val description =
                                        childSnapshot.child(AppConstants.DATABASE.DESCRIPTION).value.toString()
                                    val category =
                                        childSnapshot.child(AppConstants.DATABASE.CATEGORY).value.toString()
                                    val paymentDateFormatted = "${
                                        dateDatabase.substring(
                                            8, 10
                                        )
                                    }/${dateDatabase.substring(5, 7)}/${
                                        dateDatabase.substring(
                                            0, 4
                                        )
                                    }"
                                    if ((!childSnapshot.child(AppConstants.DATABASE.PURCHASE_DATE)
                                            .exists()) || (!childSnapshot.child(AppConstants.DATABASE.INPUT_DATE_TIME)
                                            .exists())
                                    ) {
                                        val expense = Expense(
                                            id,
                                            priceFormatted,
                                            description,
                                            category,
                                            paymentDateFormatted,
                                            "",
                                            ""
                                        )
                                        expenseList.add(expense)
                                    } else {
                                        val purchaseDateDatabase =
                                            childSnapshot.child(AppConstants.DATABASE.PURCHASE_DATE).value.toString()
                                        val purchaseDateFormatted = "${
                                            purchaseDateDatabase.substring(
                                                8, 10
                                            )
                                        }/${
                                            purchaseDateDatabase.substring(
                                                5, 7
                                            )
                                        }/${purchaseDateDatabase.substring(0, 4)}"
                                        val inputDateTime =
                                            childSnapshot.child(AppConstants.DATABASE.INPUT_DATE_TIME).value.toString()
                                        val expense = Expense(
                                            id,
                                            priceFormatted,
                                            description,
                                            category,
                                            paymentDateFormatted,
                                            purchaseDateFormatted,
                                            inputDateTime
                                        )
                                        expenseList.add(expense)
                                    }

                                }
                            }
                        }
                    }
                    expensesList.complete(expenseList)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
            return@withContext expensesList
        }

    suspend fun observeExpenseList(filter: String = ""): Deferred<List<Expense>> =
        withContext(Dispatchers.IO) {
            val expensesList = CompletableDeferred<MutableList<Expense>>()
            expense_list.orderByKey().addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expenseList = mutableListOf<Expense>()
                    if (snapshot.exists()) {
                        if (filter == "") {
                            for (childSnapshot in snapshot.children) {
                                val id = childSnapshot.key.toString()
                                val priceDatabase =
                                    BigDecimal(childSnapshot.child(AppConstants.DATABASE.PRICE).value.toString())
                                val priceFormatted =
                                    priceDatabase.setScale(8, RoundingMode.HALF_UP).toString()
                                val description =
                                    childSnapshot.child(AppConstants.DATABASE.DESCRIPTION).value.toString()
                                val category =
                                    childSnapshot.child(AppConstants.DATABASE.CATEGORY).value.toString()
                                val paymentDateDatabase =
                                    childSnapshot.child(AppConstants.DATABASE.PAYMENT_DATE).value.toString()
                                var paymentDateFormatted = "${
                                    paymentDateDatabase.substring(
                                        8, 10
                                    )
                                }/${
                                    paymentDateDatabase.substring(
                                        5, 7
                                    )
                                }/${paymentDateDatabase.substring(0, 4)}"
                                if ((!childSnapshot.child(AppConstants.DATABASE.PURCHASE_DATE)
                                        .exists()) || (!childSnapshot.child(AppConstants.DATABASE.INPUT_DATE_TIME)
                                        .exists())
                                ) {
                                    val expense = Expense(
                                        id,
                                        priceFormatted,
                                        description,
                                        category,
                                        paymentDateFormatted,
                                        "",
                                        ""
                                    )
                                    expenseList.add(expense)
                                } else {
                                    val purchaseDateDatabase =
                                        childSnapshot.child(AppConstants.DATABASE.PURCHASE_DATE).value.toString()
                                    val purchaseDateFormatted = "${
                                        purchaseDateDatabase.substring(
                                            8, 10
                                        )
                                    }/${
                                        purchaseDateDatabase.substring(
                                            5, 7
                                        )
                                    }/${purchaseDateDatabase.substring(0, 4)}"
                                    val inputDateTime =
                                        childSnapshot.child(AppConstants.DATABASE.INPUT_DATE_TIME).value.toString()
                                    val expense = Expense(
                                        id,
                                        priceFormatted,
                                        description,
                                        category,
                                        paymentDateFormatted,
                                        purchaseDateFormatted,
                                        inputDateTime
                                    )
                                    expenseList.add(expense)
                                }

                            }
                        } else {
                            for (childSnapshot in snapshot.children) {
                                val dateDatabase =
                                    childSnapshot.child(AppConstants.DATABASE.PAYMENT_DATE).value.toString()
                                val dateFromDatabase = "${dateDatabase.substring(0, 4)}-${
                                    dateDatabase.substring(
                                        5, 7
                                    )
                                }"
                                val dateFromFilter =
                                    formatDateFromFilterToDatabaseForInfoPerMonth(filter)
                                if (dateFromDatabase == dateFromFilter) {
                                    val id = childSnapshot.key.toString()
                                    val priceDatabase =
                                        BigDecimal(childSnapshot.child(AppConstants.DATABASE.PRICE).value.toString())
                                    val priceFormatted =
                                        priceDatabase.setScale(8, RoundingMode.HALF_UP).toString()
                                    val description =
                                        childSnapshot.child(AppConstants.DATABASE.DESCRIPTION).value.toString()
                                    val category =
                                        childSnapshot.child(AppConstants.DATABASE.CATEGORY).value.toString()
                                    val paymentDateFormatted = "${
                                        dateDatabase.substring(
                                            8, 10
                                        )
                                    }/${dateDatabase.substring(5, 7)}/${
                                        dateDatabase.substring(
                                            0, 4
                                        )
                                    }"
                                    if ((!childSnapshot.child(AppConstants.DATABASE.PURCHASE_DATE)
                                            .exists()) || (!childSnapshot.child(AppConstants.DATABASE.INPUT_DATE_TIME)
                                            .exists())
                                    ) {
                                        val expense = Expense(
                                            id,
                                            priceFormatted,
                                            description,
                                            category,
                                            paymentDateFormatted,
                                            "",
                                            ""
                                        )
                                        expenseList.add(expense)
                                    } else {
                                        val purchaseDateDatabase =
                                            childSnapshot.child(AppConstants.DATABASE.PURCHASE_DATE).value.toString()
                                        val purchaseDateFormatted = "${
                                            purchaseDateDatabase.substring(
                                                8, 10
                                            )
                                        }/${
                                            purchaseDateDatabase.substring(
                                                5, 7
                                            )
                                        }/${purchaseDateDatabase.substring(0, 4)}"
                                        val inputDateTime =
                                            childSnapshot.child(AppConstants.DATABASE.INPUT_DATE_TIME).value.toString()
                                        val expense = Expense(
                                            id,
                                            priceFormatted,
                                            description,
                                            category,
                                            paymentDateFormatted,
                                            purchaseDateFormatted,
                                            inputDateTime
                                        )
                                        expenseList.add(expense)
                                    }

                                }
                            }
                        }
                    }
                    expensesList.complete(expenseList)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
            return@withContext expensesList
        }

    suspend fun getExpenseMonths(formatted: Boolean): List<String> =
        suspendCoroutine { continuation ->
            var isCompleted = false
            expenses_information_per_month.addValueEventListener(object : ValueEventListener {
                val expenseMonths = mutableListOf<String>()
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (month in snapshot.children) {
                        var date: String
                        if (formatted) {
                            date = formatDateForFilterOnExpenseList(month.key.toString())
                        } else {
                            date = month.key.toString()
                        }
                        if (month.child(AppConstants.DATABASE.BUDGET).value != month.child(
                                AppConstants.DATABASE.AVAILABLE_NOW
                            ).value
                        ) {
                            expenseMonths.add(date)
                        }
                    }
                    if (!isCompleted) { // Verifica se já foi retomado
                        isCompleted = true
                        continuation.resume(expenseMonths)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!isCompleted) { // Verifica se já foi retomado
                        isCompleted = true
                        continuation.resume(emptyList())
                    }
                }

            })
        }

    suspend fun getInformationPerMonth(): Deferred<MutableList<InformationPerMonthExpense>> =
        withContext(Dispatchers.IO) {
            val informationPerMonthInfo =
                CompletableDeferred<MutableList<InformationPerMonthExpense>>()
            expenses_information_per_month.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val infoList = mutableListOf<InformationPerMonthExpense>()
                    for (month in snapshot.children) {
                        val monthInfo = InformationPerMonthExpense(
                            month.key.toString(),
                            month.child(AppConstants.DATABASE.AVAILABLE_NOW).value.toString(),
                            month.child(AppConstants.DATABASE.BUDGET).value.toString(),
                            month.child(AppConstants.DATABASE.EXPENSE).value.toString(),
                        )
                        infoList.add(monthInfo)
                    }
                    informationPerMonthInfo.complete(infoList)
                }

                override fun onCancelled(error: DatabaseError) {
                    try {
                        informationPerMonthInfo.complete(emptyList<InformationPerMonthExpense>() as MutableList<InformationPerMonthExpense>)
                    } catch (e: Exception) {
                    }
                }
            })
            return@withContext informationPerMonthInfo
        }

    fun formatDateForFilterOnExpenseList(date: String): String {
        var formattedDate = ""
        val month = date.substring(5, 7)
        if (month == "01") {
            formattedDate = "Janeiro - ${date.substring(0, 4)}"
        } else if (month == "02") {
            formattedDate = "Fevereiro - ${date.substring(0, 4)}"
        } else if (month == "03") {
            formattedDate = "Março - ${date.substring(0, 4)}"
        } else if (month == "04") {
            formattedDate = "Abril - ${date.substring(0, 4)}"
        } else if (month == "05") {
            formattedDate = "Maio - ${date.substring(0, 4)}"
        } else if (month == "06") {
            formattedDate = "Junho - ${date.substring(0, 4)}"
        } else if (month == "07") {
            formattedDate = "Julho - ${date.substring(0, 4)}"
        } else if (month == "08") {
            formattedDate = "Agosto - ${date.substring(0, 4)}"
        } else if (month == "09") {
            formattedDate = "Setembro - ${date.substring(0, 4)}"
        } else if (month == "10") {
            formattedDate = "Outubro - ${date.substring(0, 4)}"
        } else if (month == "11") {
            formattedDate = "Novembro - ${date.substring(0, 4)}"
        } else if (month == "12") {
            formattedDate = "Dezembro - ${date.substring(0, 4)}"
        }
        return formattedDate
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
    suspend fun updateExpensePerListInformationPath() = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine<Unit> { continuation ->
            rootRef.child(auth.currentUser?.uid.toString())
                .child(AppConstants.DATABASE.EXPENSES_LIST)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val expenseList = mutableListOf<Expense>()
                        for (expense in snapshot.children) {
                            val date = expense.child(AppConstants.DATABASE.DATE).value.toString()
                            val inputDateTime = "$date-${FormatValuesToDatabase().timeNow()}"
                            val newExpense = Expense(
                                expense.key.toString(),
                                expense.child(AppConstants.DATABASE.PRICE).value.toString(),
                                expense.child(AppConstants.DATABASE.DESCRIPTION).value.toString(),
                                expense.child(AppConstants.DATABASE.CATEGORY).value.toString(),
                                expense.child(AppConstants.DATABASE.DATE).value.toString(),
                                expense.child(AppConstants.DATABASE.DATE).value.toString(),
                                inputDateTime
                            )
                            expenseList.add(newExpense)
                        }
                        val expenseListMap = generateMapToUpdateUserExpenses(expenseList)
                        user_root.child(AppConstants.DATABASE.EXPENSES)
                            .updateChildren(expenseListMap)
                        continuation.resume(Unit) {}
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
        }
    }

    suspend fun updateDefaultValuesPath() = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine<Unit> { continuation ->
            user_root.child(AppConstants.DATABASE.DEFAULT_VALUES)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            user_root.child(AppConstants.DATABASE.EXPENSES)
                                .child(AppConstants.DATABASE.DEFAULT_VALUES)
                                .child(AppConstants.DATABASE.DEFAULT_BUDGET).setValue(
                                    snapshot.child(AppConstants.DATABASE.DEFAULT_BUDGET).value.toString()
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

    suspend fun updateInformationPerMonthPath() = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine<Unit> { continuation ->
            user_root.child(AppConstants.DATABASE.INFORMATION_PER_MONTH)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val infoPerMonthList = mutableListOf<InformationPerMonthExpense>()
                        for (infoPerMonth in snapshot.children) {
                            infoPerMonthList.add(
                                InformationPerMonthExpense(
                                    infoPerMonth.key.toString(),
                                    infoPerMonth.child(AppConstants.DATABASE.AVAILABLE_NOW).value.toString(),
                                    infoPerMonth.child(AppConstants.DATABASE.BUDGET).value.toString(),
                                    infoPerMonth.child(AppConstants.DATABASE.EXPENSE).value.toString(),
                                )
                            )
                        }
                        val infoPerMonthMap =
                            generateMapToUpdateInformationPerMonth(infoPerMonthList)
                        user_root.child(AppConstants.DATABASE.EXPENSES)
                            .updateChildren(infoPerMonthMap)
                        continuation.resume(Unit) {}
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
        }
    }

    suspend fun updateTotalExpensePath() = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine<Unit> { continuation ->
            user_root.child(AppConstants.DATABASE.TOTAL_EXPENSE)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        user_root.child(AppConstants.DATABASE.EXPENSES)
                            .child(AppConstants.DATABASE.TOTAL_EXPENSE).setValue(
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

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun verifyExistsExpensesPath(): Boolean {
        return checkIfExistsOnDatabse(expenses)
    }

    suspend fun setDefaultPaymentDay(date: String): Boolean {
        val result = CompletableDeferred<Boolean>()

        default_expense_values.child(AppConstants.DATABASE.PAYMENT_DAY).setValue(date)
            .addOnSuccessListener {
                // Operação bem-sucedida
                result.complete(true)
            }.addOnFailureListener { e ->
                // Operação falhou
                result.complete(false)
            }

        return result.await()
    }

    suspend fun getDefaultPaymentDay(): String {
        val paymentDay = CompletableDeferred<String>()

        default_expense_values.child(AppConstants.DATABASE.PAYMENT_DAY).get()
            .addOnSuccessListener { snapshot ->
                if(snapshot.value != null){
                    paymentDay.complete(snapshot.value.toString())
                }else{
                    paymentDay.complete(AppConstants.DEFAULT_MESSAGES.FAIL)
                }
            }
            .addOnFailureListener {
                paymentDay.complete(AppConstants.DEFAULT_MESSAGES.FAIL)
            }
        return paymentDay.await()
    }
}