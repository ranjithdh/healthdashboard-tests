export default {
    name: "HealthDashboard E2E Tests",
    output: "build/allure-report-v3",
    historyPath: "build/allure-history.jsonl",
    plugins: {
        awesome: {
            options: {
                reportName: "HealthDashboard: All Reports",
                singleFile: false,
                reportLanguage: "en",
                groupBy: ["epic", "feature", "story"],
            },
        },
        awesomeE2E: {
            options: {
                reportName: "HealthDashboard: Awesome E2E",
                singleFile: false,
                reportLanguage: "en",
                groupBy: ["epic", "feature", "story"],
            },
        }
    },
};
