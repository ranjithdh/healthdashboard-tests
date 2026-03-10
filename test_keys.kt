import java.io.File
fun main() {
    val logDir = File("/Users/apple/Documents/QA-Automation/healthdashboard-tests/build/reports/tests/test/classes")
    if(logDir.exists()) {
        logDir.walkTopDown().filter { it.isFile && it.name.endsWith(".html") }.forEach {
           if(it.readText().contains("user_profile")) {
               println("Found user_profile in ${it.name}")
           }
        }
    }
}
