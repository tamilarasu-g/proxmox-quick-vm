package com.proxmox.api

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import io.ktor.serialization.kotlinx.json.*
import javax.net.ssl.*
import kotlinx.serialization.Serializable
import org.dotenv.vault.dotenvVault

// Initialize dotenv
val dotenv = dotenvVault()
val authHeader = dotenv["API_TOKEN"]
val proxmoxApi = dotenv["API_HOST"]

// Create the ktor client with SSL Disabled
fun createKtorClient(): HttpClient {
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
    return client
}

// function to get all VMs
suspend fun getAllVm() {
    val client: HttpClient = createKtorClient()
    try {
        val response: HttpResponse = client.get("${proxmoxApi}/nodes/pve/qemu") {
            header("Authorization", authHeader)
        }
        println(response.bodyAsText())
    } catch (e: Exception) {
        println("Error: ${e.message}")
    } finally {
        client.close()
    }
}


// Function to create a VM
suspend fun createVm(vmid: Int, node: String, cores: Int, name: String, memory: String, net0: String, cpu: String): Result<HttpResponse> {
    val client: HttpClient = createKtorClient()

    @Serializable
    data class createVmParams(
        val vmid: Int,
        val node: String,
        val cores: Int,
        val name: String,
        val memory: String,
        val net0: String,
        val cpu: String
    )

    val createVmData = createVmParams(vmid, node, cores, name, memory, net0, cpu)

    try {
        val response: HttpResponse = client.post("${proxmoxApi}/nodes/${node}/qemu") {
            header("Authorization", authHeader)
            contentType(ContentType.Application.Json)
            setBody(createVmData)
        }
        return Result.success(response)
    } catch (e: Exception) {
        return Result.failure(e)
    } finally {
        client.close()
    }
}

suspend fun attachDisk(vmid: Int, qcow2File: String, node: String, localPath: String, localLvmPath: String ): Result<HttpResponse> {
    val client: HttpClient = createKtorClient()

    @Serializable
    data class attachDiskParams(
        val scsihw: String,
        val scsi0: String,
    )

    val attachDiskData = attachDiskParams(scsihw = "virtio-scsi-pci", scsi0 = "${localLvmPath}:0,import-from=${localPath}:import/${qcow2File}")

    try {
        val response: HttpResponse = client.post("${proxmoxApi}/nodes/${node}/qemu/${vmid}/config") {
            header("Authorization", authHeader)
            contentType(ContentType.Application.Json)
            setBody(attachDiskData)
        }
        return Result.success(response)
    } catch (e: Exception) {
        return Result.failure(e)
    } finally {
        client.close()
    }
}

suspend fun resizeDisk(disk: String, size: String, node: String, vmid: Int): Result<HttpResponse> {
    val client: HttpClient = createKtorClient()

    @Serializable
    data class resizeDiskParams(
        val disk: String,
        val size: String
    )

    val resizeDiskData = resizeDiskParams(disk, size)
    try {
        val response: HttpResponse = client.put("${proxmoxApi}/nodes/${node}/qemu/${vmid}/resize") {
            header("Authorization", authHeader)
            contentType(ContentType.Application.Json)
            setBody(resizeDiskData)
        }
        return Result.success(response)
    } catch (e: Exception) {
        return Result.failure(e)
    } finally {
        client.close()
    }

}


suspend fun addCloudInit(cdrom: String, ciupgrade: Boolean, ciuser: String, sshkeys: String, ipconfig0: String, node: String, vmid: Int): Result<HttpResponse> {
    val client: HttpClient = createKtorClient()

    @Serializable
    data class addCloudInitParams(
        val cdrom: String,
        val ciupgrade: Boolean,
        val ciuser: String,
        val sshkeys: String,
        val ipconfig0: String,
    )

    val addCloudInitData = addCloudInitParams(
        cdrom,
        ciupgrade,
        ciuser,
        sshkeys,
        ipconfig0
    )
    try {
        val response: HttpResponse = client.put("${proxmoxApi}/nodes/${node}/qemu/${vmid}/config") {
            header("Authorization", authHeader)
            contentType(ContentType.Application.Json)
            setBody(addCloudInitData)
        }
        return Result.success(response)
    } catch (e: Exception) {
        return Result.failure(e)
    } finally {
        client.close()
    }
}

suspend fun addSerialConsoleAndSetBootOrder(serial0: String, vga: String, boot:String, node: String, vmid: Int): Result<HttpResponse> {
    val client: HttpClient = createKtorClient()

    @Serializable
    data class consoleAndBootOrderParams(
        val serial0: String,
        val vga: String,
        val boot: String
    )

    val consoleAndBootOrderData = consoleAndBootOrderParams(
        serial0,
        vga,
        boot
    )

    try {
        val response: HttpResponse = client.put("${proxmoxApi}/nodes/${node}/qemu/${vmid}/config") {
            header("Authorization", authHeader)
            contentType(ContentType.Application.Json)
            setBody(consoleAndBootOrderData)
        }
        return Result.success(response)
    } catch (e: Exception) {
        return Result.failure(e)
    } finally {
        client.close()
    }


}