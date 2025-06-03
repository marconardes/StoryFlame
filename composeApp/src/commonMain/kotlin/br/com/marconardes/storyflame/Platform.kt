package br.com.marconardes.storyflame

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform