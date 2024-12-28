import com.proxmox.api.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import java.net.URLEncoder
import kotlin.system.exitProcess
import java.nio.charset.StandardCharsets

fun main() = runBlocking {

    // Start of the CLI

    data class imageVersion(
        val id: Int,
        val version: String,
        val file: String
    )

    val multiValueMap: Map<String, List<imageVersion>> = mapOf(
        "archImageVersionMap" to listOf(
            imageVersion(0,"v20241215.289320", "Arch-Linux-x86_64-cloudimg-20241215.289320.qcow2"),
            imageVersion(1, "v20241201.284788", "Arch-Linux-x86_64-cloudimg-20241201.284788.qcow2"),
            imageVersion(2, "v20241115.279641", "Arch-Linux-x86_64-cloudimg-20241115.279641.qcow2")
        ),
        "ubuntuImageVersionMap" to listOf(
            imageVersion(0,"24.04 LTS", "noble-server-cloudimg-amd64.qcow2"),
            imageVersion(1,"22.04 LTS", "jammy-server-cloudimg-amd64.qcow2"),
            imageVersion(2,"20.04 LTS", "focal-server-cloudimg-amd64.qcow2")
        ),
        "fedoraImageVersionMap" to listOf(
            imageVersion(0, "41", "Fedora-Cloud-Base-Generic-41-1.4.x86_64.qcow2"),
            imageVersion(1, "40", "Fedora-Cloud-Base-Generic.x86_64-40-1.14.qcow2"),
            imageVersion(2, "39", "Fedora-Cloud-Base-39-1.5.x86_64.qcow2")
        ),
        "debianImageVersionMap" to listOf(
            imageVersion(0,"Bookworm 12", "debian-cloud.qcow2"),
            imageVersion(1,"Bullseye 11", "debian-11-genericcloud-amd64.qcow2"),
            imageVersion(2,"Buster 10", "debian-10-genericcloud-amd64.qcow2")
        ),
        "almaImageVersionMap" to listOf(
            imageVersion(0, "AlmaLinux 9.5", "AlmaLinux-9-GenericCloud-9.5-20241120.x86_64.qcow2"),
            imageVersion(1, "AlmaLinux 8", "AlmaLinux-8-GenericCloud-8.10-20240819.x86_64.qcow2")
        ),
        "rockyImageVersionMap" to listOf(
            imageVersion(0, "Rocky Linux 9.5", "Rocky-9-GenericCloud-Base-9.5-20241118.0.x86_64.qcow2"),
            imageVersion(1, "Rocky Linux 8.9", "Rocky-8-GenericCloud-Base-8.9-20231119.0.x86_64.qcow2")
        )
    )

    val node = "pve" // Have to change this in future
    val localPath = "local"
    val localLvmPath = "local-lvm"
    val cdrom = "${localPath}:cloudinit"
    val ciuser = "eren"
    var cpu = "kvm64" // Default cpu type


    val printImageMap = mapOf(
        0 to "Arch",
        1 to "Ubuntu",
        2 to "Fedora",
        3 to "Debian",
        4 to "AlmaLinux",
        5 to "RockyLinux"
    )

    // Mention all the files for the versions, also match it the debianImageVersion above
    val imageMap = mapOf(
        0 to multiValueMap["archImageVersionMap"],
        1 to multiValueMap["ubuntuImageVersionMap"],
        2 to multiValueMap["fedoraImageVersionMap"],
        3 to multiValueMap["debianImageVersionMap"],
        4 to multiValueMap["almaImageVersionMap"],
        5 to multiValueMap["rockyImageVersionMap"]
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

    if (imageInput == 4 || imageInput == 5 ) {
        cpu = "host"
    }

    println()
    println("Choose the version : ")

    val selectedImage: List<imageVersion>  = imageMap[imageInput]!!
    // println(selectedImage)

    selectedImage.forEach{ image ->
        print("${image.id} : ${image.version}")
        println()
    }
    print("Enter the corresponding number : ")

    val selectedImageInput = readln().toInt()


    val selectedImageVersion: String = selectedImage[selectedImageInput].file

    // println("Selected version is $selectedImageVersion")
    println()
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
    print("Enter the IP Address for the VM - Example -> 10.2.0.20 : ")
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
            net0 = "virtio,bridge=vmbr0",
            cpu)
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
            ipconfig0 = "ip=${ipAddress}/24,gw=${gateway}",
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

    suspend fun startVmFunc() {
        val result = startVm(
            vmid,
            node
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

    // Invoke all the methods that are created
    createVmFunc()
    Thread.sleep(5000)
    attachDiskFunc()
    Thread.sleep(5000)
    resizeDiskFunc()
    Thread.sleep(5000)
    cloudInitFunc()
    Thread.sleep(5000)
    consoleAndBootOrderFunc()
    Thread.sleep(3000)
    startVmFunc()
}