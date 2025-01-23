import com.proxmox.api.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import java.net.URLEncoder
import kotlin.system.exitProcess
import java.nio.charset.StandardCharsets
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction

fun connectToSQLite() {
    Database.connect("jdbc:sqlite:./proxmox.db", driver = "org.sqlite.JDBC")
}

fun main() = runBlocking {

    // Start of the CLI
    val node = "pve" // Have to change this in future
    val localPath = "local"
    val localLvmPath = "local-lvm"
    val cdrom = "${localPath}:cloudinit"
    val ciuser = "eren"
    var cpu = "kvm64" // Default cpu type

    println("Welcome to Quick-Proxmox-VM")

    connectToSQLite()
    println("Choose a Distro")
    transaction {
        val allDistros = Distros.selectAll().map {
            it[Distros.distro_id] to it[Distros.name]
        }

        allDistros.forEach { (id, name) ->
            println("$id : $name")
        }
    }
    print("Enter the corresponding number : ")
    val selectedDistro: Int = readln().toInt()
    // println("Entered value is distroInput")

    if (selectedDistro == 5 || selectedDistro == 6) {
        cpu = "host"
    }

    println()
    println("Choose the version : ")

    transaction {
        val versions = DistroVersions.select(DistroVersions.distroId eq selectedDistro).map {
            it[DistroVersions.version_id] to it[DistroVersions.version]
        }
        versions.forEach { (version_id, version) ->
            println("$version_id : $version")
        }
    }

    print("Enter the corresponding number : ")
    val selectedVersion = readln().toInt()

    var qcow2_file: String = ""

    transaction {
        qcow2_file = DistroVersions.slice(DistroVersions.qcow2_file)
            .select(DistroVersions.version_id eq selectedVersion)
            .single().get(DistroVersions.qcow2_file)
        println(qcow2_file)
    }

    println()
    println("Choose the size of the vm")

    transaction {
        val sizes = VMSize.selectAll().map {
            listOf(
                it[VMSize.size_id],
                it[VMSize.cpu_cores],
                it[VMSize.storage],
                it[VMSize.memory]
            )
        }
        sizes.forEach { (id, cores, storage, mem) ->
            println("$id -> CPU : $cores  Storage : $storage  Memory : ${mem}M")
        }
    }

    print("Enter the corresponding number : ")
    val inputSize: Int = readln().toInt()

    var cores: Int = 0
    var memory: String = ""
    var storage: String = ""

    transaction {
        val selectedSize = VMSize.select { VMSize.size_id eq inputSize }.singleOrNull()
        if (selectedSize != null) {
            cores = selectedSize[VMSize.cpu_cores]
            memory = selectedSize[VMSize.memory]
            storage = selectedSize[VMSize.storage]
        }
    }

    println()

    println("Choose the SSH Key : ")

    transaction {
        val ssh_keys = Sshkeys.selectAll().map {
            it[Sshkeys.key_id] to it[Sshkeys.name]
        }
        ssh_keys.forEach { (id, name) ->
            println("$id : $name")
        }
    }

    print("Enter the corresponding number or Enter 0 to use paste the SSH Key : ")

    var sshKey: String = ""
    val inputSSHKey = readln().toInt()
    if (inputSSHKey == 0) {
        print("Enter the SSH Key : ")
        val ssh_key = readln()
        val ssh_key_url_encoded = URLEncoder.encode(ssh_key, StandardCharsets.UTF_8.toString()).replace("+", "%20")
        sshKey = ssh_key_url_encoded
        println()
        print("Enter the name for the SSH Key : ")
        val name = readln()
        transaction {
            val keys = listOf(
                ssh_key to ssh_key_url_encoded
            )
            Sshkeys.batchInsert(keys) { (key, encoded_key) ->
                this[Sshkeys.ssh_key] = key
                this[Sshkeys.ssh_key_url_encoded] = encoded_key
                this[Sshkeys.name] = name
            }
        }
    }
    else {
        transaction {
            sshKey = Sshkeys.slice(Sshkeys.ssh_key_url_encoded).select(Sshkeys.key_id eq inputSSHKey).single().get(Sshkeys.ssh_key_url_encoded)
        }
    }

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
            cpu
        )
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
            qcow2File = qcow2_file,
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