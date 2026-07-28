package com.aman.smartplantdoctor

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChatActivity : AppCompatActivity() {
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        val recyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
        chatAdapter = ChatAdapter(messages)
        recyclerView.adapter = chatAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val input = findViewById<EditText>(R.id.chatInput)
        findViewById<ImageButton>(R.id.sendButton).setOnClickListener {
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) {
                addMessage(ChatMessage(text, true))
                input.text.clear()
                generateResponse(text)
            }
        }

        // Welcome message
        addMessage(ChatMessage("Hello! I'm your AI Plant Assistant. How can I help you today?", false))
    }

    private fun addMessage(message: ChatMessage) {
        messages.add(message)
        chatAdapter.notifyItemInserted(messages.size - 1)
        findViewById<RecyclerView>(R.id.chatRecyclerView).scrollToPosition(messages.size - 1)
    }

    private fun generateResponse(userText: String) {
        val response = when {
            userText.contains("water", ignoreCase = true) -> "Most plants need watering when the top inch of soil feels dry."
            userText.contains("sun", ignoreCase = true) -> "Check your plant's tag! Some love full sun, while others prefer shade."
            userText.contains("yellow", ignoreCase = true) -> "Yellow leaves can mean overwatering or lack of nutrients."
            else -> "That's interesting! Tell me more about your plant so I can help better."
        }
        
        findViewById<RecyclerView>(R.id.chatRecyclerView).postDelayed({
            addMessage(ChatMessage(response, false))
        }, 1000)
    }
}

data class ChatMessage(val text: String, val isUser: Boolean)
