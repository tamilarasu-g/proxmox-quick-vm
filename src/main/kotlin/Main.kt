import com.proxmox.api.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import java.net.URLEncoder
import kotlin.system.exitProcess
import java.nio.charset.StandardCharsets

fun main() = runBlocking {

    // Start of the CLI

    val node = "pve" // Have to change this in future
    val localPath: String = "local"
    val localLvmPath: String = "local-lvm"
    val cdrom = "${localPath}:cloudinit"
    val ciuser = "eren"


    val printImageMap = mapOf(
        0 to "Arch",
        1 to "Ubuntu",
        2 to "Fedora",
        3 to "Debian",
        4 to "AlmaLinux",
        5 to "RockyLinux"
    )

    val ubuntuImageVersion = mapOf(
        0 to "24.10",
        1 to "24.04 LTS",
        2 to "24.04 LTS"
    )


    val debianImageVersion = mapOf(
        0 to "Bookworm 12",
        1 to "Bullseye 11",
        2 to "Buster 10",
    )

    // Mention all the files for the versions, also match it the debianImageVersion above
    val debianImageFile = mapOf(
        0 to "debian-cloud.qcow2",
        1 to "debian-11-genericcloud-amd64.qcow2",
        2 to "debian-10-genericcloud-amd64.qcow2"
    )

    val fedoraImageVersion = mapOf(
        0 to "41",
        1 to "40",
        2 to "39",
    )

    val imageMap = mapOf(
        1 to ubuntuImageVersion,
        3 to debianImageVersion,
        2 to fedoraImageVersion
    )

    val size1 = mapOf(
        "CPU" to 2,
        "Storage" to "50G",
        "Memory" to "2048"
    )

    val size2 = mapOf(
        "CPU" to 1,
        "Storage" to "25G",
        "Memory" to "1024"
    )

    val printSize = mapOf(
        0 to size1,
        1 to size2
    )

    println("Welcome to Quick-Proxmox-VM")

    println("Choose an image")
    printImageMap.forEach { entry ->
        print("${entry.key} : ${entry.value}")
        println()
    }
    print("Enter the corresponding number : ")
    val imageInput: Int = readln().toInt()
    // println("Entered value is $imageInput")

    println("The versions are : ")

    val selectedImage: Map<Int, String> = imageMap[imageInput] ?: emptyMap()
    // println(selectedImage)

    println("Choose the version : ")
    selectedImage.forEach{ entry ->
        print("${entry.key} : ${entry.value}")
        println()
    }
    print("Enter the corresponding number : ")

    val selectedImageInput = readln().toInt()
    val selectedImageVersion: String = debianImageFile[selectedImageInput]!!

    // println("Selected version is $selectedImageVersion")

    println("Choose the size of the vm")
    print("0 -> ")
    size1.forEach{ size ->
        print("${size.key} : ${size.value}   ")
    }
    println()
    print("1 -> ")
    size2.forEach{ size ->
        print("${size.key} : ${size.value}   ")
    }
    println()
    print("Enter the corresponding number : ")
    val inputSize: Int = readln().toInt()
    val selectedSize = printSize[inputSize] ?: emptyMap()

    val cores = selectedSize["CPU"].toString().toInt()
    val memory = selectedSize["Memory"].toString()
    val storage = selectedSize["Storage"].toString()
    println()

    print("Enter the SSH Key : ")
    val sshKey: String = URLEncoder.encode(readln(), StandardCharsets.UTF_8.toString()).replace("+", "%20")

    println()

    print("Enter the Name for the VM : ")
    val name: String = readln()

    println()

    print("Enter the VMID : ")
    val vmid: Int = readln().toInt()

    println()
    print("Enter the IP Address for the VM - Example -> 10.2.0.20/24 : ")
    val ipAddress: String = readln()

    println()
    print("Enter the gateway : ")
    val gateway: String = readln()

    suspend fun createVmFunc() {
        val result = createVm(
            vmid,
            node,
            cores,
            name,
            memory,
            net0 = "virtio,bridge=vmbr0")
        result.fold(
            onSuccess = { response ->
                println("Success : ${response.status}")
                println("Data is ${response.bodyAsText()}")
            },
            onFailure = { exception ->
                println("Error : ${exception.message}")
                exitProcess(1)
            }
        )
    }

    suspend fun attachDiskFunc() {
        val result = attachDisk(
            vmid,
            qcow2File = selectedImageVersion,
            node,
            localPath,
            localLvmPath
        )
        result.fold(
            onSuccess = { response ->
                println("Success : ${response.status}")
                println("Data is ${response.bodyAsText()}")
            },
            onFailure = { exception ->
                println("Error : ${exception.message}")
            }
        )
    }

    suspend fun resizeDiskFunc() {
        val result = resizeDisk(
            disk = "scsi0",
            size = storage,
            node,
            vmid,
        )
        result.fold(
            onSuccess = { response ->
                println("Success : ${response.status}")
                println("Data is ${response.bodyAsText()}")
            },
            onFailure = { exception ->
                println("Error : ${exception.message}")
            }
        )
    }

    suspend fun cloudInitFunc() {
        val result = addCloudInit(
            cdrom,
            ciuser = ciuser,
            ciupgrade = false,
            sshkeys = sshKey,
            ipconfig0 = "ip=${ipAddress},gw=${gateway}",
            node = node,
            vmid = vmid
        )
        result.fold(
            onSuccess = { response ->
                println("Success : ${response.status}")
                println("Data is ${response.bodyAsText()}")
            },
            onFailure = { exception ->
                println("Error : ${exception.message}")
            }
        )
    }

    suspend fun consoleAndBootOrderFunc() {
        val result = addSerialConsoleAndSetBootOrder(
            serial0 = "socket",
            vga = "serial0",
            boot = "order=scsi0",
            node,
            vmid
        )
    }

    // Invoke all the methods that are created
    createVmFunc()
    Thread.sleep(2000)
    attachDiskFunc()
    Thread.sleep(2000)
    resizeDiskFunc()
    Thread.sleep(2000)
    cloudInitFunc()
    Thread.sleep(2000)
    consoleAndBootOrderFunc()
}