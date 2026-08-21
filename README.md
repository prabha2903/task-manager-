# Task Management System 📋

A full-stack task management application built with Java Spring Boot, Spring Security, JWT authentication, MySQL, and a responsive HTML/CSS/JavaScript frontend.

**Live Demo:** https://task-manager-pj2c.onrender.com

---

## Features

- 🔐 Secure user registration and login with JWT authentication 
- 👥 Role-based access control using Spring Security
- 📋 Create, update, delete, assign, and track tasks
- 📁 Create and manage projects
- 📌 Kanban board for visual task management
- 🔎 Search and filter tasks
- 💬 Add and view comments on tasks
- 📜 Track task activity through activity logs
- 📊 Dashboard with task statistics and progress
- 🔔 Real-time notification support using WebSocket
- 📈 Task and project reports
- 📱 Responsive user interface

---

## Tech Stack

**Backend**
- Java
- Spring Boot
- Spring Security
- JWT Authentication
- Spring Data JPA / Hibernate
- REST APIs
- WebSocket
- Maven

**Frontend**
- HTML5
- CSS3
- JavaScript
- Bootstrap

**Database**
- MySQL

**Tools**
- IntelliJ IDEA
- Visual Studio Code
- MySQL Workbench
- Postman
- Git & GitHub

**Deployment**
- Backend hosted on **Render**

---

## Project Structure

```text
TaskManager/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/taskmanager/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── dto/
│   │   │       ├── exception/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       ├── security/
│   │   │       └── service/
│   │   │
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── css/
│   │       │   ├── js/
│   │       │   └── pages/
│   │       └── application.properties
│   │
├── pom.xml
├── Dockerfile
├── .gitignore
└── README.md

## **API Overview**

| Route | Purpose |
|---|---|
| `/api/auth` | User registration and authentication |
| `/api/users` | User management |
| `/api/tasks` | Task management |
| `/api/projects` | Project management |
| `/api/comments` | Task comments |
| `/api/activity` | Activity logs |
| `/api/dashboard` | Dashboard statistics |
| `/api/reports` | Reports |

---

## **Authentication Flow**

```text
User Login
    ↓
Spring Security
    ↓
JWT Token
    ↓
JWT Authentication Filter
    ↓
Protected REST APIs

Author

Prabha

B.E. Computer Science Engineering

Java | Spring Boot | Backend Development | Full-Stack Development
