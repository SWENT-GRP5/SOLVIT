package com.android.solvit.shared.model.chat

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue

class ChatRepositoryFirestore(private val auth: FirebaseAuth, private val db: FirebaseDatabase) :
    ChatRepository {

  // Name of collection containing messages
  private val collectionPath = "messages"

  // Create a chat room in the collection or retrive if a conversation already exits between user
  // sender and receiver
  override fun initChat(onSuccess: (String) -> Unit, receiverUid: String) {
    // Check that sender (current authenticated user is not null)
    auth.addAuthStateListener {
      if (it.currentUser != null) {
        val databaseRef = db.getReference(collectionPath)
        Log.e("InitChat", "$databaseRef")
        try {
          databaseRef.addListenerForSingleValueEvent(
              object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                  Log.e("initChat", "entered there")
                  var chatId: String? = null
                  // p0 represent all chat Rooms
                  if (p0.hasChildren()) {
                    // Chat Snapshot represents a specific chatRoom
                    for (chatSnapshot in p0.children) {
                      // Each Chat Room is linked to 2 users
                      val chatUsers = chatSnapshot.child("users").value as? Map<*, *>
                      // Check if there is an already existing chat room between 2 users
                      if (chatUsers?.containsKey(receiverUid) == true &&
                          chatUsers.containsKey(auth.currentUser?.uid)) {
                        Log.e("initChat", "soy Aqui")
                        chatId = chatSnapshot.key
                        if (chatId != null) {
                          onSuccess(chatId)
                        }
                        break
                      }
                    }
                  }
                  // In case it doesn't exist, we create a new chatRoom
                  if (chatId == null) {
                    chatId = databaseRef.push().key
                    val chatData =
                        mapOf(
                            (auth.currentUser?.uid ?: "") to true,
                            receiverUid to true,
                        )
                    databaseRef.child(chatId ?: "").child("users").setValue(chatData)
                    if (chatId != null) {
                      onSuccess(chatId)
                    }
                  }
                }

                override fun onCancelled(p0: DatabaseError) {
                  TODO("Not yet implemented")
                }
              })
        } catch (e: Exception) {
          Log.e("Init Chat Failed", "$e")
        }
      }
    }
  }

  // Send Message in a chatRoom than links 2 users
  override fun sendMessage(
      chatRoomId: String,
      message: ChatMessage.TextMessage,
      onSuccess: () -> Unit,
      onFailure: () -> Unit
  ) {

    Log.e("sendMessage", "Entered there")
    val chatNode = db.getReference(collectionPath)
    val chatMessageId = chatNode.child(chatRoomId).child("chats").push().key
    try {
      if (chatMessageId != null) {
        Log.e("sendMessage", "$chatMessageId : $chatRoomId")
        chatNode.child(chatRoomId).child("chats").child(chatMessageId).setValue(message)
        onSuccess()
      } else {
        Log.e("Send Message", "chatMessageId is null")
      }
    } catch (e: Exception) {
      Log.e("Send Message", "$e")
    }
  }

  // Get All Messages of a chatRoom between 2 users
  override fun listenForMessages(
      chatRoomId: String,
      onSuccess: (List<ChatMessage.TextMessage>) -> Unit,
      onFailure: () -> Unit
  ) {

    val chatNode = db.getReference(collectionPath)
    // Listener to trigger new list of message when changes occur
    chatNode
        .child(chatRoomId)
        .child("chats")
        .orderByChild("timestamp")
        .addValueEventListener(
            object : ValueEventListener {
              override fun onDataChange(snapshot: DataSnapshot) {
                val messages =
                    snapshot.children.mapNotNull { messageSnap ->
                      messageSnap.getValue(ChatMessage.TextMessage::class.java)
                    }
                onSuccess(messages)
              }

              override fun onCancelled(error: DatabaseError) {}
            })
  }

  override fun listenForLastMessages(
      onSuccess: (Map<String?, ChatMessage.TextMessage>) -> Unit,
      onFailure: () -> Unit
  ) {
    val chatNode = db.getReference(collectionPath)
    val lastMessages = mutableMapOf<String?, ChatMessage.TextMessage>()

    chatNode
        .orderByChild("users/${auth.currentUser?.uid}")
        .equalTo(true)
        .addListenerForSingleValueEvent(
            object : ValueEventListener {
              override fun onDataChange(p0: DataSnapshot) {

                // Check that there is indeed chatRooms
                if (p0.hasChildren()) {
                  var processedCount = 0
                  for (chatRoomsSnapshot in p0.children) {
                    val chatRoomId = chatRoomsSnapshot.key ?: continue
                    // Retrieve the receiver Uid
                    val usersSnapshots = chatRoomsSnapshot.child("users")
                    val receiverUid =
                        usersSnapshots.children
                            .mapNotNull { it.key }
                            .find { it != auth.currentUser?.uid }

                    // Retrieve the last Message in the Chat Room
                    getLastMessageForChatRoom(
                        chatRoomId,
                        onComplete = { chatMessage ->
                          Log.e("add chatMessage to Last Message", "$chatMessage")
                          if (chatMessage != null) {
                            lastMessages[receiverUid] = chatMessage
                          }
                          processedCount++
                          // If we get All last messages
                          if (processedCount == p0.children.count()) {
                            onSuccess(lastMessages)
                          }
                          Log.e("lastMessages.add", "${lastMessages.toList()}")
                        })
                  }
                } else {
                  Log.e("listenForLastMessages", "chatNode sorted with userId don't have children")
                }
              }

              override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
              }
            })
  }

  fun getLastMessageForChatRoom(
      chatRoomId: String,
      onComplete: (ChatMessage.TextMessage?) -> Unit
  ) {
    val chatRef = db.getReference(collectionPath).child(chatRoomId).child("chats")

    // Query Messages ordered by timestamp and get last message
    chatRef
        .orderByChild("timestamp")
        .limitToLast(1)
        .addListenerForSingleValueEvent(
            object : ValueEventListener {
              override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                  val lastMessage =
                      snapshot.children.first().getValue(ChatMessage.TextMessage::class.java)
                  Log.e("getLastMessage", "$lastMessage")
                  onComplete(lastMessage)
                } else {
                  Log.e("getLastMessageForChatRoom", "snapshot doesn't exist")
                  onComplete(null)
                }
              }

              override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
              }
            })
  }
}
