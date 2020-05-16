val externalLibraryVersion: String? by extra
val guiceVersion: String? by extra
val kotlinGuiceVersion: String? by extra
dependencies {
    implementation("il.ac.technion.cs.softwaredesign", "primitive-storage-layer", externalLibraryVersion)
   // implementation(project(":coursetorrent-app"))
    implementation("com.google.inject", "guice", guiceVersion)
    implementation("dev.misfitlabs.kotlinguice4", "kotlin-guice", kotlinGuiceVersion)
}

