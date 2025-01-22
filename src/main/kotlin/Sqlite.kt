import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URLEncoder
import java.nio.charset.StandardCharsets



object Distros : Table("distros") {
    val distro_id = integer("os_id").autoIncrement()
    val name = varchar("name", 255)
    override val primaryKey = PrimaryKey(distro_id)
}

object DistroVersions : Table("distro_versions") {
    val version_id = integer("version_id").autoIncrement()
    val version = varchar("version", 255)
    val qcow2_file = varchar("qcow2_file", 255)
    val distroId = integer("distro_id").references(Distros.distro_id, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(version_id)
}

object Username: Table("username") {
    val user_id = integer("user_id").autoIncrement()
    val username = varchar("username", 255)
    override val primaryKey = PrimaryKey(user_id)
}

object Sshkeys: Table("ssh_keys") {
    val key_id = integer("key_id").autoIncrement()
    val ssh_key = text("ssh_key")
    val ssh_key_url_encoded = text("ssh_key_url_encoded")
    override val primaryKey = PrimaryKey(key_id)
}

object VMSize: Table("vm_size") {
    val size_id = integer("size_id").autoIncrement()
    val cpu_cores = integer("cpu_cores")
    val storage = varchar("storage", 255)
    val memory = varchar("memory", 255)
    override val primaryKey = PrimaryKey(size_id)
}

fun createTables() {
    transaction {
        SchemaUtils.create(Distros, DistroVersions, Username, Sshkeys, VMSize)
    }
}

fun main() {
    connectToSQLite()
//    createTables()
//    transaction {
//        val rows = listOf(
//            "Arch",
//            "Ubuntu",
//            "Fedora",
//            "Debian",
//            "Alma",
//            "Rocky"
//        )
//
//        Distros.batchInsert(rows) { distroName ->
//            this[Distros.name] = distroName
//        }
//    }

//    transaction {
//        val distroId = Distros.select { Distros.name eq "Debian" }
//            .singleOrNull()?.get(Distros.distro_id)
//        print(distroId)
//
//        if (distroId != null) {
////            val versions = listOf(
////                "Rocky Linux 9.5" to "Rocky-9-GenericCloud-Base-9.5-20241118.0.x86_64.qcow2",
////                "Rocky Linux 8.9" to "Rocky-8-GenericCloud-Base-8.9-20231119.0.x86_64.qcow2"
////            )
//
////            val versions = listOf(
////                "v20241215.289320" to "Arch-Linux-x86_64-cloudimg-20241215.289320.qcow2",
////                "v20241201.284788" to "Arch-Linux-x86_64-cloudimg-20241201.284788.qcow2",
////                "v20241115.279641" to "Arch-Linux-x86_64-cloudimg-20241115.279641.qcow2"
////            )
//
////            val versions = listOf(
////                "24.04 LTS" to "noble-server-cloudimg-amd64.qcow2",
////                "22.04 LTS" to "jammy-server-cloudimg-amd64.qcow2",
////                "20.04 LTS" to "focal-server-cloudimg-amd64.qcow2"
////            )
//
////            val versions = listOf(
////                "41" to "Fedora-Cloud-Base-Generic-41-1.4.x86_64.qcow2",
////                "40" to "Fedora-Cloud-Base-Generic.x86_64-40-1.14.qcow2",
////                "39" to "Fedora-Cloud-Base-39-1.5.x86_64.qcow2"
////            )
//
////            val versions = listOf(
////                "Bookworm 12" to "debian-cloud.qcow2",
////                "Bullseye 11" to "debian-11-genericcloud-amd64.qcow2",
////                "Buster 10" to "debian-10-genericcloud-amd64.qcow2"
////            )
//
////            val versions = listOf(
////                "AlmaLinux 9.5" to "AlmaLinux-9-GenericCloud-9.5-20241120.x86_64.qcow2",
////                "AlmaLinux 8" to "AlmaLinux-8-GenericCloud-8.10-20240819.x86_64.qcow2",
////            )
//
//            DistroVersions.batchInsert(versions) { (version, qcow2_file) ->
//                this[DistroVersions.version] = version
//                this[DistroVersions.qcow2_file] = qcow2_file
//                this[DistroVersions.distroId] = distroId
//            }
//        }
//    }

//    println("Welcome to Proxmox Quick VM !!")
//    println("Choose the distro :  ")
//
//    transaction {
//        val allDistros = Distros.selectAll().map {
//            it[Distros.distro_id] to it[Distros.name]
//        }
//
//        allDistros.forEach { (id, name) ->
//            println("$id : $name")
//        }
//    }
//
//    print("Enter the no : ")
//    val selectedDistro = readln().toInt()
//
//
//    transaction {
//        val versions = DistroVersions.select(DistroVersions.distroId eq selectedDistro).map {
//            it[DistroVersions.version_id] to it[DistroVersions.version]
//        }
//        versions.forEach { (version_id, version) ->
//            println("$version_id : $version")
//        }
//    }
//    print("Choose the version : ")
//    val selectedVersion = readln().toInt()
//
//    transaction {
//        val qcow2_file = DistroVersions.slice(DistroVersions.qcow2_file)
//            .select(DistroVersions.version_id eq selectedVersion)
//            .single().get(DistroVersions.qcow2_file)
//        println(qcow2_file)
//    }


//    transaction {
//        val users = listOf(
//            "eren",
//            "tamila",
//            "light"
//        )
//        Username.batchInsert(users){ user ->
//            this[Username.username] = user
//        }
//    }


//    transaction {
//        val sizes = listOf(
//            Triple(2,"50G","2048"),
//            Triple(1,"25G","1024")
//        )
//        VMSize.batchInsert(sizes) { (cores, storage, memory) ->
//            this[VMSize.cpu_cores] = cores
//            this[VMSize.storage] = storage
//            this[VMSize.memory] = memory
//        }
//    }

//    val public_key = "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIJFSCgxWu3m0FNyTnETY/yVZZn7OZddUGY1Yk2JNHHnJ tamila@warhammer"
//    val ssh_key_url_encoded = URLEncoder.encode(public_key, StandardCharsets.UTF_8.toString()).replace("+", "%20")
//    println(ssh_key_url_encoded)
//
//    transaction {
//        val keys = listOf(
//            public_key to ssh_key_url_encoded
//        )
//        Sshkeys.batchInsert(keys) { (key, encoded_key) ->
//            this[Sshkeys.ssh_key] = key
//            this[Sshkeys.ssh_key_url_encoded] = encoded_key
//        }
//    }

}