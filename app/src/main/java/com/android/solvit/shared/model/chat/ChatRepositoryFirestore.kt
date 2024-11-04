package com.android.solvit.shared.model.chat

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatRepositoryFirestore(private val db: FirebaseDatabase) : ChatRepository {

  private val collectionPath = "messages"

  override fun initChat(onSuccess: (String) -> Unit, receiverUid: String) {
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        val databaseRef = db.getReference(collectionPath)
        val chatQuery =
            databaseRef.orderByChild("users/${Firebase.auth.currentUser?.uid}").equalTo(true)
        try {
          chatQuery.get().addOnSuccessListener { snapshot ->
            var chatId: String? = null
            for (chatSnapshot in snapshot.children) {
              val chatUsers = chatSnapshot.child("users").value as? Map<*, *>
              if (chatUsers?.containsKey(receiverUid) == true) {
                chatId = chatSnapshot.key
                break
              }
            }

            if (chatId == null) {
              chatId = databaseRef.push().key
              val chatData =
                  mapOf(
                      "${Firebase.auth.currentUser?.uid}" to true,
                      receiverUid to true,
                  )
              databaseRef.child(chatId ?: "").child("users").setValue(chatData)
            }

            // Retrieve current chatId
            if (chatId != null) {
              onSuccess(chatId)
            }
          }
        } catch (e: Exception) {
          Log.e("Init Chat Failed", "$e")
        }
      }
    }
  }

  override fun sendMessage(
      chatRoomId: String,
      message: ChatMessage,
      onSuccess: () -> Unit,
      onFailure: () -> Unit
  ) {
    val chatNode = db.getReference(collectionPath)
    val chatMessageId = chatNode.child(chatRoomId).child("messages").push().key
    try {
      if (chatMessageId != null) {
        chatNode.child(chatRoomId).child("messages").child(chatMessageId).setValue(message)
        onSuccess()
      } else {
        Log.e("Send Message", "chatMessageId is null")
      }
    } catch (e: Exception) {
      Log.e("Send Message", "$e")
    }
  }

  override fun listenForMessages(
      chatRoomId: String,
      onSuccess: (List<ChatMessage>) -> Unit,
      onFailure: () -> Unit
  ) {
    val chatNode = db.getReference(collectionPath)
    // Listener to trigger new list of message when changes occur
    chatNode
        .child(chatRoomId)
        .child("messages")
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

  override fun listenForAllMessages(
      onSuccess: (List<ChatMessage>) -> Unit,
      onFailure: () -> Unit
  ) {}
}
