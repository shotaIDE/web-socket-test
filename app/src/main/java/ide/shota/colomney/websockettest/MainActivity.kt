package ide.shota.colomney.websockettest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ide.shota.colomney.websockettest.ui.theme.WebSocketTestTheme
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class MyWebSocketClient :
    WebSocketClient(URI("wss://demo.piesocket.com/v3/channel_123?api_key=VCXCEuvhGcBDP7XhiJJUDvR1e1D3eiVjgZ9VRiaV&notify_self")) {
    init {
        this.setProxy(
            Proxy(
                Proxy.Type.SOCKS,
                InetSocketAddress("192.168.11.13", 8889)
            )
        )

        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate>? = null
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) =
                Unit

            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) =
                Unit
        })
        val socketFactory = SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, java.security.SecureRandom())
        }.socketFactory
        this.setSocketFactory(socketFactory)
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        println("opened connection")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        println("closed connection")
    }

    override fun onMessage(message: String?) {
        println("received: $message")
    }

    override fun onError(ex: Exception?) {
        println("error: ${ex?.message}")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WebSocketTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        Greeting(
                            name = "Android",
                            modifier = Modifier.padding(innerPadding)
                        )
                        Spacer(modifier = Modifier.padding(16.dp))
                        Button(onClick = {
                            val webSocketClient = MyWebSocketClient()
                            webSocketClient.connect()
                        }) {
                            Text("Connect")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WebSocketTestTheme {
        Greeting("Android")
    }
}