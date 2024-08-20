import com.example.fico.api.FirebaseAPI2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class FirebaseAPITest {

    private lateinit var firebaseAPI: FirebaseAPI2
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
        `when`(mockDatabaseReference.orderByKey()).thenReturn(mockDatabaseReference) // Adicionando esta linha

        // Injecting the mocks into FirebaseAPI
        firebaseAPI = FirebaseAPI2(auth = mockFirebaseAuth, database = mockFirebaseDatabase)
    }

    @Test
    fun `test observeExpenseList with data`() = runTest {
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
        `when`(mockPurchaseDateSnapshot.exists()).thenReturn(false)  // Simulating a missing field
        `when`(mockInputDateTimeSnapshot.exists()).thenReturn(false) // Simulating a missing field

        // Mock the ValueEventListener to trigger onDataChange
        doAnswer { invocation ->
            val listener = invocation.getArgument<ValueEventListener>(0)
            listener.onDataChange(mockDataSnapshot)
            null
        }.`when`(mockDatabaseReference).addValueEventListener(any(ValueEventListener::class.java))

        // Call the function observing the expense list
        val expensesList = firebaseAPI.observeExpenseList().await()

        // Verify that orderByKey was called and validate the results
        verify(mockDatabaseReference, times(1)).orderByKey()
        assertEquals(1, expensesList.size)
        assertEquals("Test Description", expensesList[0].description)
    }
}