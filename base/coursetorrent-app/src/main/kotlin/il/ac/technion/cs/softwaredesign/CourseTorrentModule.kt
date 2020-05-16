package il.ac.technion.cs.softwaredesign


import dev.misfitlabs.kotlinguice4.KotlinModule
import il.ac.technion.cs.softwaredesign.storage.SecureStorage
import il.ac.technion.cs.softwaredesign.storage.SecureStorageFactory
import il.ac.technion.cs.softwaredesign.storage.impl.SecureStorageFactoryImpl
import il.ac.technion.cs.softwaredesign.storage.impl.SecureStorageImpl


class CourseTorrentModule : KotlinModule() {
    override fun configure() {

        bind<SecureStorageFactory>().to<SecureStorageFactoryImpl>()
        //TODO : BIND TO OUR FACTORY
        }
    }
