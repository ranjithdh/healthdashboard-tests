import { defineConfig } from "allure";

export default defineConfig({
    name: "HealthDashboard E2E Tests",
    output: "build/allure-report-v3",
    historyPath: "build/allure-history.jsonl",
    plugins: {
        awesome: {
            import: "@allurereport/plugin-awesome",
            options: {
                reportName: "HealthDashboard: All Reports",
                singleFile: false,
                reportLanguage: "en",
                groupBy: ["epic", "feature", "story"],
            },
        },
        awesomeE2E: {
            import: "@allurereport/plugin-awesome",
            options: {
                reportName: "HealthDashboard: Awesome E2E",
                singleFile: false,
                reportLanguage: "en",
                groupBy: ["epic", "feature", "story"],
                // You can add filters here if needed, e.g. based on labels
            },
        }
    },
});
