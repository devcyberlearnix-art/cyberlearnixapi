package com.lms.seeder.generator;

import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
public class CourseGenerator {

    private static final Random random = new Random();

    private static final String[] CATEGORIES = {
        "Java/JVM", "Spring Boot", "Microservices", "Backend Development",
        "SQL/PostgreSQL", "Docker/Kubernetes", "AWS/Cloud", "DevOps/CI-CD",
        "Python", "Cyber Security", "React/JavaScript", "AI/ML",
        "Data Engineering", "System Design", "Software Engineering"
    };

    private static final String[] LEVELS = {
        "Beginner", "Intermediate", "Advanced"
    };

    private static final String[] LANGUAGES = {
        "English", "Hindi", "Tamil", "Telugu"
    };

    // Weighted status distribution for realistic proportions
    private static final String[] STATUSES = {
        "PUBLISHED", "PUBLISHED", "PUBLISHED", "PUBLISHED", "PUBLISHED",
        "PUBLISHED", "PUBLISHED", "PUBLISHED", "DRAFT", "ARCHIVED"
    };

    // Course templates with realistic titles and descriptions
    private static final CourseTemplate[] COURSE_TEMPLATES = {
        new CourseTemplate("Java/JVM",
            "Master Java Programming from Scratch",
            "Learn Java fundamentals, OOP concepts, and advanced features to build robust applications",
            "Beginner to Advanced Java - Complete Guide"),
        new CourseTemplate("Java/JVM",
            "Java Multithreading and Concurrency",
            "Master multithreading, concurrency patterns, and performance optimization in Java",
            "Advanced Java Concurrency Mastery"),
        new CourseTemplate("Java/JVM",
            "Java Performance Tuning",
            "Optimize Java applications for maximum performance and scalability",
            "Java Performance Engineering"),
        new CourseTemplate("Spring Boot",
            "Spring Boot Masterclass",
            "Build production-ready Spring Boot applications with best practices",
            "Complete Spring Boot Developer Guide"),
        new CourseTemplate("Spring Boot",
            "Spring Boot Microservices",
            "Design and deploy scalable microservices using Spring Boot and Spring Cloud",
            "Microservices with Spring Boot"),
        new CourseTemplate("Spring Boot",
            "Spring Security in Depth",
            "Implement authentication, authorization, and security in Spring applications",
            "Spring Security Complete Course"),
        new CourseTemplate("Microservices",
            "Microservices Architecture Patterns",
            "Learn microservices design patterns, communication, and deployment strategies",
            "Microservices Architecture Masterclass"),
        new CourseTemplate("Microservices",
            "Event-Driven Microservices",
            "Build event-driven architectures with Kafka and message brokers",
            "Event-Driven Architecture with Kafka"),
        new CourseTemplate("Backend Development",
            "REST API Design Best Practices",
            "Design scalable, secure, and maintainable REST APIs",
            "REST API Design and Development"),
        new CourseTemplate("Backend Development",
            "GraphQL for Backend Development",
            "Build efficient GraphQL APIs for modern applications",
            "GraphQL Backend Development"),
        new CourseTemplate("SQL/PostgreSQL",
            "PostgreSQL Database Mastery",
            "Master PostgreSQL from basics to advanced features and optimization",
            "Complete PostgreSQL Guide"),
        new CourseTemplate("SQL/PostgreSQL",
            "Advanced SQL and Query Optimization",
            "Write complex SQL queries and optimize database performance",
            "Advanced SQL Techniques"),
        new CourseTemplate("Docker/Kubernetes",
            "Docker and Containerization",
            "Learn Docker fundamentals and containerize applications",
            "Docker Complete Course"),
        new CourseTemplate("Docker/Kubernetes",
            "Kubernetes for Production",
            "Deploy and manage containerized applications at scale with Kubernetes",
            "Kubernetes Production Deployment"),
        new CourseTemplate("AWS/Cloud",
            "AWS Cloud Practitioner",
            "Get started with AWS cloud services and infrastructure",
            "AWS Cloud Fundamentals"),
        new CourseTemplate("AWS/Cloud",
            "AWS Solutions Architect",
            "Design scalable and secure AWS architectures",
            "AWS Solutions Architect Professional"),
        new CourseTemplate("DevOps/CI-CD",
            "DevOps Fundamentals",
            "Learn DevOps practices, tools, and culture",
            "Complete DevOps Guide"),
        new CourseTemplate("DevOps/CI-CD",
            "CI/CD with Jenkins and GitLab",
            "Build automated CI/CD pipelines for continuous delivery",
            "CI/CD Pipeline Mastery"),
        new CourseTemplate("Python",
            "Python Programming Complete",
            "Learn Python from basics to advanced programming",
            "Python Complete Masterclass"),
        new CourseTemplate("Python",
            "Python for Data Science",
            "Use Python for data analysis, visualization, and machine learning",
            "Python Data Science Bootcamp"),
        new CourseTemplate("Cyber Security",
            "Ethical Hacking and Penetration Testing",
            "Learn ethical hacking techniques and security testing",
            "Ethical Hacking Complete Course"),
        new CourseTemplate("Cyber Security",
            "Network Security and Firewalls",
            "Secure networks and implement firewall configurations",
            "Network Security Fundamentals"),
        new CourseTemplate("React/JavaScript",
            "React.js Complete Guide",
            "Build modern web applications with React and Redux",
            "React.js Masterclass"),
        new CourseTemplate("React/JavaScript",
            "Advanced React Patterns",
            "Master advanced React patterns and performance optimization",
            "Advanced React Development"),
        new CourseTemplate("AI/ML",
            "Machine Learning with Python",
            "Build ML models using Python and scikit-learn",
            "Machine Learning Complete Course"),
        new CourseTemplate("AI/ML",
            "Deep Learning with TensorFlow",
            "Build neural networks and deep learning models",
            "Deep Learning with TensorFlow"),
        new CourseTemplate("Data Engineering",
            "Data Engineering with Apache Spark",
            "Build data pipelines using Apache Spark and big data technologies",
            "Data Engineering with Spark"),
        new CourseTemplate("Data Engineering",
            "ETL and Data Warehousing",
            "Design and implement ETL processes and data warehouses",
            "ETL and Data Warehousing"),
        new CourseTemplate("System Design",
            "System Design Interviews",
            "Master system design concepts for technical interviews",
            "System Design Interview Prep"),
        new CourseTemplate("System Design",
            "Scalable System Architecture",
            "Design scalable and reliable distributed systems",
            "Scalable System Design"),
        new CourseTemplate("Software Engineering",
            "Clean Code and Refactoring",
            "Write maintainable, readable, and clean code",
            "Clean Code Principles"),
        new CourseTemplate("Software Engineering",
            "Software Testing and QA",
            "Master software testing strategies and automation",
            "Software Testing Complete Guide")
    };

