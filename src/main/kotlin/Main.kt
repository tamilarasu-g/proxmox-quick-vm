import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import io.ktor.serialization.kotlinx.json.*
import javax.net.ssl.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.Serializable
import org.dotenv.vault.dotenvVault

val dotenv = dotenvVault()
val auth_header = dotenv["API_TOKEN"]
val proxmox_api = dotenv["API_HOST"]


suspend fun main() {
    // Create a TrustManager that disables SSL verification
    val trustAllCerts = arrayOf<TrustManager>(
        object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
        }
    )

    // Create an SSLContext with the TrustManager
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, trustAllCerts, java.security.SecureRandom())

    // Configure the Ktor HTTP client
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { prettyPrint = true; ignoreUnknownKeys = true; isLenient = true })
        }
        engine {
            https {
                sslContext
                trustManager = trustAllCerts[0] as X509TrustManager
            }
        }
    }


//    try {
//        val response: HttpResponse = client.get("${proxmox_api}/nodes/pve/qemu") {
//            header("Authorization", auth_header)
//        }
//        println(response.bodyAsText())
//    } catch (e: Exception) {
//        println("Error: ${e.message}")
//    } finally {
//        client.close()
//    }

//    @Serializable
//    data class Mydata(val iface: String, val node: String, val type: String)
//
//    val new_data = Mydata(iface="anothernet", node = "pve", type = "bridge" )
//    val jsonData = Json.encodeToString(new_data)
//    println("Sending JSON: $jsonData")
//
//
//    try {
//        val response: HttpResponse = client.post("${proxmox_api}/nodes/pve/network") {
//            header("Authorization", auth_header)
//            contentType(ContentType.Application.Json)
//            setBody(new_data)
//        }
//        println(response.status)
//    } catch (e: Exception) {
//        println("Error: ${e.message}")
//    } finally {
//        client.close()
//    }


    @Serializable
    data class create_vm_params(
        val vmid: Int,
        val node: String,
        val cores: Int,
        val name: String,
        val memory: String,
        val net0: String
    )

    val new_data = create_vm_params(vmid = 110, node = "pve", cores = 2, name = "test2-vm", memory = "current=1024", net0 = "virtio,bridge=vmbr0")
    val jsonData = Json.encodeToString(new_data)
    println("Sending JSON: $jsonData")


    try {
        val response: HttpResponse = client.post("${proxmox_api}/nodes/pve/qemu") {
            header("Authorization", auth_header)
            contentType(ContentType.Application.Json)
            setBody(new_data)
        }
        println(response.status)
    } catch (e: Exception) {
        println("Error: ${e.message}")
    } finally {
        client.close()
    }



}
