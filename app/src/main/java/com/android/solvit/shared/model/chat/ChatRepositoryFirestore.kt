package com.android.solvit.shared.model.chat

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
        try {
          databaseRef.addListenerForSingleValueEvent(
              object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
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
      message: ChatMessage,
      onSuccess: () -> Unit,
      onFailure: () -> Unit
  ) {

    val chatNode = db.getReference(collectionPath)
    val chatMessageId = chatNode.child(chatRoomId).child("chats").push().key
    try {
      if (chatMessageId != null) {
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
      onSuccess: (List<ChatMessage>) -> Unit,
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
                      messageSnap.getValue(ChatMessage::class.java)
                    }
                onSuccess(messages)
              }

              override fun onCancelled(error: DatabaseError) {}
            })
  }
}