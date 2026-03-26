import re

with open("build/reports/tests/test/classes/webView.actionPlanAdmin.ActionPlanAdminTest.html", "r") as f:
    text = f.read()

start = text.find("Full User Data JSON:")
if start != -1:
    json_start = text.find("{", start)
    end = text.find("</pre>", json_start)
    if end != -1:
        json_str = text[json_start:end].replace("&quot;", "\"")

        # Let's locate the position of "userProfile" in the string
        up_idx = json_str.find('"userProfile"')
        print("Index of userProfile:", up_idx)
        
        # Let's find "data":{"data":
        dd_idx = json_str.find('"data":{"')
        print("Index of data.data:", dd_idx)
        
        # where is the end of data.data?
        # we can't easily parse without failing on JSON errors.
        # But we can just see if up_idx > dd_idx.
        
        # We can also search for the context around it
        if up_idx != -1:
             print("Context before userProfile:", json_str[max(0, up_idx - 100): up_idx])
             print("Context after userProfile:", json_str[up_idx: up_idx + 100])
