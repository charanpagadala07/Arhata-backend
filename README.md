# Arhata-AI

An AI-powered candidate screening and shortlisting platform that helps recruiters analyze large applicant pools against a Job Description (JD) and custom screening criteria.

Arhata-AI is designed to reduce the manual effort involved in reviewing hundreds or thousands of applications and help recruiters quickly identify the most relevant candidates.

---

## What Does This Project Do?

ShortlistAI allows a recruiter to:

- Upload an Excel (`.xlsx`) file containing applicant information.
- Provide a Job Description.
- Define custom screening criteria.
- Send the applicant data and requirements to Google Gemini for AI analysis.
- Receive a structured shortlist of candidates.
- Choose the percentage of candidates to shortlist.
- View shortlisted candidates and their relevant information.
- Store screening jobs and results in a SQL database.

For example:

> 1,000 applicants + 50% shortlist → the system identifies the top 500 candidates based on the provided JD and screening criteria.

The system is intended as an **AI-assisted recruitment tool**, not as a replacement for human recruiters.

---

##  How It Works

```text
Recruiter
    │
    │ Upload XLSX + Job Description + Criteria
    ▼
API Gateway
    │
    ▼
Screening Service
    │
    ├── Read applicant data
    ├── Process screening requirements
    │
    ▼
Google Gemini
    │
    │ AI analyzes candidates
    ▼
Structured Screening Result
    │
    ├── Candidate ranking
    ├── Match/relevance information
    └── Candidate details
    │
    ▼
SQL Database
    │
    ▼
Frontend
```

---

## Gemini AI Integration

The Screening Service sends the relevant applicant information together with:

- Job Description
- Screening Criteria
- Candidate information

to Google Gemini.

Gemini analyzes candidates according to the provided requirements and returns structured screening information.

A simplified example of the response is:

```json
{
  "shortlistedCandidates": [
    {
      "name": "John Doe",
      "email": "john@example.com",
      "score": 92,
      "reason": "Strong match for Java and Spring Boot requirements"
    },
    {
      "name": "Jane Smith",
      "email": "jane@example.com",
      "score": 87,
      "reason": "Good backend experience with relevant Spring technologies"
    }
  ]
}
```

The exact response depends on the Job Description, applicant data, and screening criteria provided to Gemini.

The application processes the AI response and stores the screening job and results in the database.

---

## Architecture

The backend follows a microservices architecture:

```text
                    ┌──────────────────┐
                    │   API Gateway    │
                    │      :8080       │
                    └────────┬─────────┘
                             │
                    ┌────────┴─────────┐
                    │                  │
              Auth Service       Screening Service
                 :8081                 :8083
                    │                  │
                    │                  ├── Gemini API
                    │                  │
                    │                  └── SQL Database
                    │
                    └──── JWT Authentication

                         ▲
                         │
                   Eureka Server
                      :8761
```

### Main Components

| Component | Responsibility |
|---|---|
| Eureka Server | Service discovery |
| API Gateway | Single entry point and request routing |
| Auth Service | Authentication and JWT generation/validation |
| Screening Service | Applicant processing and AI screening |
| SQL Database | Persistent storage of screening data |
| Google Gemini | AI-powered candidate analysis |
| React Frontend | User interface |

---

##  Authentication

Authentication is handled using JWT.

The typical flow is:

```text
Login
  ↓
Auth Service
  ↓
JWT Token
  ↓
Frontend
  ↓
Authorization: Bearer <token>
  ↓
API Gateway
  ↓
JWT Validation
  ↓
Protected Microservice
```

Authentication endpoints are publicly accessible, while protected screening endpoints require a valid JWT token.

---

## 📥 How Can You Use This Project?

### 1. Clone the Repository

```bash
git clone <repository-url>
cd ShortlistAI
```

### 2. Start Eureka Server

Start Eureka first so that the microservices can register themselves.

```text
Eureka Server → :8761
```

### 3. Start the Microservices

Start:

```text
Auth Service       → :8081
Screening Service  → :8083
API Gateway        → :8080
```

The services register themselves with Eureka.

### 4. Configure Gemini

Add your Google Gemini API key to the Screening Service configuration.

**Do not commit API keys or secrets to GitHub.**

Use environment variables or external configuration.

Example:

```properties
GEMINI_API_KEY=your_api_key
JWT_SECRET=your_jwt_secret
```

### 5. Authenticate

Login through the API Gateway and obtain a JWT.

### 6. Submit a Screening Request

Send a `multipart/form-data` request through the API Gateway:

```text
POST /api/screening/analyze
```

Required fields:

```text
file               → Applicant Excel file
jobDescription     → Job Description
screeningCriteria  → Screening requirements
Authorization      → Bearer <JWT>
```

### 7. Retrieve the Shortlist

After the screening job is created:

```text
GET /api/screening/{jobId}/shortlist?percentage=50
```

This returns the candidates selected according to the requested shortlist percentage.

---

## 🛠️ Tech Stack

### Backend

- Java
- Spring Boot
- Spring Cloud
- Spring Cloud Gateway
- Netflix Eureka
- Spring Security
- JWT
- REST APIs
- SQL
- Maven

### AI

- Google Gemini API

### Frontend

- React
- Vite
- JavaScript / JSX
- Tailwind CSS
- React Query
- shadcn/ui

---

## Future Improvements

Possible future enhancements include:

- Resume file processing
- Advanced candidate scoring
- Recruiter dashboards
- Screening history and analytics
- Role-based access control
- Asynchronous processing using Kafka/RabbitMQ
- Rate limiting and monitoring
- Cloud deployment

---

## Project Goal

ShortlistAI demonstrates how Generative AI can be integrated into a practical microservices-based application to solve a real-world recruitment problem.

The project combines:

**Microservices + Service Discovery + API Gateway + JWT Security + SQL + Generative AI**

to create an end-to-end AI-assisted candidate screening system.
