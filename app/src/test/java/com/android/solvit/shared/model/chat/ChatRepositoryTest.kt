package com.android.solvit.shared.model.chat

import android.net.Uri
import com.android.solvit.shared.model.utils.uploadImageToStorage
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlin.test.DefaultAsserter.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify

class ChatRepositoryTest {
  private lateinit var mockFirebaseDatabase: FirebaseDatabase
  private lateinit var mockFirebaseFirestore: FirebaseFirestore
  private lateinit var chatRepository: ChatRepository
  private lateinit var mockDatabaseReference: DatabaseReference
  private lateinit var mockDataSnapshot: DataSnapshot
  private lateinit var mockFirebaseStorage: FirebaseStorage
  private lateinit var mockCollectionRef: CollectionReference
  private lateinit var mockDocumentRef: DocumentReference
  private lateinit var mockDocumentSnapshot: DocumentSnapshot
  private lateinit var mockTaskDoc: Task<DocumentSnapshot>
  private lateinit var mockTaskVoid: Task<Void>

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    mockFirebaseDatabase = mock(FirebaseDatabase::class.java)
    mockFirebaseStorage = mock(FirebaseStorage::class.java)
    mockFirebaseFirestore = mock(FirebaseFirestore::class.java)
    mockDatabaseReference = mock(DatabaseReference::class.java)
    mockDataSnapshot = mock(DataSnapshot::class.java)
    mockCollectionRef = mock(CollectionReference::class.java)
    mockDocumentRef = mock(DocumentReference::class.java)
    mockDocumentSnapshot = mock(DocumentSnapshot::class.java)
    mockTaskDoc = mock(Task::class.java) as Task<DocumentSnapshot>
    mockTaskVoid = mock(Task::class.java) as Task<Void>
    chatRepository =
        ChatRepositoryFirestore(mockFirebaseDatabase, mockFirebaseStorage, mockFirebaseFirestore)

