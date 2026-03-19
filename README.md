# 🤖 AI Code Reviewer

An enterprise-grade, full-stack SaaS application that automatically analyzes your GitHub Pull Requests using Google Gemini and posts actionable, single-click inline code suggestions directly to your repository!

## ✨ Features
* **Automated AI Intelligence**: Leverages **Google Gemini 2.5 Flash** via Spring AI to detect security vulnerabilities, logic errors, and styling inconsistencies inside your PR diffs.
* **Granular Inline Suggestions**: Utilizes advanced JSON structuring to output precise markdown ````suggestion```` blocks attached exactly to the correct line numbers so developers can accept the changes with a single click.
* **Hybrid Review System**: Generates a high-level architectural overview coupled with surgically precise codebase refactoring blocks.
* **GitHub Native**: Authenticates seamlessly using GitHub OAuth2 and operates 100% autonomously via Webhook events.
* **Modern UI**: A beautiful, glassmorphic React/Vite frontend crafted with TailwindCSS for managing registered repositories.
* **Containerized Production**: Includes full Docker and Docker Compose support for effortless deployment of the Spring Boot application, NGINX frontend proxy, and PostgreSQL database.

## 🛠️ Technology Stack
* **Backend**: Spring Boot 3.4, Java 21, Spring Security (OAuth2), Spring AI (OpenAI REST compatibility), Spring Data JPA, PostgreSQL.
* **Frontend**: React 19, Vite, Tailwind CSS v4, Lucide React, Axios.
* **Orchestration**: Docker, Docker Compose.

---

## 🚀 Running Locally

### 1. Prerequisites
You will require a few local dependencies and secure developer keys:
* **Java 21** & **Node.js 22**
* **Google Gemini API Key** (acquired from Google AI Studio)
* **GitHub Classic Personal Access Token** (equipped with `repo` scopes)
* **GitHub OAuth Application** (acquired from GitHub Developer Settings, with the callback URL set to `http://localhost:8080/login/oauth2/code/github`)

### 2. Environment Variables
Inject your secure keys into your system's Environment Variables (or a strict `.env` layer) so Spring Boot can map them:
```text
GITHUB_CLIENT_ID=<your_oauth_client_id>
GITHUB_CLIENT_SECRET=<your_oauth_client_secret>
GEMINI_API_KEY=<your_gemini_api_key>
GITHUB_TOKEN=<your_github_classic_pat>
```

### 3. Start the Spring Boot Backend
```bash
cd ai-code-reviewer-backend
./mvnw clean spring-boot:run
```
*(Runs securely on `http://localhost:8080`)*

### 4. Start the Vite Frontend
```bash
cd ai-code-reviewer-frontend
npm install
npm run dev
```
*(Runs securely on `http://localhost:5173`. Clicking "Continue with GitHub" will authenticate matching against the backend.)*

---

## 🐳 Running with Docker
The entire project architecture can be spun up asynchronously using the Docker-Compose network.
```bash
docker-compose up --build -d
```
Docker mounts the optimized `eclipse-temurin` build outputs for Spring Boot and proxies Vite through a lightweight `Nginx` web server.

---

## 🔗 Testing Webhooks
If running locally without a static IP, you need to tunnel GitHub Webhooks past your local firewall using `smee-client`. There are two ways to do this:

### Option 1: Automated Script (Recommended)
We provide a built-in script that generates a random proxy URL and automatically starts the tunnel for you:
```bash
node ai-code-reviewer-frontend/setup-tunnel.cjs
```
*(The script will print a generated `https://smee.io/...` URL to the console. Copy this and paste it as the Payload URL in your GitHub Repository's Webhook Settings.)*

### Option 2: Manual Setup (Persistent URL)
If you want to use the same persistent webhook URL every time so you don't have to keep updating GitHub:
1. Go to [smee.io](https://smee.io/) and click "Start a new channel".
2. You will be given a unique Webhook Proxy URL (e.g., `https://smee.io/aB1cD2eF3gH4iJ5k`).
3. Install the client globally (if you haven't already):
```bash
npm install --global smee-client
```
4. Run the proxy command manually, replacing the URL with your unique one:
```bash
smee --url https://smee.io/YOUR_UNIQUE_URL --path /api/webhooks/github --port 8080
```

Register your chosen `smee.io` payload URL onto your target GitHub repository under **Settings -> Webhooks**, create a PR, and watch the AI process your code within SECONDS!
