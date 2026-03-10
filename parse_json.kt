import java.io.File

fun main() {
    val htmlFile = File("build/reports/tests/test/classes/webView.actionPlanAdmin.ActionPlanAdminTest.html")
    val text = htmlFile.readText()
    val startIndex = text.indexOf("Full User Data JSON: {")
    if (startIndex != -1) {
        val jsonStart = text.indexOf("{", startIndex)
        val endIndex = text.indexOf("</pre>", jsonStart)
        if (endIndex != -1) {
            val jsonStr = text.substring(jsonStart, endIndex).replace("&quot;", "\"")
            
            // let's just find where `"userProfile"` or `"user_profile"` is
            val idx1 = jsonStr.indexOf("userProfile\"")
            val idx2 = jsonStr.indexOf("user_profile\"")
            if (idx1 != -1) println("Contains userProfile at $idx1")
            if (idx2 != -1) println("Contains user_profile at $idx2")
            
            // extract the first 1000 chars near `"data":{"`
            var dIdx = jsonStr.indexOf("\"data\":{")
            if (dIdx != -1) {
                dIdx = jsonStr.indexOf("\"data\":{", dIdx + 1)
                if (dIdx != -1) {
                    println(jsonStr.substring(dIdx, dIdx + 1000.coerceAtMost(jsonStr.length - dIdx)))
                }
            }
        }
    } else {
        println("Could not find JSON")
    }
}