    `when`(mockFirebaseDatabase.getReference(any())).thenReturn(mockDatabaseReference)
  }

  @Test
  fun `initChat Test with existing ChatRoom`() {
    val receiverUid = "receiverUid"
    val chatId = "existingChatId"

    val mockChildSnapshot = mock(DataSnapshot::class.java)
    val mockUserSnapshot = mock(DataSnapshot::class.java)

    // Mocking child snapshot structure to simulate Firebase data
    `when`(mockChildSnapshot.child("users")).thenReturn(mockUserSnapshot)
    `when`(mockChildSnapshot.key).thenReturn(chatId)
    `when`(mockUserSnapshot.value).thenReturn(mapOf("currentUserUid" to true, receiverUid to true))

    val mockPushReference = mock(DatabaseReference::class.java)
    `when`(mockDatabaseReference.child(any())).thenReturn(mockPushReference)
    `when`(mockDatabaseReference.push()).thenReturn(mockPushReference)
    `when`(mockPushReference.key).thenReturn("newChatId")

    val mockUsersReference = mock(DatabaseReference::class.java)
    `when`(mockPushReference.child("users")).thenReturn(mockUsersReference)
    val mockSetValueTask = mock(Task::class.java) as Task<Void>
    `when`(mockUsersReference.setValue(any())).thenReturn(mockSetValueTask)

    // Set up DataSnapshot to return children
    `when`(mockDataSnapshot.hasChildren()).thenReturn(true)
    `when`(mockDataSnapshot.children).thenReturn(listOf(mockChildSnapshot).asIterable())

    // Capture the ValueEventListener
    val valueEventListenerCaptor = argumentCaptor<ValueEventListener>()
    doNothing()
        .`when`(mockDatabaseReference)
        .addListenerForSingleValueEvent(valueEventListenerCaptor.capture())

    var resultChatId: String? = null

    chatRepository.initChat(
        false,
        "currentUserUid",
        onSuccess = { chatid -> resultChatId = chatid },
        onFailure = {},
        receiverUid)

    verify(mockDatabaseReference).addListenerForSingleValueEvent(valueEventListenerCaptor.capture())
    valueEventListenerCaptor.firstValue.onDataChange(mockDataSnapshot)

    // Check that the callback returned the expected chatId
    assertEquals(chatId, resultChatId)
  }

  @Test
  fun `initChat Test with new ChatRoom`() {
    val receiverUid = "receiverUid"
    val chatId = "newChatId"

    val mockPushReference = mock(DatabaseReference::class.java)
    `when`(mockDatabaseReference.child(any())).thenReturn(mockPushReference)
    `when`(mockDatabaseReference.push()).thenReturn(mockPushReference)
    `when`(mockPushReference.key).thenReturn("newChatId")

    val mockUsersReference = mock(DatabaseReference::class.java)
    `when`(mockPushReference.child("users")).thenReturn(mockUsersReference)
    val mockSetValueTask = mock(Task::class.java) as Task<Void>
    `when`(mockUsersReference.setValue(any())).thenReturn(mockSetValueTask)

    // Make sure that data Snapshot has no children
    `when`(mockDataSnapshot.hasChildren()).thenReturn(false)

    // Capture the ValueEventListener
    val valueEventListenerCaptor = argumentCaptor<ValueEventListener>()
    doNothing()
        .`when`(mockDatabaseReference)
        .addListenerForSingleValueEvent(valueEventListenerCaptor.capture())

    var resultChatId: String? = null

    chatRepository.initChat(
        false,
        "currentUserUid",
        onSuccess = { chatid -> resultChatId = chatid },
        onFailure = {},
        receiverUid)

    verify(mockDatabaseReference).addListenerForSingleValueEvent(valueEventListenerCaptor.capture())
    valueEventListenerCaptor.firstValue.onDataChange(mockDataSnapshot)

    // Check that the callback returned the expected chatId
    assertEquals(chatId, resultChatId)
  }

  @Test
  fun `sendMessage Test`() {
    val chatRoomId = "testChatRoom"
    val message =
        ChatMessage.TextMessage(
            "Test message", "senderName", "senderId", timestamp = System.currentTimeMillis())

    val mockPushReference = mock(DatabaseReference::class.java)
    `when`(mockDatabaseReference.child(chatRoomId)).thenReturn(mockPushReference)
    `when`(mockPushReference.child("chats")).thenReturn(mockPushReference)
    `when`(mockPushReference.push()).thenReturn(mockPushReference)
    `when`(mockPushReference.key).thenReturn("newMessageId")
    `when`(mockPushReference.child("newMessageId")).thenReturn(mockPushReference)

    val mockSetValueTask = mock(Task::class.java) as Task<Void>
    `when`(mockPushReference.setValue(any())).thenReturn(mockSetValueTask)

    var isSuccessCalled = false
    var isFailureCalled = false

    chatRepository.sendMessage(
        false,
        chatRoomId,
        message,
        onSuccess = { isSuccessCalled = true },
        onFailure = { isFailureCalled = true })

    // Verify that `setValue` was called on the right DatabaseReference
    verify(mockPushReference).setValue(message)
    assertEquals(true, isSuccessCalled)
    assertEquals(false, isFailureCalled)
  }

  @Test
  fun `listenForMessages Test`() {
    val chatRoomId = "testChatRoom"
    val mockMessageSnapshot1 = mock(DataSnapshot::class.java)
    val mockMessageSnapshot2 = mock(DataSnapshot::class.java)
    val message1 =
        ChatMessage.TextMessage(
            "Message 1", "text", "senderName", "Hello", timestamp = System.currentTimeMillis())
    val message2 =
        ChatMessage.TextMessage(
            "Message 2",
            "text",
            "senderName",
            "How Are you",
            timestamp = System.currentTimeMillis())

    val mockChildType1 = mock(DataSnapshot::class.java)
    val mockChildType2 = mock(DataSnapshot::class.java)
    `when`(mockMessageSnapshot1.child("type")).thenReturn(mockChildType1)
    `when`(mockChildType1.value).thenReturn("text")

    `when`(mockMessageSnapshot2.child("type")).thenReturn(mockChildType2)
    `when`(mockChildType2.value).thenReturn("text")

    `when`(mockMessageSnapshot1.getValue(ChatMessage.TextMessage::class.java)).thenReturn(message1)
    `when`(mockMessageSnapshot2.getValue(ChatMessage.TextMessage::class.java)).thenReturn(message2)

    `when`(mockDataSnapshot.children)
        .thenReturn(listOf(mockMessageSnapshot1, mockMessageSnapshot2).asIterable())

    val valueEventListenerCaptor = argumentCaptor<ValueEventListener>()
    `when`(mockDatabaseReference.child(chatRoomId)).thenReturn(mockDatabaseReference)
    `when`(mockDatabaseReference.child("chats")).thenReturn(mockDatabaseReference)
    `when`(mockDatabaseReference.orderByChild("timestamp")).thenReturn(mockDatabaseReference)

    var receivedMessages: List<ChatMessage>? = null
    chatRepository.listenForMessages(
        false, chatRoomId, onSuccess = { messages -> receivedMessages = messages }, onFailure = {})

    // Capture and trigger the ValueEventListener
    verify(mockDatabaseReference).addValueEventListener(valueEventListenerCaptor.capture())
    valueEventListenerCaptor.firstValue.onDataChange(mockDataSnapshot)

    // Verify that the message received are as expected
    assertEquals(listOf(message1, message2), receivedMessages)
  }

  @Test
  fun `listenForLastMessages Test`() {
    // Set up the current user
    val currentUserUid = "currentUserUid"

    // Mock the database reference for "messages"
    val mockChatNodeReference = mock(DatabaseReference::class.java)
    `when`(mockFirebaseDatabase.getReference("messages")).thenReturn(mockChatNodeReference)

    // Mock the query: orderByChild("users/$currentUserUid").equalTo(true)
    val mockQuery = mock(Query::class.java)
    `when`(mockChatNodeReference.orderByChild("users/$currentUserUid")).thenReturn(mockQuery)
    `when`(mockQuery.equalTo(true)).thenReturn(mockQuery)

    // Set up the chat rooms data
    val chatRoomId1 = "chatRoomId1"
    val chatRoomId2 = "chatRoomId2"

    val mockChatRoomSnapshot1 = mock(DataSnapshot::class.java)
    val mockChatRoomSnapshot2 = mock(DataSnapshot::class.java)

    `when`(mockChatRoomSnapshot1.key).thenReturn(chatRoomId1)
    `when`(mockChatRoomSnapshot2.key).thenReturn(chatRoomId2)

    val mockUsersSnapshot1 = mock(DataSnapshot::class.java)
    val mockUsersSnapshot2 = mock(DataSnapshot::class.java)

    `when`(mockChatRoomSnapshot1.child("users")).thenReturn(mockUsersSnapshot1)
    `when`(mockChatRoomSnapshot2.child("users")).thenReturn(mockUsersSnapshot2)

    // Mock children of "users"
    val child1 = mock(DataSnapshot::class.java)
    val child2 = mock(DataSnapshot::class.java)
    `when`(child1.key).thenReturn("user1Uid")
    `when`(child2.key).thenReturn("currentUserUid")
    `when`(mockUsersSnapshot1.children).thenReturn(listOf(child1, child2))

    // Mock children of "users"
    val child3 = mock(DataSnapshot::class.java)
    val child4 = mock(DataSnapshot::class.java)
    `when`(child3.key).thenReturn("user2Uid")
    `when`(child4.key).thenReturn("currentUserUid")
    `when`(mockUsersSnapshot2.children).thenReturn(listOf(child3, child3))

    val mockChatRoomsDataSnapshot = mock(DataSnapshot::class.java)
    `when`(mockChatRoomsDataSnapshot.hasChildren()).thenReturn(true)
    `when`(mockChatRoomsDataSnapshot.children)
        .thenReturn(listOf(mockChatRoomSnapshot1, mockChatRoomSnapshot2).asIterable())

    // Capture the ValueEventListener for the chat rooms query
    val valueEventListenerCaptor = argumentCaptor<ValueEventListener>()
    doNothing().`when`(mockQuery).addListenerForSingleValueEvent(valueEventListenerCaptor.capture())

    // Mock the database references for chat rooms' chats
    val mockChatRoomReference1 = mock(DatabaseReference::class.java)
    val mockChatRoomReference2 = mock(DatabaseReference::class.java)

    val mockChatsReference1 = mock(DatabaseReference::class.java)
    val mockChatsReference2 = mock(DatabaseReference::class.java)

    `when`(mockChatNodeReference.child(chatRoomId1)).thenReturn(mockChatRoomReference1)
    `when`(mockChatNodeReference.child(chatRoomId2)).thenReturn(mockChatRoomReference2)

    `when`(mockChatRoomReference1.child("chats")).thenReturn(mockChatsReference1)
    `when`(mockChatRoomReference2.child("chats")).thenReturn(mockChatsReference2)

    val mockChatsQuery1 = mock(Query::class.java)
    val mockChatsQuery2 = mock(Query::class.java)

    `when`(mockChatsReference1.orderByChild("timestamp")).thenReturn(mockChatsQuery1)
    `when`(mockChatsReference2.orderByChild("timestamp")).thenReturn(mockChatsQuery2)

    `when`(mockChatsQuery1.limitToLast(1)).thenReturn(mockChatsQuery1)
    `when`(mockChatsQuery2.limitToLast(1)).thenReturn(mockChatsQuery2)

    // Capture ValueEventListeners for the chats queries
    val chatsValueEventListenerCaptor1 = argumentCaptor<ValueEventListener>()
    val chatsValueEventListenerCaptor2 = argumentCaptor<ValueEventListener>()

    doNothing()
        .`when`(mockChatsQuery1)
        .addListenerForSingleValueEvent(chatsValueEventListenerCaptor1.capture())
    doNothing()
        .`when`(mockChatsQuery2)
        .addListenerForSingleValueEvent(chatsValueEventListenerCaptor2.capture())

    // Prepare the result variables
    var receivedMessages: Map<String?, ChatMessage>? = null
    var onFailureCalled = false

    // Call the method under test
    chatRepository.listenForLastMessages(
        "currentUserUid",
        onSuccess = { messages -> receivedMessages = messages },
        onFailure = { onFailureCalled = true })

    // Simulate the onDataChange for the chat rooms query
    verify(mockQuery).addValueEventListener(valueEventListenerCaptor.capture())
    valueEventListenerCaptor.firstValue.onDataChange(mockChatRoomsDataSnapshot)

    // Simulate the last messages for each chat room
    val lastMessage1 =
        ChatMessage.TextMessage(
            "Message from chatRoomId1", "text", "senderName", "senderId1", 2000L)
    val lastMessage2 =
        ChatMessage.TextMessage(
            "Message from chatRoomId2", "text", "senderName", "senderId2", 3000L)

    // Mock DataSnapshots for last messages
    val mockLastMessageSnapshot1 = mock(DataSnapshot::class.java)
    val mockLastMessageSnapshot2 = mock(DataSnapshot::class.java)

    val mockMessageDataSnapshot1 = mock(DataSnapshot::class.java)
    val mockMessageDataSnapshot2 = mock(DataSnapshot::class.java)

    `when`(mockLastMessageSnapshot1.exists()).thenReturn(true)
    `when`(mockLastMessageSnapshot2.exists()).thenReturn(true)

    `when`(mockLastMessageSnapshot1.children)
        .thenReturn(listOf(mockMessageDataSnapshot1).asIterable())
    `when`(mockLastMessageSnapshot2.children)
        .thenReturn(listOf(mockMessageDataSnapshot2).asIterable())

    `when`(mockMessageDataSnapshot1.getValue(ChatMessage.TextMessage::class.java))
        .thenReturn(lastMessage1)
    `when`(mockMessageDataSnapshot2.getValue(ChatMessage.TextMessage::class.java))
        .thenReturn(lastMessage2)

    val mockChildType1 = mock(DataSnapshot::class.java)
    val mockChildType2 = mock(DataSnapshot::class.java)
    `when`(mockMessageDataSnapshot1.child("type")).thenReturn(mockChildType1)
    `when`(mockChildType1.value).thenReturn("text")

    `when`(mockMessageDataSnapshot2.child("type")).thenReturn(mockChildType2)
    `when`(mockChildType2.value).thenReturn("text")

    // Simulate the onDataChange for the chats queries
    chatsValueEventListenerCaptor1.firstValue.onDataChange(mockLastMessageSnapshot1)
    chatsValueEventListenerCaptor2.firstValue.onDataChange(mockLastMessageSnapshot2)

    // Verify that the received messages are as expected
    val expectedMessages =
        mapOf(
            "user1Uid" to lastMessage1,
            "user2Uid" to lastMessage2) // Sorted by timestamp descending
    assertEquals(expectedMessages, receivedMessages)
    assertEquals(false, onFailureCalled)
  }

  @Test
  fun uploadImageToStorageTest() {
    // Mock dependencies
    val mockImageUri = mock(Uri::class.java)
    val mockFirebaseStorage = mock(FirebaseStorage::class.java)
    val mockStorageReference = mock(StorageReference::class.java)
    val mockUploadTask = mock(UploadTask::class.java)
    val mockDownloadTask = mock(Task::class.java) as Task<Uri>

    // Stubbing Firebase Storage reference behavior
    `when`(mockFirebaseStorage.reference).thenReturn(mockStorageReference)
    `when`(mockStorageReference.child(any())).thenReturn(mockStorageReference)
    `when`(mockStorageReference.putFile(eq(mockImageUri))).thenReturn(mockUploadTask)

    // Stubbing success behavior of putFile
    `when`(mockUploadTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val onSuccessListener = invocation.arguments[0] as OnSuccessListener<UploadTask.TaskSnapshot>
      onSuccessListener.onSuccess(mock(UploadTask.TaskSnapshot::class.java))
      mockUploadTask
    }

    // Stubbing success behavior of downloadUrl
    `when`(mockStorageReference.downloadUrl).thenReturn(mockDownloadTask)
    `when`(mockDownloadTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val onSuccessListener = invocation.arguments[0] as OnSuccessListener<Uri>
      onSuccessListener.onSuccess(mock(Uri::class.java))
      mockDownloadTask
    }

    // Verify the behavior of the tested function
    var wasSuccessCalled = false
    uploadImageToStorage(
        storage = mockFirebaseStorage,
        path = "testPath/",
        imageUri = mockImageUri,
        onSuccess = { url ->
          wasSuccessCalled = true
          assertNotNull(url)
        },
        onFailure = { fail("onFailure should not be called") })

    // Verify interactions
    verify(mockFirebaseStorage.reference).child(any())
    verify(mockStorageReference).putFile(eq(mockImageUri))
    verify(mockStorageReference).downloadUrl

    // Assert success callback was called
    assertTrue(wasSuccessCalled)
  }

  @Test
  fun clearConversation_callsCorrectReferenceAndHandlesSuccess() {

    val mockDatabaseReference = mock(DatabaseReference::class.java)
    val mockRemoveTask = mock(Task::class.java) as Task<Void>

    val isIaConversation = false
    val chatRoomId = "testChatRoomId"

    `when`(mockFirebaseDatabase.getReference(any())).thenReturn(mockDatabaseReference)
    `when`(mockDatabaseReference.child(any())).thenReturn(mockDatabaseReference)
    // `when`(mockDatabaseReference.child("chats")).thenReturn(mockDatabaseReference)
    `when`(mockDatabaseReference.removeValue()).thenReturn(mockRemoveTask)

    `when`(mockRemoveTask.addOnSuccessListener(any())).thenAnswer { invocation ->
      val onSuccessListener = invocation.arguments[0] as OnSuccessListener<Void>
      onSuccessListener.onSuccess(null)
      mockRemoveTask
    }

    var successCalled = false
    var failureCalled = false

    chatRepository.clearConversation(
        isIaConversation = isIaConversation,
        chatRoomId = chatRoomId,
        onSuccess = { successCalled = true },
        onFailure = { failureCalled = true })

    assertTrue(successCalled)
    assertFalse(failureCalled)
  }

  @Test
  fun `linkChatToRequest should link chat room to service request`() {
    val chatRoomId = "testChatRoomId"
    val serviceRequestId = "testServiceRequestId"

    `when`(mockFirebaseFirestore.collection("chatRooms")).thenReturn(mockCollectionRef)
    `when`(mockCollectionRef.document(chatRoomId)).thenReturn(mockDocumentRef)
    `when`(mockDocumentRef.set(eq(mapOf("serviceRequestId" to serviceRequestId)), any()))
        .thenReturn(mockTaskVoid)
    `when`(mockTaskVoid.addOnSuccessListener(any())).thenReturn(mockTaskVoid)
    `when`(mockTaskVoid.addOnFailureListener(any())).thenReturn(mockTaskVoid)

    chatRepository.linkChatToRequest(chatRoomId, serviceRequestId, {}, {})

    verify(mockDocumentRef).set(eq(mapOf("serviceRequestId" to serviceRequestId)), any())
  }

  @Test
  fun `getChatRequest should return serviceRequestId on success`() {
    val chatRoomId = "testChatRoomId"
    val serviceRequestId = "testServiceRequestId"

    `when`(mockDocumentSnapshot.getString("serviceRequestId")).thenReturn(serviceRequestId)
    `when`(mockFirebaseFirestore.collection("chatRooms")).thenReturn(mockCollectionRef)
    `when`(mockCollectionRef.document(chatRoomId)).thenReturn(mockDocumentRef)
    `when`(mockTaskDoc.isSuccessful).thenReturn(true)
    `when`(mockTaskDoc.result).thenReturn(mockDocumentSnapshot)
    `when`(mockDocumentRef.get()).thenReturn(mockTaskDoc)
    `when`(mockTaskDoc.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<DocumentSnapshot>
      listener.onSuccess(mockDocumentSnapshot)
      mockTaskDoc
    }
    `when`(mockTaskDoc.addOnFailureListener(any())).thenReturn(mockTaskDoc)

    var result: String? = null
    chatRepository.getChatRequest(
        chatRoomId, { result = it }, { fail("onFailure should not be called") })

    assertNotNull(result)
    assertEquals(serviceRequestId, result)
  }

  @Test
  fun `getChatRequest should call onFailure when serviceRequestId is null`() {
    val chatRoomId = "testChatRoomId"

    `when`(mockDocumentSnapshot.getString("serviceRequestId")).thenReturn(null)
    `when`(mockFirebaseFirestore.collection("chatRooms")).thenReturn(mockCollectionRef)
    `when`(mockCollectionRef.document(chatRoomId)).thenReturn(mockDocumentRef)
    `when`(mockTaskDoc.isSuccessful).thenReturn(true)
    `when`(mockTaskDoc.result).thenReturn(mockDocumentSnapshot)
    `when`(mockDocumentRef.get()).thenReturn(mockTaskDoc)
    `when`(mockTaskDoc.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<DocumentSnapshot>
      listener.onSuccess(mockDocumentSnapshot)
      mockTaskDoc
    }
    `when`(mockTaskDoc.addOnFailureListener(any())).thenReturn(mockTaskDoc)

    var result: String? = null
    var failureCalled = false
    chatRepository.getChatRequest(chatRoomId, { result = it }, { failureCalled = true })

    assertNull(result)
    assert(failureCalled)
  }

  @Test
  fun `getChatRequest should call onFailure on error`() {
    val chatRoomId = "testChatRoomId"

    `when`(mockFirebaseFirestore.collection("chatRooms")).thenReturn(mockCollectionRef)
    `when`(mockCollectionRef.document(chatRoomId)).thenReturn(mockDocumentRef)
    `when`(mockTaskDoc.isSuccessful).thenReturn(false)
    `when`(mockDocumentRef.get()).thenReturn(mockTaskDoc)
    `when`(mockTaskDoc.addOnSuccessListener(any())).thenReturn(mockTaskDoc)
    `when`(mockTaskDoc.addOnFailureListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnFailureListener
      listener.onFailure(Exception())
      mockTaskDoc
    }

    var result: String? = null
    var failureCalled = false
    chatRepository.getChatRequest(chatRoomId, { result = it }, { failureCalled = true })

    assertNull(result)
    assert(failureCalled)
  }

  @Test
  fun `seekerShouldCreateRequest successfully sets the flag`() {
    val chatRoomId = "testChatRoomId"
    val shouldCreateRequest = true

    `when`(mockFirebaseFirestore.collection("chatRooms")).thenReturn(mockCollectionRef)
    `when`(mockCollectionRef.document(chatRoomId)).thenReturn(mockDocumentRef)
    `when`(
            mockDocumentRef.set(
                eq(mapOf("shouldCreateRequest" to shouldCreateRequest)), eq(SetOptions.merge())))
        .thenReturn(mockTaskVoid)
    `when`(mockTaskVoid.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<Void>
      listener.onSuccess(null)
      mockTaskVoid
    }

    var successCalled = false
    var failureCalled = false

    chatRepository.seekerShouldCreateRequest(
        chatRoomId,
        shouldCreateRequest,
        onSuccess = { successCalled = true },
        onFailure = { failureCalled = true })

    verify(mockDocumentRef)
        .set(eq(mapOf("shouldCreateRequest" to shouldCreateRequest)), eq(SetOptions.merge()))
    assertTrue(successCalled)
    assertFalse(failureCalled)
  }

  @Test
  fun `getShouldCreateRequest successfully retrieves the flag`() {
    val chatRoomId = "testChatRoomId"
    val shouldCreateRequest = true

    `when`(mockFirebaseFirestore.collection("chatRooms")).thenReturn(mockCollectionRef)
    `when`(mockCollectionRef.document(chatRoomId)).thenReturn(mockDocumentRef)
    `when`(mockTaskDoc.isSuccessful).thenReturn(true)
    `when`(mockTaskDoc.result).thenReturn(mockDocumentSnapshot)
    `when`(mockDocumentSnapshot.get("shouldCreateRequest")).thenReturn(shouldCreateRequest)
    `when`(mockDocumentRef.get()).thenReturn(mockTaskDoc)
    `when`(mockTaskDoc.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<DocumentSnapshot>
      listener.onSuccess(mockDocumentSnapshot)
      mockTaskDoc
    }

    var result: Boolean? = null
    chatRepository.getShouldCreateRequest(
        chatRoomId,
        onSuccess = { result = it },
        onFailure = { fail("onFailure should not be called") })

    assertNotNull(result)
    assertEquals(shouldCreateRequest, result)
  }

  @Test
  fun `getShouldCreateRequest fails when flag is not set`() {
    val chatRoomId = "testChatRoomId"

    `when`(mockFirebaseFirestore.collection("chatRooms")).thenReturn(mockCollectionRef)
    `when`(mockCollectionRef.document(chatRoomId)).thenReturn(mockDocumentRef)
    `when`(mockTaskDoc.isSuccessful).thenReturn(true)
    `when`(mockTaskDoc.result).thenReturn(mockDocumentSnapshot)
    `when`(mockDocumentSnapshot.get("shouldCreateRequest")).thenReturn(null)
    `when`(mockDocumentRef.get()).thenReturn(mockTaskDoc)
    `when`(mockTaskDoc.addOnSuccessListener(any())).thenAnswer {
      val listener = it.arguments[0] as OnSuccessListener<DocumentSnapshot>
      listener.onSuccess(mockDocumentSnapshot)
      mockTaskDoc
    }

    var successCalled = false
    var failureCalled = false

    chatRepository.getShouldCreateRequest(
        chatRoomId, onSuccess = { successCalled = true }, onFailure = { failureCalled = true })

    assertFalse(successCalled)
    assertTrue(failureCalled)
  }
}
