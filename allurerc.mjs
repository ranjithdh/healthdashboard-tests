// Allure 3 Configuration - Single file mode for easy local viewing
export default {
    name: "HealthDashboard E2E Tests",
    output: "build/allure-report-v3",
    historyPath: "build/allure-history.jsonl",
    plugins: {
        awesome: {
            options: {
                reportName: "HealthDashboard E2E Tests",
                singleFile: true,  // Creates a single HTML file that opens directly
                reportLanguage: "en",
                groupBy: ["parentSuite", "suite", "subSuite"],
            },
        },
    },
};
