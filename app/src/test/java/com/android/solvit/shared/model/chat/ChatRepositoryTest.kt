package com.android.solvit.shared.model.chat

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify

class ChatRepositoryTest {
  private lateinit var mockFirebaseDatabase: FirebaseDatabase
  private lateinit var mockAuth: FirebaseAuth
  private lateinit var chatRepository: ChatRepository
  private lateinit var mockDatabaseReference: DatabaseReference
  private lateinit var mockDataSnapshot: DataSnapshot

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    mockFirebaseDatabase = mock(FirebaseDatabase::class.java)
    mockDatabaseReference = mock(DatabaseReference::class.java)
    mockAuth = mock(FirebaseAuth::class.java)
    mockDataSnapshot = mock(DataSnapshot::class.java)
    chatRepository = ChatRepositoryFirestore(mockAuth, mockFirebaseDatabase)

    `when`(mockFirebaseDatabase.getReference(any())).thenReturn(mockDatabaseReference)
  }

  @Test
  fun `initChat Test with existing ChatRoom`() {
    val receiverUid = "receiverUid"
    val chatId = "existingChatId"

    val mockChildSnapshot = mock(DataSnapshot::class.java)
    val mockUserSnapshot = mock(DataSnapshot::class.java)

    val mockCurrentUser = mock(FirebaseUser::class.java)
    `when`(mockAuth.currentUser).thenReturn(mockCurrentUser)
    `when`(mockCurrentUser.uid).thenReturn("currentUserUid")

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

    // Capture the AuthStateListener
    val authStateListenerCaptor = argumentCaptor<FirebaseAuth.AuthStateListener>()
    doNothing().`when`(mockAuth).addAuthStateListener(authStateListenerCaptor.capture())

    // Set up DataSnapshot to return children
    `when`(mockDataSnapshot.hasChildren()).thenReturn(true)
    `when`(mockDataSnapshot.children).thenReturn(listOf(mockChildSnapshot).asIterable())

    // Capture the ValueEventListener
    val valueEventListenerCaptor = argumentCaptor<ValueEventListener>()
    doNothing()
        .`when`(mockDatabaseReference)
        .addListenerForSingleValueEvent(valueEventListenerCaptor.capture())

    var resultChatId: String? = null

    chatRepository.initChat(onSuccess = { chatid -> resultChatId = chatid }, receiverUid)

    verify(mockAuth).addAuthStateListener(authStateListenerCaptor.capture())
    authStateListenerCaptor.firstValue.onAuthStateChanged(mockAuth)
    verify(mockDatabaseReference).addListenerForSingleValueEvent(valueEventListenerCaptor.capture())
    valueEventListenerCaptor.firstValue.onDataChange(mockDataSnapshot)

    // Check that the callback returned the expected chatId
    assertEquals(chatId, resultChatId)
  }

  @Test
  fun `initChat Test with new ChatRoom`() {
    val receiverUid = "receiverUid"
    val chatId = "newChatId"

    val mockCurrentUser = mock(FirebaseUser::class.java)
    `when`(mockAuth.currentUser).thenReturn(mockCurrentUser)
    `when`(mockCurrentUser.uid).thenReturn("currentUserUid")

    val mockPushReference = mock(DatabaseReference::class.java)
    `when`(mockDatabaseReference.child(any())).thenReturn(mockPushReference)
    `when`(mockDatabaseReference.push()).thenReturn(mockPushReference)
    `when`(mockPushReference.key).thenReturn("newChatId")

    val mockUsersReference = mock(DatabaseReference::class.java)
    `when`(mockPushReference.child("users")).thenReturn(mockUsersReference)
    val mockSetValueTask = mock(Task::class.java) as Task<Void>
    `when`(mockUsersReference.setValue(any())).thenReturn(mockSetValueTask)

    // Capture the AuthStateListener
    val authStateListenerCaptor = argumentCaptor<FirebaseAuth.AuthStateListener>()
    doNothing().`when`(mockAuth).addAuthStateListener(authStateListenerCaptor.capture())

    // Make sure that data Snapshot has no children
    `when`(mockDataSnapshot.hasChildren()).thenReturn(false)

    // Capture the ValueEventListener
    val valueEventListenerCaptor = argumentCaptor<ValueEventListener>()
    doNothing()
        .`when`(mockDatabaseReference)
        .addListenerForSingleValueEvent(valueEventListenerCaptor.capture())

    var resultChatId: String? = null

    chatRepository.initChat(onSuccess = { chatid -> resultChatId = chatid }, receiverUid)

    verify(mockAuth).addAuthStateListener(authStateListenerCaptor.capture())
    authStateListenerCaptor.firstValue.onAuthStateChanged(mockAuth)
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
            "Test message", "senderName", "senderId", System.currentTimeMillis())

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
        ChatMessage.TextMessage("Message 1", "senderName", "Hello", System.currentTimeMillis())
    val message2 =
        ChatMessage.TextMessage(
            "Message 2", "senderName", "How Are you", System.currentTimeMillis())

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
        chatRoomId, onSuccess = { messages -> receivedMessages = messages }, onFailure = {})

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
    val mockCurrentUser = mock(FirebaseUser::class.java)
    `when`(mockAuth.currentUser).thenReturn(mockCurrentUser)
    `when`(mockCurrentUser.uid).thenReturn(currentUserUid)

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
    var receivedMessages: List<ChatMessage.TextMessage>? = null
    var onFailureCalled = false

    // Call the method under test
    chatRepository.listenForLastMessages(
        onSuccess = { messages -> receivedMessages = messages },
        onFailure = { onFailureCalled = true })

    // Simulate the onDataChange for the chat rooms query
    verify(mockQuery).addListenerForSingleValueEvent(valueEventListenerCaptor.capture())
    valueEventListenerCaptor.firstValue.onDataChange(mockChatRoomsDataSnapshot)

    // Simulate the last messages for each chat room
    val lastMessage1 =
        ChatMessage.TextMessage("Message from chatRoomId1", "senderName", "senderId1", 2000L)
    val lastMessage2 =
        ChatMessage.TextMessage("Message from chatRoomId2", "senderName", "senderId2", 3000L)

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

    // Simulate the onDataChange for the chats queries
    chatsValueEventListenerCaptor1.firstValue.onDataChange(mockLastMessageSnapshot1)
    chatsValueEventListenerCaptor2.firstValue.onDataChange(mockLastMessageSnapshot2)

    // Verify that the received messages are as expected
    val expectedMessages = listOf(lastMessage2, lastMessage1) // Sorted by timestamp descending
    assertEquals(expectedMessages, receivedMessages)
    assertEquals(false, onFailureCalled)
  }
}
