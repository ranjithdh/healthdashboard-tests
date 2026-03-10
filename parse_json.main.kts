import java.io.File

val htmlFile = File("build/reports/tests/test/classes/webView.actionPlanAdmin.ActionPlanAdminTest.html")
val text = htmlFile.readText()
val startIndex = text.indexOf("Full User Data JSON: ")
if (startIndex != -1) {
    val jsonStart = startIndex + "Full User Data JSON: ".length
    val endIndex = text.indexOf("</pre>", jsonStart)
    if (endIndex != -1) {
        val jsonStr = text.substring(jsonStart, endIndex).replace("&quot;", "\"")
        
        // Find keys at root level of data.data
        val dataMatch = "\"data\":\\{".toRegex().findAll(jsonStr).toList()
        if (dataMatch.size >= 2) {
             val startKeys = dataMatch[1].range.last + 1
             val snippet = jsonStr.substring(startKeys, startKeys + 1000)
             println("Keys near data.data: $snippet")
        }
    }
} else {
    println("Could not find JSON")
}
