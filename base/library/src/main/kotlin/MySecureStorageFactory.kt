import il.ac.technion.cs.softwaredesign.storage.SecureStorage
import il.ac.technion.cs.softwaredesign.storage.SecureStorageFactory
import il.ac.technion.cs.softwaredesign.storage.impl.SecureStorageFactoryImpl

class MySecureStorageFactory : SecureStorageFactory {

    var factory = SecureStorageFactoryImpl()

    override fun open(name: ByteArray): SecureStorage {
        return factory.open(name)
    }
}