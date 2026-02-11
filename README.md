# CloudNest ☁️

CloudNest is a powerful, modern, and user-centric personal cloud storage platform designed to help you manage, store, and share your digital life effortlessly. Built with a focus on security, collaboration, and seamless user experience, CloudNest provides a robust alternative to traditional storage solutions.

## 🚀 Key Features

- **Personal Dashboard**: A clean, intuitive interface to manage all your files at a glance.
- **File Versioning**: Automatically tracks changes to your documents, allowing you to view and restore previous versions.
- **Secure File Sharing**: Share files with specific users and manage access permissions with ease.
- **Real-time Notifications**: Stay updated with activities related to your shared files and system alerts.
- **Collaboration**: Seamlessly collaborate with partners and team members on shared documents.
- **Role-Based Access Control**: Secure authentication and authorization for different user levels.
- **Cloud Storage Integration**: Scalable and reliable file storage powered by AWS S3.

## 🛠️ Tech Stack

### Frontend
- **Framework**: [React](https://react.dev/) (Vite)
- **Routing**: [React Router](https://reactrouter.com/)
- **State/Data Fetching**: Axios
- **Icons**: [Lucide React](https://lucide.dev/)
- **Styling**: Modern, responsive CSS

### Backend
- **Core**: [Spring Boot 3.3.5](https://spring.io/projects/spring-boot)
- **Language**: Java 17
- **Database**: [PostgreSQL](https://www.postgresql.org/)
- **Schema Management**: [Flyway](https://flywaydb.org/)
- **Security**: Spring Security & JWT
- **File Storage**: [AWS S3](https://aws.amazon.com/s3/)
- **Build Tool**: Maven

## 🏗️ Project Structure

```text
CloudNest/
├── CloudNest-frontend/     # React frontend application
└── cloud-nest-backend/      # Spring Boot backend application
```

## ⚙️ Getting Started

### Prerequisites
- JDK 17
- Node.js (v18+)
- PostgreSQL
- AWS Account (for S3 storage)

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/CloudNest.git
   cd CloudNest
   ```

2. **Backend Setup**:
   - Navigate to the backend directory: `cd cloud-nest-backend`
   - Configure your PostgreSQL database and AWS S3 credentials in `src/main/resources/application.properties`.
   - Run the application: `./mvnw spring-boot:run`

3. **Frontend Setup**:
   - Navigate to the frontend directory: `cd CloudNest-frontend`
   - Install dependencies: `npm install`
   - Start the development server: `npm run dev`

## 🛡️ Security

CloudNest implements industry-standard security practices, including:
- JWT-based authentication
- Password hashing
- Secure AWS S3 buckets for file storage
- Role-based endpoint protection

## 🤝 Contributing

Contributions are welcome! If you'd like to improve CloudNest, please fork the repository and submit a pull request.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
