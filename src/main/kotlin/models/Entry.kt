package models

data class Entry(val name: String, val url: String, var isLoaded: Boolean) {
    companion object {
        private var counter = 0
    }
    val id = counter++
}