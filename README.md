# ScriptReview 🤖📄

**ScriptReview** is an intelligent RPA code review automation tool built with **Spring Boot**. It leverages **Google Gemini AI** to analyze UiPath and Blue Prism scripts, providing detailed code quality assessments, compatibility scores, and actionable recommendations in a downloadable Excel format.

---

## 🚀 Key Features

* **Multi-Platform Support:** Supports upload of **UiPath** (`.xaml`) and **Blue Prism** (`.xml`, `.bprelease`) script files.
* **AI-Powered Analysis:** secure integration with **Google Gemini API** to perform logic verification and code review based on custom prompts.
* **Structured Metrics:** Parses AI responses into structured JSON data:
    * `tool`: Detects if it is UiPath or Blue Prism.
    * `compatibility_score`: A calculated quality score (0-100).
    * `issues`: List of identified bugs, logic errors, or security risks.
    * `recommendations`: Suggested fixes and best practices.
* **Automated Reporting:** Converts the analysis results into a formatted **Excel (.xlsx)** file for immediate download.

---

## 🛠 Tech Stack

* **Backend:** Java 17+, Spring Boot 3.x
* **AI Integration:** Google Gemini API
* **Data Processing:** Jackson (JSON Parsing)
* **Report Generation:** Apache POI (Excel Export)
* **Build Tool:** Maven

---

## ⚙️ How It Works

1.  **Upload:** The user uploads a script file via the REST API.
2.  **Prompt Construction:** The backend reads the file and appends a specific prompt: *"Analyze this script, identify the tool, assign a score, list issues, and recommend fixes in JSON format."*
3.  **AI Processing:** The payload is sent to Gemini.
4.  **JSON Parsing:** The backend parses the raw text response into a Java Object.
5.  **Excel Conversion:** The object data is written to an Excel sheet.
6.  **Download:** The file is returned to the user.

---

## 📝 Prerequisites

* Java JDK 17 or higher
* Maven installed
* A valid **Google Gemini API Key**

---

## 🔧 Installation & Configuration

  **Clone the Repository**
    ```bash
    git clone [https://github.com/your-username/script-review.git](https://github.com/your-username/script-review.git)
    cd script-review
    ```


## 🧠 AI Response Format (Internal)

The system expects Gemini to return JSON in the following structure before converting it to Excel:

```json
{
  "tool": "UiPath",
  "compatibility_score": 85,
  "issues": [
    "Variable 'password' is hardcoded in the login sequence.",
    "Timeout value is set to infinite."
  ],
  "recommendations": [
    "Use Windows Credential Manager for passwords.",
    "Set a default timeout of 30 seconds."
  ]
}
