package ide.shota.colomney.websockettest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ide.shota.colomney.websockettest.ui.theme.WebSocketTestTheme
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.net.InetSocketAddress
import java.net.Proxy

class WebSocketClient : WebSocketListener() {
    private val webSocket: WebSocket

    init {
        val client = OkHttpClient.Builder()
            .proxy(
                Proxy(
                    Proxy.Type.HTTP,
                    InetSocketAddress("192.168.11.13", 8080)
                )
            )
            .build()
        val request = Request.Builder()
            .url("wss://echo.websocket.org")
            .build()
        webSocket = client.newWebSocket(request, this)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        println("WebSocket opened successfully")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        println("Received text message: $text")
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        println("Received binary message: ${bytes.hex()}")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
        println("Connection closed: $code $reason")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        println("Connection failed: ${t.localizedMessage}")
    }

    fun send(message: String) {
        webSocket.send(message)
    }

    fun close() {
        webSocket.close(1000, null)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WebSocketTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("WebSocket test")
                        Spacer(modifier = Modifier.padding(16.dp))
                        WebSocketPanel()
                    }
                }
            }
        }
    }
}

@Composable
fun WebSocketPanel() {
    var webSocketClient: WebSocketClient? by rememberSaveable { mutableStateOf(null) }
    var counter by rememberSaveable { mutableStateOf(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Button(onClick = {
            if (webSocketClient == null) {
                webSocketClient = WebSocketClient()
                return@Button
            }

            webSocketClient!!.close()
            webSocketClient = null
            counter = 0
        }) {
            Text(
                if (webSocketClient == null)
                    "Connect to WebSocket server"
                else
                    "Disconnect from WebSocket server"
            )
        }
        Spacer(modifier = Modifier.padding(16.dp))
        Button(onClick = {
            val client = webSocketClient ?: return@Button

            client.send("$counter")
            counter++
        }, enabled = webSocketClient != null) {
            Text("Send \"$counter\"")
        }
    }
}
