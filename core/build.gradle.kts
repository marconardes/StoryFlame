plugins {
    `java-library`
}

java {
    withSourcesJar()
}

dependencies {
    api("com.google.code.gson:gson:2.11.0")
    implementation("org.apache.pdfbox:pdfbox:3.0.3")
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
