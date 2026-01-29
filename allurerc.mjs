// Allure 3 Configuration - Simplified for npx usage
export default {
    name: "HealthDashboard E2E Tests",
    output: "build/allure-report-v3",
    historyPath: "build/allure-history.jsonl",
    plugins: {
        awesome: {
            options: {
                reportName: "HealthDashboard E2E Tests",
                singleFile: false,
                reportLanguage: "en",
                groupBy: ["parentSuite", "suite", "subSuite"],
            },
        },
    },
};