    private static class CourseTemplate {
        String category;
        String title;
        String description;
        String subtitle;

        CourseTemplate(String category, String title, String description, String subtitle) {
            this.category = category;
            this.title = title;
            this.description = description;
            this.subtitle = subtitle;
        }
    }

    public static String generateCategory() {
        return CATEGORIES[random.nextInt(CATEGORIES.length)];
    }

    public static String generateLevel() {
        return LEVELS[random.nextInt(LEVELS.length)];
    }

    public static String generateLanguage() {
        return LANGUAGES[random.nextInt(LANGUAGES.length)];
    }

    public static String generateStatus() {
        // Weighted distribution: more PUBLISHED
        return STATUSES[random.nextInt(STATUSES.length)];
    }

    public static CourseData generateCourseData() {
        CourseTemplate template = COURSE_TEMPLATES[random.nextInt(COURSE_TEMPLATES.length)];
        return new CourseData(
            template.category,
            template.title,
            template.subtitle,
            template.description,
            generateLevel(),
            generateLanguage(),
            generateStatus(),
            generatePrice(),
            generateViewCount(),
            generateSearchCount(),
            random.nextBoolean() // premium
        );
    }

    public static double generatePrice() {
        // Realistic INR prices: ₹999 to ₹14999
        int[] pricePoints = {999, 1499, 1999, 2499, 2999, 3999, 4999, 5999, 7999, 9999, 12999, 14999};
        return pricePoints[random.nextInt(pricePoints.length)];
    }

    public static long generateViewCount() {
        // 0 to 50000 views
        return random.nextInt(50001);
    }

    public static long generateSearchCount() {
        // 0 to 10000 searches
        return random.nextInt(10001);
    }

    public static String generateSectionTitle(String courseCategory, int sectionIndex) {
        String[] sectionTemplates = {
            "Introduction to {category}",
            "{category} Fundamentals",
            "Advanced {category} Concepts",
            "Practical {category} Projects",
            "{category} Best Practices",
            "Building {category} Applications",
            "{category} Tools and Frameworks",
            "Debugging and Troubleshooting {category}",
            "Performance Optimization in {category}",
            "Security in {category}"
        };
        
        String template = sectionTemplates[sectionIndex % sectionTemplates.length];
        return template.replace("{category}", courseCategory);
    }

    public static String generateLectureTitle(String sectionTitle, int lectureIndex) {
        String[] lectureTemplates = {
            "Overview and Setup",
            "Core Concepts Explained",
            "Hands-on Implementation",
            "Common Patterns and Anti-patterns",
            "Real-world Examples",
            "Best Practices",
            "Advanced Techniques",
            "Troubleshooting Guide"
        };
        
        String template = lectureTemplates[lectureIndex % lectureTemplates.length];
        return template;
    }

    public static String generateLectureDescription(String lectureTitle) {
        return String.format("Learn %s with practical examples and detailed explanations. " +
                "This lecture covers key concepts and provides hands-on guidance.", lectureTitle.toLowerCase());
    }

    public static int generateDuration() {
        // 10 to 60 minutes
        return 10 + random.nextInt(51);
    }

    public static class CourseData {
        public final String category;
        public final String title;
        public final String subtitle;
        public final String description;
        public final String level;
        public final String language;
        public final String status;
        public final double price;
        public final long viewCount;
        public final long searchCount;
        public final boolean premium;

        public CourseData(String category, String title, String subtitle, String description,
                         String level, String language, String status, double price,
                         long viewCount, long searchCount, boolean premium) {
            this.category = category;
            this.title = title;
            this.subtitle = subtitle;
            this.description = description;
            this.level = level;
            this.language = language;
            this.status = status;
            this.price = price;
            this.viewCount = viewCount;
            this.searchCount = searchCount;
            this.premium = premium;
        }
    }
}
