package com.android.solvit.shared.model.chat

import android.net.Uri
import android.util.Log
import com.android.solvit.shared.model.utils.uploadImageToStorage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage

class ChatRepositoryFirestore(
    private val db: FirebaseDatabase,
    private val storage: FirebaseStorage,
    private val firestore: FirebaseFirestore
) : ChatRepository {

  // Name of collection containing messages
  private val collectionPath = "messages"
  private val collectionPathIA = "iamessages"
  private val chatImagesPath = "chatImages/"

  // Create a chat room in the collection or retrive if a conversation already exits between user
  // sender and receiver
  override fun initChat(
      isIaMessage: Boolean,
      currentUserUid: String?,
      onSuccess: (String) -> Unit,
      onFailure: () -> Unit,
      receiverUid: String
  ) {
    val collection = if (isIaMessage) collectionPathIA else collectionPath
    // Check that sender (current authenticated user is not null)
    if (currentUserUid != null) {
      val databaseRef = db.getReference(collection)
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
                        chatUsers.containsKey(currentUserUid)) {
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
                          (currentUserUid) to true,
                          receiverUid to true,
                      )
                  databaseRef.child(chatId ?: "").child("users").setValue(chatData)
                  if (chatId != null) {
                    onSuccess(chatId)
                  } else {
                    onFailure()
                  }
                }
              }

              override fun onCancelled(p0: DatabaseError) {
                onFailure()
                TODO("Not yet implemented")
              }
            })
      } catch (e: Exception) {
        onFailure()
        Log.e("Init Chat Failed", "$e")
      }
    } else {
      onFailure()
    }
  }

  override fun linkChatToRequest(
      chatRoomId: String,
      serviceRequestId: String,
      onSuccess: () -> Unit,
      onFailure: () -> Unit
  ) {
    firestore
        .collection("chatRooms")
        .document(chatRoomId)
        .set(mapOf("serviceRequestId" to serviceRequestId), SetOptions.merge())
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { onFailure() }
  }

  override fun getChatRequest(
      chatRoomId: String,
      onSuccess: (String) -> Unit,
      onFailure: () -> Unit
  ) {
    firestore
        .collection("chatRooms")
        .document(chatRoomId)
        .get()
        .addOnSuccessListener { document ->
          val serviceRequestId = document.getString("serviceRequestId")
          if (serviceRequestId != null) {
            onSuccess(serviceRequestId)
          } else {
            onFailure()
          }
        }
        .addOnFailureListener { onFailure() }
  }

  // Send Message in a chatRoom than links 2 users
  override fun sendMessage(
      isIaMessage: Boolean,
      chatRoomId: String,
      message: ChatMessage,
      onSuccess: () -> Unit,
      onFailure: () -> Unit
  ) {

    val collection = if (isIaMessage) collectionPathIA else collectionPath
    Log.e("sendMessage", "Entered there $collection")
    val chatNode = db.getReference(collection)
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
      isIaConversation: Boolean,
      chatRoomId: String,
      onSuccess: (List<ChatMessage>) -> Unit,
      onFailure: () -> Unit
  ) {

    val collection = if (isIaConversation) collectionPathIA else collectionPath
    val chatNode = db.getReference(collection)
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
                      getMessageFromFirebase(messageSnap)
                    }
                onSuccess(messages)
              }

              override fun onCancelled(error: DatabaseError) {}
            })
  }

  override fun listenForLastMessages(
      currentUserUid: String?,
      onSuccess: (Map<String?, ChatMessage>) -> Unit,
      onFailure: () -> Unit
  ) {
    val chatNode = db.getReference(collectionPath)
    val lastMessages = mutableMapOf<String?, ChatMessage>()

    chatNode
        .orderByChild("users/${currentUserUid}")
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
                        usersSnapshots.children.mapNotNull { it.key }.find { it != currentUserUid }

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

  override fun clearConversation(
      isIaConversation: Boolean,
      chatRoomId: String,
      onSuccess: () -> Unit,
      onFailure: () -> Unit
  ) {
    val collection = if (isIaConversation) collectionPathIA else collectionPath
    val chatRef = db.getReference(collection).child(chatRoomId).child("chats")
    chatRef.removeValue().addOnSuccessListener { onSuccess() }.addOnFailureListener { onFailure() }
  }

  override fun uploadChatImagesToStorage(
      imageUri: Uri,
      onSuccess: (String) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    uploadImageToStorage(storage, chatImagesPath, imageUri, onSuccess, onFailure)
  }

  fun getLastMessageForChatRoom(chatRoomId: String, onComplete: (ChatMessage?) -> Unit) {
    val chatRef = db.getReference(collectionPath).child(chatRoomId).child("chats")

    // Query Messages ordered by timestamp and get last message
    chatRef
        .orderByChild("timestamp")
        .limitToLast(1)
        .addListenerForSingleValueEvent(
            object : ValueEventListener {
              override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                  val firstChild = snapshot.children.first()
                  val lastMessage = getMessageFromFirebase(firstChild)
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

  /** given Type Field retrieve the correct ChatMessage format */
  fun getMessageFromFirebase(firstChild: DataSnapshot): ChatMessage? {
    return when (val type = firstChild.child("type").value as? String) {
      "text" -> firstChild.getValue(ChatMessage.TextMessage::class.java)
      "image" -> firstChild.getValue(ChatMessage.ImageMessage::class.java)
      "textAndImage" -> firstChild.getValue(ChatMessage.TextImageMessage::class.java)
      else -> {
        Log.e("getMessage", "Unknown message type: $type")
        null
      }
    }
  }
}
