import com.example.fico.api.FirebaseAPI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class FirebaseAPITest {

    private lateinit var firebaseAPI: FirebaseAPI
    private lateinit var mockDatabaseReference: DatabaseReference
    private lateinit var mockFirebaseAuth: FirebaseAuth
    private lateinit var mockFirebaseUser: FirebaseUser

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Mocking FirebaseAuth, FirebaseUser, and FirebaseDatabase
        mockFirebaseAuth = mock(FirebaseAuth::class.java)
        mockFirebaseUser = mock(FirebaseUser::class.java)
        val mockFirebaseDatabase = mock(FirebaseDatabase::class.java)
        mockDatabaseReference = mock(DatabaseReference::class.java)

        // Setup the FirebaseUser to return a test UID
        `when`(mockFirebaseUser.uid).thenReturn("test_uid")
        `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

        // Setup the FirebaseDatabase to return a mock reference
        `when`(mockFirebaseDatabase.getReference(anyString())).thenReturn(mockDatabaseReference)

        // Setup the DatabaseReference to return itself when child() or orderByKey() is called
        `when`(mockDatabaseReference.child(anyString())).thenReturn(mockDatabaseReference)
        `when`(mockDatabaseReference.orderByKey()).thenReturn(mockDatabaseReference)

        // Injecting the mocks into FirebaseAPI
        firebaseAPI = FirebaseAPI(auth = mockFirebaseAuth, database = mockFirebaseDatabase)
    }

    //Verify if getting data from database is returning a expenseList when have data
    @Test
    fun `test getExpenseList with data`() = runTest {
        val mockDataSnapshot = mock(DataSnapshot::class.java)
        val mockChildSnapshot = mock(DataSnapshot::class.java)

        // Setup the parent snapshot
        `when`(mockDataSnapshot.exists()).thenReturn(true)
        `when`(mockDataSnapshot.children).thenReturn(listOf(mockChildSnapshot).asIterable())

        // Setup the child snapshots to return values for "price", "description", etc.
        val mockPriceSnapshot = mock(DataSnapshot::class.java)
        val mockDescriptionSnapshot = mock(DataSnapshot::class.java)
        val mockCategorySnapshot = mock(DataSnapshot::class.java)
        val mockPaymentDateSnapshot = mock(DataSnapshot::class.java)
        val mockPurchaseDateSnapshot = mock(DataSnapshot::class.java)
        val mockInputDateTimeSnapshot = mock(DataSnapshot::class.java)

        // Setup the return of exists() function
        `when`(mockPurchaseDateSnapshot.exists()).thenReturn(true)
        `when`(mockInputDateTimeSnapshot.exists()).thenReturn(true)

        `when`(mockChildSnapshot.key).thenReturn("id")
        `when`(mockChildSnapshot.child("price")).thenReturn(mockPriceSnapshot)
        `when`(mockChildSnapshot.child("description")).thenReturn(mockDescriptionSnapshot)
        `when`(mockChildSnapshot.child("category")).thenReturn(mockCategorySnapshot)
        `when`(mockChildSnapshot.child("payment_date")).thenReturn(mockPaymentDateSnapshot)
        `when`(mockChildSnapshot.child("purchase_date")).thenReturn(mockPurchaseDateSnapshot)
        `when`(mockChildSnapshot.child("input_date_time")).thenReturn(mockInputDateTimeSnapshot)

        `when`(mockPriceSnapshot.value).thenReturn("123.45")
        `when`(mockDescriptionSnapshot.value).thenReturn("Test Description")
        `when`(mockCategorySnapshot.value).thenReturn("Test Category")
        `when`(mockPaymentDateSnapshot.value).thenReturn("2023-08-19")
        `when`(mockPurchaseDateSnapshot.value).thenReturn("2023-07-16")  // Simulating a missing field
        `when`(mockInputDateTimeSnapshot.value).thenReturn("2023-07-18-17-27-55") // Simulating a missing field

        // Mock the ValueEventListener to trigger onDataChange
        doAnswer { invocation ->
            val listener = invocation.getArgument<ValueEventListener>(0)
            listener.onDataChange(mockDataSnapshot)
            null
        }.`when`(mockDatabaseReference).addValueEventListener(any(ValueEventListener::class.java))

        // Call the function observing the expense list
        val expensesList = firebaseAPI.getExpenseList().await()

        // Verify that orderByKey was called and validate the results
        verify(mockDatabaseReference, times(1)).orderByKey()
        assertEquals(1, expensesList.size)
        assertEquals("123.45000000", expensesList[0].price)
        assertEquals("Test Description", expensesList[0].description)
        assertEquals("Test Category", expensesList[0].category)
        assertEquals("19/08/2023", expensesList[0].paymentDate)
        assertEquals("16/07/2023", expensesList[0].purchaseDate)
        assertEquals("2023-07-18-17-27-55", expensesList[0].inputDateTime)
    }

    //Verify if getting data from database is returning a empty expenseList when do not have data on database
    @Test
    fun `test getExpenseList with empty data`() = runTest {
        val mockDataSnapshot = mock(DataSnapshot::class.java)

        // Simulate an empty snapshot
        `when`(mockDataSnapshot.exists()).thenReturn(false)

        // Mock the ValueEventListener to trigger onDataChange
        doAnswer { invocation ->
            val listener = invocation.getArgument<ValueEventListener>(0)
            listener.onDataChange(mockDataSnapshot)
            null
        }.`when`(mockDatabaseReference).addValueEventListener(any(ValueEventListener::class.java))

        val expensesList = firebaseAPI.getExpenseList().await()

        verify(mockDatabaseReference, times(1)).orderByKey()
        assertTrue(expensesList.isEmpty())
    }

}