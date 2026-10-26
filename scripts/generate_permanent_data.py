#!/usr/bin/env python3
"""
CyberLearnix LMS — Permanent Realistic Data Generator
======================================================
Generates deterministic, idempotent SQL seed files for ALL microservice databases.
Uses uuid5 for reproducible IDs. All INSERT statements use WHERE NOT EXISTS for safety.

Run once, then remove. Data lives permanently in PostgreSQL.
"""

import uuid
import random
import hashlib
import os
from datetime import datetime, timedelta

# Deterministic seed
random.seed(42)

OUTPUT_DIR = os.path.dirname(os.path.abspath(__file__))

# ──────────────────────────────────────────────────────────────
# Helpers
# ──────────────────────────────────────────────────────────────
def get_uuid(seed_str: str) -> str:
    return str(uuid.uuid5(uuid.NAMESPACE_URL, "cyberlearnix_permanent_" + seed_str))

# BCrypt hash of "Password13!" — pre-computed
PASSWORD_HASH = "$2a$10$IXHGtp2am7E0V2t/WiH4KeHrqqYiEV4HwKfODgOO3XIpicLdbAdUG"

def sql_escape(s: str) -> str:
    return s.replace("'", "''")

def random_date(start_year=2024, end_year=2026):
    start = datetime(start_year, 1, 1)
    end = datetime(end_year, 9, 30)
    delta = (end - start).days
    d = start + timedelta(days=random.randint(0, delta))
    return d.strftime("%Y-%m-%d %H:%M:%S")

# ──────────────────────────────────────────────────────────────
# Realistic Names & Data
# ──────────────────────────────────────────────────────────────
FIRST_NAMES = [
    "Arjun", "Priya", "Rahul", "Ananya", "Kiran", "Sneha", "Vikram", "Neha",
    "Akhil", "Meghana", "Rohan", "Sanya", "Aarav", "Isha", "Ravi", "Kavya",
    "Dev", "Tara", "Yash", "Riya", "Aditya", "Pooja", "Manish", "Divya",
    "Suresh", "Laxmi", "Amit", "Nidhi", "Harsh", "Sakshi", "Nikhil", "Ankita",
    "Siddharth", "Radhika", "Pranav", "Shreya", "Varun", "Jyoti", "Gaurav", "Pallavi",
    "Kartik", "Swati", "Akash", "Richa", "Tarun", "Sonali", "Deepak", "Megha",
    "Vivek", "Bhavna", "Ashish", "Poonam", "Kunal", "Simran", "Rajesh", "Komal",
    "Abhishek", "Tanvi", "Mohit", "Kriti", "Anuj", "Nisha", "Sahil", "Anjali",
    "Tushar", "Preeti", "Naveen", "Aparna", "Rohit", "Shruti", "Sandeep", "Ritika",
    "Mayank", "Namita", "Saurabh", "Vanshika", "Dhruv", "Aditi", "Ajay", "Fatima",
    "Omar", "Zara", "Irfan", "Sara", "Hassan", "Aisha", "Faisal", "Mariam",
    "Kabir", "Layla", "Tariq", "Noor", "Bilal", "Hina", "Imran", "Saba"
]

LAST_NAMES = [
    "Reddy", "Sharma", "Kumar", "Rao", "Varma", "Patel", "Singh", "Mehta",
    "Das", "Gupta", "Jain", "Bose", "Nair", "Iyer", "Pillai", "Menon",
    "Sen", "Roy", "Kapoor", "Chopra", "Banerjee", "Mukherjee", "Chatterjee", "Dutta",
    "Srivastava", "Tiwari", "Mishra", "Pandey", "Saxena", "Agarwal", "Chauhan", "Thakur",
    "Verma", "Yadav", "Dubey", "Shukla", "Rajput", "Bhatt", "Kulkarni", "Deshmukh",
    "Patil", "Joshi", "Kaur", "Sethi", "Malhotra", "Khanna", "Bhatia", "Tandon",
    "Khan", "Ahmed", "Syed", "Hussain", "Ali", "Sheikh", "Ansari", "Qureshi"
]

CITIES = ["Hyderabad", "Bangalore", "Mumbai", "Delhi", "Chennai", "Pune", "Kolkata", "Ahmedabad", "Jaipur", "Lucknow", "Chandigarh", "Indore", "Coimbatore", "Kochi", "Visakhapatnam", "Nagpur"]
STATES = ["Telangana", "Karnataka", "Maharashtra", "Delhi", "Tamil Nadu", "Maharashtra", "West Bengal", "Gujarat", "Rajasthan", "Uttar Pradesh", "Punjab", "Madhya Pradesh", "Tamil Nadu", "Kerala", "Andhra Pradesh", "Maharashtra"]
COUNTRIES = ["India"]
ORGANIZATIONS = ["TechCorp", "Infosys", "Wipro", "TCS", "HCL Technologies", "Google India", "Microsoft India", "Amazon India", "Flipkart", "Zoho", "Freshworks", "Razorpay", "PhonePe", "Swiggy", "Ola", "Self-Employed"]
QUALIFICATIONS = ["B.Tech", "M.Tech", "BCA", "MCA", "B.Sc CS", "M.Sc CS", "MBA", "PhD", "Diploma", "B.E."]
SKILLS_LIST = ["Java", "Python", "JavaScript", "React", "Spring Boot", "Docker", "Kubernetes", "AWS", "SQL", "PostgreSQL", "MongoDB", "Node.js", "TypeScript", "Angular", "Machine Learning", "Deep Learning", "Cyber Security", "Linux", "Git", "REST APIs"]

# ──────────────────────────────────────────────────────────────
# Course Categories & Realistic Course Data
# ──────────────────────────────────────────────────────────────
COURSE_CATALOG = [
    # (title, subtitle, category, level)
    # --- Java (30 courses) ---
    ("Java Fundamentals: From Zero to Hero", "Master the building blocks of Java programming", "Java", "BEGINNER"),
    ("Object-Oriented Programming in Java", "Design patterns, inheritance, polymorphism and more", "Java", "INTERMEDIATE"),
    ("Java Concurrency and Multithreading", "Build high-performance concurrent applications", "Java", "ADVANCED"),
    ("Java Collections Framework Deep Dive", "ArrayList, HashMap, TreeSet and custom implementations", "Java", "INTERMEDIATE"),
    ("Java Stream API Mastery", "Functional programming with Java streams and lambdas", "Java", "INTERMEDIATE"),
    ("Java Memory Management and JVM Internals", "Garbage collection, heap tuning, and profiling", "Java", "ADVANCED"),
    ("Building REST APIs with Java", "Create production-grade RESTful services", "Java", "INTERMEDIATE"),
    ("Java Design Patterns in Practice", "Gang of Four patterns with real-world examples", "Java", "ADVANCED"),
    ("Java Testing: JUnit 5 and Mockito", "Test-driven development for Java applications", "Java", "INTERMEDIATE"),
    ("Enterprise Java with Jakarta EE", "Build scalable enterprise applications", "Java", "ADVANCED"),

    # --- Spring Boot (30 courses) ---
    ("Spring Boot Essentials", "Build production-ready applications in minutes", "Spring Boot", "BEGINNER"),
    ("Spring Boot Microservices Architecture", "Design and implement microservice systems", "Spring Boot", "ADVANCED"),
    ("Spring Security: Authentication and Authorization", "Secure your Spring Boot applications", "Spring Boot", "INTERMEDIATE"),
    ("Spring Data JPA and Hibernate", "Database access made simple with Spring", "Spring Boot", "INTERMEDIATE"),
    ("Spring Boot with PostgreSQL", "Build data-driven applications with PostgreSQL", "Spring Boot", "INTERMEDIATE"),
    ("Spring Boot REST API Best Practices", "Industry-standard API design patterns", "Spring Boot", "INTERMEDIATE"),
    ("Spring Boot Testing Strategies", "Integration testing, mocking, and test containers", "Spring Boot", "INTERMEDIATE"),
    ("Spring Boot with Docker and Kubernetes", "Containerize and orchestrate Spring apps", "Spring Boot", "ADVANCED"),
    ("Spring Boot Actuator and Monitoring", "Production monitoring with Prometheus and Grafana", "Spring Boot", "ADVANCED"),
    ("Reactive Spring with WebFlux", "Non-blocking reactive programming", "Spring Boot", "ADVANCED"),

    # --- Python (30 courses) ---
    ("Python Programming Bootcamp", "Complete Python course for absolute beginners", "Python", "BEGINNER"),
    ("Advanced Python: Decorators and Metaclasses", "Deep dive into Python internals", "Python", "ADVANCED"),
    ("Python for Data Analysis", "NumPy, Pandas, and Matplotlib essentials", "Python", "INTERMEDIATE"),
    ("Python Web Development with Django", "Build full-stack web applications", "Python", "INTERMEDIATE"),
    ("Python Flask API Development", "Lightweight REST APIs with Flask", "Python", "INTERMEDIATE"),
    ("Python Automation and Scripting", "Automate repetitive tasks with Python", "Python", "BEGINNER"),
    ("Python Machine Learning Foundations", "Scikit-learn, regression, and classification", "Python", "INTERMEDIATE"),
    ("Python for Cyber Security", "Ethical hacking tools and network analysis", "Python", "ADVANCED"),
    ("Python Async Programming", "Asyncio, aiohttp, and concurrent programming", "Python", "ADVANCED"),
    ("Python Testing with PyTest", "Professional testing strategies and CI/CD", "Python", "INTERMEDIATE"),

    # --- Cyber Security (30 courses) ---
    ("Cyber Security Fundamentals", "Introduction to information security principles", "Cyber Security", "BEGINNER"),
    ("Ethical Hacking and Penetration Testing", "Learn to think like a hacker", "Cyber Security", "INTERMEDIATE"),
    ("Network Security and Firewalls", "Protect networks from modern threats", "Cyber Security", "INTERMEDIATE"),
    ("Web Application Security", "OWASP Top 10 and secure coding practices", "Cyber Security", "INTERMEDIATE"),
    ("Cryptography and Encryption", "Symmetric, asymmetric, and hashing algorithms", "Cyber Security", "ADVANCED"),
    ("Incident Response and Digital Forensics", "Investigate and respond to security breaches", "Cyber Security", "ADVANCED"),
    ("Cloud Security Essentials", "Securing AWS, Azure, and GCP environments", "Cyber Security", "INTERMEDIATE"),
    ("Security Operations Center (SOC) Analyst", "Monitor, detect, and respond to threats", "Cyber Security", "INTERMEDIATE"),
    ("Malware Analysis and Reverse Engineering", "Dissect and understand malicious software", "Cyber Security", "ADVANCED"),
    ("Compliance and Risk Management", "GDPR, HIPAA, ISO 27001 frameworks", "Cyber Security", "INTERMEDIATE"),

    # --- Docker & Kubernetes (25 courses) ---
    ("Docker for Beginners", "Containerize applications with Docker", "Docker", "BEGINNER"),
    ("Docker Compose and Multi-Container Apps", "Orchestrate development environments", "Docker", "INTERMEDIATE"),
    ("Kubernetes Fundamentals", "Container orchestration at scale", "Kubernetes", "BEGINNER"),
    ("Kubernetes in Production", "Deploy, scale, and manage production clusters", "Kubernetes", "ADVANCED"),
    ("Docker Security Best Practices", "Harden containers and images", "Docker", "ADVANCED"),
    ("Kubernetes Networking Deep Dive", "Services, Ingress, and network policies", "Kubernetes", "ADVANCED"),
    ("Helm Charts and Kubernetes Packages", "Package and deploy K8s applications", "Kubernetes", "INTERMEDIATE"),
    ("Service Mesh with Istio", "Advanced traffic management and observability", "Kubernetes", "ADVANCED"),
    ("Docker for Java Developers", "Containerize Spring Boot applications", "Docker", "INTERMEDIATE"),
    ("Container Registry and CI/CD Pipelines", "Automate container builds and deployments", "Docker", "INTERMEDIATE"),

    # --- AWS (25 courses) ---
    ("AWS Cloud Practitioner Certification Prep", "Foundation-level AWS knowledge", "AWS", "BEGINNER"),
    ("AWS Solutions Architect Associate", "Design scalable cloud architectures", "AWS", "INTERMEDIATE"),
    ("AWS EC2 and Networking", "Virtual servers and VPC configuration", "AWS", "INTERMEDIATE"),
    ("AWS Lambda and Serverless", "Build event-driven serverless applications", "AWS", "INTERMEDIATE"),
    ("AWS S3 and Storage Solutions", "Object storage, lifecycle, and security", "AWS", "BEGINNER"),
    ("AWS RDS and Database Services", "Managed databases in the cloud", "AWS", "INTERMEDIATE"),
    ("AWS DevOps and CI/CD", "CodePipeline, CodeBuild, and CodeDeploy", "AWS", "ADVANCED"),
    ("AWS Security and IAM", "Identity management and security best practices", "AWS", "INTERMEDIATE"),
    ("AWS ECS and Fargate", "Container services on AWS", "AWS", "ADVANCED"),
    ("AWS Cost Optimization", "Reduce cloud spending without sacrificing performance", "AWS", "INTERMEDIATE"),

    # --- React & Frontend (25 courses) ---
    ("React Fundamentals", "Build modern user interfaces with React", "React", "BEGINNER"),
    ("Advanced React Patterns", "Render props, HOCs, and custom hooks", "React", "ADVANCED"),
    ("React with TypeScript", "Type-safe React applications", "React", "INTERMEDIATE"),
    ("Next.js Full Stack Development", "Server-side rendering and static generation", "React", "INTERMEDIATE"),
    ("React State Management with Redux", "Predictable state management for complex apps", "React", "INTERMEDIATE"),
    ("React Testing Library and Jest", "Test React components with confidence", "React", "INTERMEDIATE"),
    ("React Native Mobile Development", "Cross-platform mobile apps with React", "React", "INTERMEDIATE"),
    ("React Performance Optimization", "Memoization, lazy loading, and profiling", "React", "ADVANCED"),
    ("Tailwind CSS with React", "Utility-first styling for React applications", "React", "BEGINNER"),
    ("React Authentication and Authorization", "JWT, OAuth2, and session management", "React", "INTERMEDIATE"),

    # --- Data Structures & Algorithms (25 courses) ---
    ("Data Structures and Algorithms in Java", "Arrays, linked lists, trees, and graphs", "Data Structures", "BEGINNER"),
    ("Algorithm Design and Analysis", "Sorting, searching, and complexity analysis", "Data Structures", "INTERMEDIATE"),
    ("Dynamic Programming Masterclass", "Solve complex optimization problems", "Data Structures", "ADVANCED"),
    ("Graph Algorithms and Network Flow", "BFS, DFS, Dijkstra, and minimum spanning trees", "Data Structures", "ADVANCED"),
    ("Competitive Programming with Java", "Prepare for coding competitions", "Data Structures", "ADVANCED"),
    ("System Design Interview Preparation", "Design scalable systems for tech interviews", "Data Structures", "ADVANCED"),
    ("Binary Trees and Binary Search Trees", "Traversals, balancing, and operations", "Data Structures", "INTERMEDIATE"),
    ("Hash Tables and Hashing Techniques", "Collision resolution and perfect hashing", "Data Structures", "INTERMEDIATE"),
    ("Recursion and Backtracking", "Solve problems using recursive thinking", "Data Structures", "INTERMEDIATE"),
    ("Heap and Priority Queue Implementations", "Min-heap, max-heap, and applications", "Data Structures", "INTERMEDIATE"),

    # --- SQL & PostgreSQL (25 courses) ---
    ("SQL for Beginners", "Write your first SQL queries with confidence", "SQL", "BEGINNER"),
    ("Advanced SQL Queries and Optimization", "Window functions, CTEs, and query tuning", "SQL", "ADVANCED"),
    ("PostgreSQL Administration", "Install, configure, and manage PostgreSQL", "PostgreSQL", "INTERMEDIATE"),
    ("Database Design and Normalization", "Design efficient relational databases", "SQL", "INTERMEDIATE"),
    ("PostgreSQL Performance Tuning", "EXPLAIN ANALYZE, indexing, and vacuuming", "PostgreSQL", "ADVANCED"),
    ("SQL for Data Analytics", "Aggregate functions and analytical queries", "SQL", "INTERMEDIATE"),
    ("PostgreSQL Replication and High Availability", "Streaming replication and failover", "PostgreSQL", "ADVANCED"),
    ("NoSQL vs SQL: When to Use What", "Choosing the right database for your project", "SQL", "BEGINNER"),
    ("PostgreSQL JSON and Full-Text Search", "Advanced PostgreSQL features", "PostgreSQL", "INTERMEDIATE"),
    ("Database Migration Strategies", "Zero-downtime schema migrations", "SQL", "ADVANCED"),

    # --- AI/ML (25 courses) ---
    ("Machine Learning Fundamentals", "Supervised and unsupervised learning basics", "AI/ML", "BEGINNER"),
    ("Deep Learning with TensorFlow", "Neural networks, CNNs, and RNNs", "AI/ML", "INTERMEDIATE"),
    ("Natural Language Processing", "Text classification, NER, and sentiment analysis", "AI/ML", "ADVANCED"),
    ("Computer Vision with OpenCV", "Image processing and object detection", "AI/ML", "INTERMEDIATE"),
    ("Reinforcement Learning", "Q-learning and policy gradient methods", "AI/ML", "ADVANCED"),
    ("MLOps and Model Deployment", "Deploy ML models to production", "AI/ML", "ADVANCED"),
    ("Generative AI and Large Language Models", "Understanding GPT, BERT, and transformers", "AI/ML", "INTERMEDIATE"),
    ("Data Engineering for ML Pipelines", "ETL, feature stores, and data quality", "AI/ML", "INTERMEDIATE"),
    ("AI Ethics and Responsible AI", "Bias, fairness, and transparency in AI", "AI/ML", "BEGINNER"),
    ("Time Series Forecasting", "ARIMA, Prophet, and LSTM for predictions", "AI/ML", "ADVANCED"),

    # --- Microservices (20 courses) ---
    ("Microservices Architecture Fundamentals", "Design principles and decomposition strategies", "Microservices", "BEGINNER"),
    ("Event-Driven Architecture with Kafka", "Build asynchronous microservice communication", "Microservices", "ADVANCED"),
    ("API Gateway Patterns", "Rate limiting, routing, and authentication", "Microservices", "INTERMEDIATE"),
    ("Microservices with gRPC", "High-performance inter-service communication", "Microservices", "ADVANCED"),
    ("Distributed Tracing with Jaeger", "End-to-end request tracing", "Microservices", "INTERMEDIATE"),
    ("Circuit Breaker and Resilience Patterns", "Build fault-tolerant distributed systems", "Microservices", "ADVANCED"),
    ("Saga Pattern for Distributed Transactions", "Managing data consistency across services", "Microservices", "ADVANCED"),
    ("Service Discovery with Consul", "Dynamic service registration and discovery", "Microservices", "INTERMEDIATE"),
    ("Microservices Security Patterns", "OAuth2, JWT, and API keys", "Microservices", "INTERMEDIATE"),
    ("Microservices Testing Strategies", "Contract testing and consumer-driven contracts", "Microservices", "INTERMEDIATE"),

    # --- Node.js (20 courses) ---
    ("Node.js Fundamentals", "Server-side JavaScript from scratch", "Node.js", "BEGINNER"),
    ("Express.js REST API Development", "Build production APIs with Express", "Node.js", "INTERMEDIATE"),
    ("Node.js with MongoDB", "Full-stack JavaScript with NoSQL", "Node.js", "INTERMEDIATE"),
    ("Node.js Microservices", "Build scalable Node.js microservice systems", "Node.js", "ADVANCED"),
    ("GraphQL with Node.js", "Build flexible APIs with GraphQL", "Node.js", "INTERMEDIATE"),
    ("Node.js Performance and Scalability", "Clustering, caching, and optimization", "Node.js", "ADVANCED"),
    ("Real-Time Applications with Socket.IO", "WebSockets and event-driven communication", "Node.js", "INTERMEDIATE"),
    ("Node.js Authentication with Passport.js", "OAuth, JWT, and session-based auth", "Node.js", "INTERMEDIATE"),
    ("Deno and Node.js Comparison", "Next-generation JavaScript runtimes", "Node.js", "INTERMEDIATE"),
    ("Node.js with TypeScript", "Type-safe server-side JavaScript", "Node.js", "INTERMEDIATE"),

    # --- Additional courses to hit 360 ---
    ("Linux System Administration", "Essential Linux skills for developers", "Linux", "BEGINNER"),
    ("Git and GitHub Mastery", "Version control for professional developers", "Git", "BEGINNER"),
    ("DevOps Engineering Bootcamp", "CI/CD, infrastructure as code, and monitoring", "DevOps", "INTERMEDIATE"),
    ("Agile and Scrum Methodology", "Manage projects with agile frameworks", "Agile", "BEGINNER"),
    ("Technical Writing for Developers", "Write clear documentation and tutorials", "Technical Writing", "BEGINNER"),
    ("MongoDB Fundamentals", "Document databases and NoSQL patterns", "MongoDB", "BEGINNER"),
    ("Redis for Caching and Sessions", "In-memory data structures and use cases", "Redis", "INTERMEDIATE"),
    ("Elasticsearch and Log Analysis", "Full-text search and analytics", "Elasticsearch", "INTERMEDIATE"),
    ("Terraform Infrastructure as Code", "Provision cloud resources declaratively", "DevOps", "INTERMEDIATE"),
    ("Jenkins CI/CD Pipelines", "Automate build, test, and deploy workflows", "DevOps", "INTERMEDIATE"),
]

CATEGORY_THUMBNAILS = {
    "Java": "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=1400&q=85",
    "Spring Boot": "https://images.unsplash.com/photo-1555949963-aa79dcee981c?auto=format&fit=crop&w=1400&q=85",
    "Microservices": "https://images.unsplash.com/photo-1558494949-ef010cbdcc31?auto=format&fit=crop&w=1400&q=85",
    "Python": "https://images.unsplash.com/photo-1526379095098-d400fd0bf935?auto=format&fit=crop&w=1400&q=85",
    "Cyber Security": "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?auto=format&fit=crop&w=1400&q=85",
    "Data Structures": "https://images.unsplash.com/photo-1515879218367-8466d910aaa4?auto=format&fit=crop&w=1400&q=85",
    "SQL": "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?auto=format&fit=crop&w=1400&q=85",
    "PostgreSQL": "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?auto=format&fit=crop&w=1400&q=85",
    "Docker": "https://images.unsplash.com/photo-1667372393119-3d4c48d07fc9?auto=format&fit=crop&w=1400&q=85",
    "Kubernetes": "https://images.unsplash.com/photo-1667372393119-3d4c48d07fc9?auto=format&fit=crop&w=1400&q=85",
    "AWS": "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=1400&q=85",
    "React": "https://images.unsplash.com/photo-1633356122544-f134324a6cee?auto=format&fit=crop&w=1400&q=85",
    "Node.js": "https://images.unsplash.com/photo-1627398242454-45a1465c2479?auto=format&fit=crop&w=1400&q=85",
    "AI/ML": "https://images.unsplash.com/photo-1620712943543-bcc4688e7485?auto=format&fit=crop&w=1400&q=85",
    "Linux": "https://images.unsplash.com/photo-1629654297299-c8506221ca97?auto=format&fit=crop&w=1400&q=85",
    "Git": "https://images.unsplash.com/photo-1618401471353-b98afee0b2eb?auto=format&fit=crop&w=1400&q=85",
    "DevOps": "https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=1400&q=85",
    "Agile": "https://images.unsplash.com/photo-1552664730-d307ca884978?auto=format&fit=crop&w=1400&q=85",
    "Technical Writing": "https://images.unsplash.com/photo-1455390582262-044cdead277a?auto=format&fit=crop&w=1400&q=85",
    "MongoDB": "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?auto=format&fit=crop&w=1400&q=85",
    "Redis": "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?auto=format&fit=crop&w=1400&q=85",
    "Elasticsearch": "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?auto=format&fit=crop&w=1400&q=85",
}

SECTION_TEMPLATES = [
    "Getting Started with {topic}",
    "Core Concepts of {topic}",
    "Working with {topic} in Practice",
    "Advanced {topic} Techniques",
    "Real-World {topic} Projects",
    "Testing and Debugging {topic}",
    "Performance Optimization in {topic}",
    "Deployment and Production {topic}",
]

LECTURE_TEMPLATES = [
    "Introduction to {topic}",
    "Setting Up Your {topic} Environment",
    "Understanding {topic} Basics",
    "Hands-On {topic} Exercise",
    "Deep Dive: {topic} Internals",
    "Best Practices for {topic}",
    "Common {topic} Mistakes to Avoid",
    "Building a {topic} Demo Project",
    "{topic} Code Walkthrough",
    "{topic} Q&A and Summary",
]

REVIEW_COMMENTS = [
    "Excellent course! The instructor explains concepts very clearly. I learned a lot and feel confident applying these skills at work.",
    "Very well structured and practical. The projects really helped solidify my understanding. Highly recommended!",
    "Good content but could use more real-world examples. The theoretical foundation is strong though.",
    "One of the best courses I have taken on this platform. The hands-on labs are fantastic.",
    "The instructor is very knowledgeable and responsive to questions. Great learning experience overall.",
    "Solid course with comprehensive coverage. The pace is just right for intermediate learners.",
    "Amazing depth of content. I have been working in this field for 3 years and still learned new things.",
    "Well organized curriculum. Each section builds on the previous one logically.",
    "The projects at the end of each section really help cement the concepts. Worth every penny.",
    "Great course for beginners. The instructor takes time to explain everything step by step.",
    "Thorough coverage of the topic. I appreciate the attention to real-world scenarios.",
    "This course helped me prepare for my certification exam. Very focused and relevant content.",
    "The code examples are clean and well-commented. Easy to follow along and practice.",
    "I wish there were more advanced topics covered, but for the target audience it is perfect.",
    "Engaging teaching style with practical demonstrations. Makes complex topics accessible.",
    "Completed this course in two weeks and already applying concepts at my job. Excellent ROI.",
    "The instructor provides excellent supplementary resources and reading materials.",
    "Clear explanations, good pacing, and useful assignments. A well-rounded learning experience.",
    "This course gave me the confidence to start building production applications. Thank you!",
    "Comprehensive and up-to-date content. The instructor clearly keeps the material current.",
]

NOTIFICATION_MESSAGES = [
    ("Welcome to CyberLearnix!", "Welcome aboard! Start exploring our courses and begin your learning journey today."),
    ("Course Enrollment Confirmed", "You have been successfully enrolled. Access your course materials now."),
    ("New Course Available", "A new course matching your interests has been published. Check it out!"),
    ("Assignment Due Reminder", "You have an upcoming assignment deadline. Don't forget to submit your work."),
    ("Certificate Ready", "Congratulations! Your course completion certificate is ready for download."),
    ("Weekly Progress Report", "Here is your weekly learning summary. You are making great progress!"),
    ("Special Offer", "Limited time offer! Get 30% off on select courses. Use code LEARN30."),
    ("Course Update", "Your enrolled course has been updated with new content. Check the latest lectures."),
    ("Platform Maintenance", "Scheduled maintenance on Sunday 2 AM to 4 AM IST. Brief downtime expected."),
    ("Community Discussion", "New discussion topic in your course forum. Join the conversation!"),
]

COUPON_DATA = [
    ("WELCOME25", "Welcome Discount", "Get 25% off your first course", "PERCENTAGE", 25, 0, 5000, 100, 1),
    ("LEARN30", "Learning Festival", "30% off on all courses", "PERCENTAGE", 30, 500, 3000, 50, 1),
    ("CYBER50", "Cyber Security Special", "50% off cyber security courses", "PERCENTAGE", 50, 999, 5000, 30, 1),
    ("FLAT200", "Flat 200 Off", "Flat Rs 200 off on orders above Rs 999", "FIXED", 200, 999, 200, 200, 2),
    ("FLAT500", "Premium Discount", "Flat Rs 500 off on orders above Rs 1999", "FIXED", 500, 1999, 500, 100, 1),
    ("JAVA20", "Java Learners", "20% off on Java courses", "PERCENTAGE", 20, 499, 2000, 75, 2),
    ("SPRING15", "Spring Boot Special", "15% off Spring Boot courses", "PERCENTAGE", 15, 499, 1500, 60, 2),
    ("DEVOPS40", "DevOps Discount", "40% off DevOps and Docker courses", "PERCENTAGE", 40, 799, 4000, 40, 1),
    ("NEWUSER", "New User Offer", "Get Rs 300 off as a new user", "FIXED", 300, 999, 300, 500, 1),
    ("PYTHON25", "Python Special", "25% off Python courses", "PERCENTAGE", 25, 499, 2500, 80, 2),
    ("AWS35", "AWS Cloud Discount", "35% off AWS certification prep", "PERCENTAGE", 35, 999, 3500, 45, 1),
    ("REACT20", "React Special Offer", "20% off React courses", "PERCENTAGE", 20, 499, 2000, 70, 2),
    ("DIWALI60", "Diwali Festival Sale", "60% off all courses", "PERCENTAGE", 60, 299, 6000, 200, 1),
    ("NEWYEAR40", "New Year Special", "40% off to start the year right", "PERCENTAGE", 40, 499, 4000, 150, 1),
    ("STUDENT10", "Student Discount", "10% off for students", "PERCENTAGE", 10, 0, 1000, 300, 1),
    ("BUNDLE500", "Bundle Offer", "Rs 500 off when buying 3 or more courses", "FIXED", 500, 2999, 500, 50, 1),
    ("FLASH75", "Flash Sale", "75% off for 24 hours only", "PERCENTAGE", 75, 499, 7500, 20, 1),
    ("REFERRAL200", "Referral Bonus", "Rs 200 off for referred users", "FIXED", 200, 499, 200, 1000, 1),
    ("UPGRADE30", "Upgrade Discount", "30% off on advanced courses", "PERCENTAGE", 30, 999, 3000, 60, 1),
    ("LOYALTY15", "Loyalty Reward", "15% off for returning learners", "PERCENTAGE", 15, 299, 1500, 100, 2),
]

INSTRUCTOR_HEADLINES = [
    "Senior Java Developer & Cloud Architect",
    "Full-Stack Developer & Technical Lead",
    "Cyber Security Consultant & CISA",
    "AI/ML Engineer & Research Scientist",
    "DevOps Engineer & SRE Specialist",
    "Database Administrator & Performance Tuner",
    "Cloud Solutions Architect (AWS Certified)",
    "Frontend Architect & UX Engineer",
    "Distributed Systems Engineer",
    "Data Engineer & Analytics Expert",
    "Backend Developer & API Specialist",
    "Kubernetes Expert & Platform Engineer",
    "Python Developer & Automation Specialist",
    "Security Researcher & Ethical Hacker",
    "Principal Software Engineer",
    "Technical Trainer & Course Author",
    "System Design Expert & Tech Mentor",
    "React Expert & Frontend Developer",
    "Microservices Architect",
    "Machine Learning Engineer",
]

# ──────────────────────────────────────────────────────────────
# Generate Student Data (80 students)
# ──────────────────────────────────────────────────────────────
NUM_STUDENTS = 80
NUM_INSTRUCTORS = 20

students = []
for i in range(1, NUM_STUDENTS + 1):
    email = f"student{i:02d}@cyberlearnix.test"
    uid = get_uuid(email)
    fname = FIRST_NAMES[(i - 1) % len(FIRST_NAMES)]
    lname = LAST_NAMES[(i - 1) % len(LAST_NAMES)]
    city_idx = (i - 1) % len(CITIES)
    students.append({
        "id": uid,
        "email": email,
        "firstName": fname,
        "lastName": lname,
        "city": CITIES[city_idx],
        "state": STATES[city_idx],
        "country": "India",
        "organization": ORGANIZATIONS[(i - 1) % len(ORGANIZATIONS)],
        "qualification": QUALIFICATIONS[(i - 1) % len(QUALIFICATIONS)],
        "skills": ", ".join(random.sample(SKILLS_LIST, min(5, len(SKILLS_LIST)))),
        "dob": f"{random.randint(1988, 2002)}-{random.randint(1,12):02d}-{random.randint(1,28):02d}",
    })

instructors = []
for i in range(1, NUM_INSTRUCTORS + 1):
    email = f"instructor{i:02d}@cyberlearnix.test"
    uid = get_uuid(email)
    user_uid = get_uuid(f"instructor_user_{email}")
    fname = FIRST_NAMES[(i + NUM_STUDENTS - 1) % len(FIRST_NAMES)]
    lname = LAST_NAMES[(i + NUM_STUDENTS - 1) % len(LAST_NAMES)]
    instructors.append({
        "id": uid,
        "userId": user_uid,
        "email": email,
        "firstName": fname,
        "lastName": lname,
        "headline": INSTRUCTOR_HEADLINES[(i - 1) % len(INSTRUCTOR_HEADLINES)],
        "rating": round(random.uniform(3.8, 5.0), 1),
    })

# Assign courses to instructors (distribute evenly)
courses = []
for idx, (title, subtitle, category, level) in enumerate(COURSE_CATALOG):
    cid = (idx + 1) * 100  # Course IDs: 100, 200, 300, ...
    inst = instructors[idx % NUM_INSTRUCTORS]
    # Status distribution: 80% Published, 12% Draft, 8% Archived
    if idx < int(len(COURSE_CATALOG) * 0.80):
        status = "Published"
    elif idx < int(len(COURSE_CATALOG) * 0.92):
        status = "Draft"
    else:
        status = "Archived"
    price = random.choice([0, 0, 499, 699, 799, 999, 1299, 1499, 1999, 2499, 2999, 3499, 3999])
    courses.append({
        "id": cid,
        "title": title,
        "subtitle": subtitle,
        "description": f"Comprehensive course on {title.lower()}. {subtitle}. "
                       f"Learn from industry experts with hands-on projects, real-world case studies, "
                       f"and practical assignments. Perfect for {level.lower()} learners looking to "
                       f"advance their career in {category}.",
        "category": category,
        "level": level,
        "language": "English",
        "price": price,
        "thumbnail": CATEGORY_THUMBNAILS.get(category, CATEGORY_THUMBNAILS["Java"]),
        "instructorId": inst["userId"],
        "instructorEmail": inst["email"],
        "instructorIdx": idx % NUM_INSTRUCTORS,
        "status": status,
        "premium": price >= 2000,
        "searchCount": random.randint(50, 15000),
        "viewCount": random.randint(100, 50000),
    })

published_courses = [c for c in courses if c["status"] == "Published"]

# ──────────────────────────────────────────────────────────────
# WRITE: lms_user_db — users table
# ──────────────────────────────────────────────────────────────
def write_users_sql():
    lines = ["BEGIN;", ""]
    
    # Students
    for s in students:
        lines.append(f"""INSERT INTO users (id, first_name, last_name, email, password, role, status, dob, city, state, country, organization, skills, highest_qualification, field_of_study, preferred_language, failed_login_attempts, is_instructor_approved, mobile_hash, created_at, updated_at)
SELECT '{s["id"]}', '{sql_escape(s["firstName"])}', '{sql_escape(s["lastName"])}', '{s["email"]}', '{PASSWORD_HASH}', 'STUDENT', 'ACTIVE', '{s["dob"]}', '{s["city"]}', '{s["state"]}', '{s["country"]}', '{sql_escape(s["organization"])}', '{sql_escape(s["skills"])}', '{s["qualification"]}', 'Computer Science', 'en', 0, false, '', '{random_date()}', '{random_date()}'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = '{s["email"]}');
""")

    # Instructor users (in user-service they also need a user record)
    for inst in instructors:
        lines.append(f"""INSERT INTO users (id, first_name, last_name, email, password, role, status, applied_role, application_status, is_instructor_approved, failed_login_attempts, mobile_hash, city, state, country, preferred_language, created_at, updated_at)
SELECT '{inst["userId"]}', '{sql_escape(inst["firstName"])}', '{sql_escape(inst["lastName"])}', '{inst["email"]}', '{PASSWORD_HASH}', 'INSTRUCTOR', 'ACTIVE', 'INSTRUCTOR', 'APPROVED', true, 0, '', 'Hyderabad', 'Telangana', 'India', 'en', '{random_date()}', '{random_date()}'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = '{inst["email"]}');
""")
    
    lines.append("COMMIT;")
    path = os.path.join(OUTPUT_DIR, "..", "seed_permanent_users.sql")
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"  ✓ {len(students)} students + {len(instructors)} instructor users → seed_permanent_users.sql")

# ──────────────────────────────────────────────────────────────
# WRITE: lms_instructor_db — instructor + course tables
# ──────────────────────────────────────────────────────────────
def write_instructors_sql():
    lines = ["BEGIN;", ""]
    
    # Insert instructors
    for inst in instructors:
        lines.append(f"""INSERT INTO instructor (id, user_id, name, email, headline, rating, verified)
SELECT '{inst["id"]}', '{inst["userId"]}', '{sql_escape(inst["firstName"])} {sql_escape(inst["lastName"])}', '{inst["email"]}', '{sql_escape(inst["headline"])}', {inst["rating"]}, true
WHERE NOT EXISTS (SELECT 1 FROM instructor WHERE email = '{inst["email"]}');
""")
    
    # Insert courses into instructor-service course table
    for c in courses:
        inst = instructors[c["instructorIdx"]]
        inst_status = "PUBLISHED" if c["status"] == "Published" else "DRAFT" if c["status"] == "Draft" else "ARCHIVED"
        slug = c["title"].lower().replace(" ", "-").replace(":", "").replace(",", "").replace("&", "and")[:80]
        lines.append(f"""INSERT INTO course (id, title, subtitle, description, price, category, level, language, status, slug, thumbnail_url, instructor_id, course_service_id, sync_status, created_at, updated_at)
SELECT {c["id"]}, '{sql_escape(c["title"])}', '{sql_escape(c["subtitle"])}', '{sql_escape(c["description"][:250])}', {c["price"]}, '{sql_escape(c["category"])}', '{c["level"]}', '{c["language"]}', '{inst_status}', '{sql_escape(slug)}', '{c["thumbnail"]}', '{inst["id"]}', {c["id"]}, 'SYNCED', '{random_date()}', '{random_date()}'
WHERE NOT EXISTS (SELECT 1 FROM course WHERE id = {c["id"]});
""")
    
    lines.append("COMMIT;")
    path = os.path.join(OUTPUT_DIR, "..", "seed_permanent_instructors.sql")
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"  ✓ {len(instructors)} instructors + {len(courses)} instructor-courses → seed_permanent_instructors.sql")

# ──────────────────────────────────────────────────────────────
# WRITE: lms_course_db — courses, sections, lectures
# ──────────────────────────────────────────────────────────────
def write_courses_sql():
    lines = ["BEGIN;", ""]
    
    section_count = 0
    lecture_count = 0
    
    for c in courses:
        thumbnail = c["thumbnail"]
        lines.append(f"""INSERT INTO courses (id, title, subtitle, description, category, level, language, price, thumbnail, instructor_id, status, premium, search_count, view_count)
SELECT {c["id"]}, '{sql_escape(c["title"])}', '{sql_escape(c["subtitle"])}', '{sql_escape(c["description"])}', '{sql_escape(c["category"])}', '{c["level"]}', '{c["language"]}', {c["price"]}, '{thumbnail}', '{c["instructorId"]}', '{c["status"]}', {str(c["premium"]).lower()}, {c["searchCount"]}, {c["viewCount"]}
WHERE NOT EXISTS (SELECT 1 FROM courses WHERE id = {c["id"]});
""")
        
        # Sections & Lectures for Published and Draft courses
        if c["status"] in ("Published", "Draft"):
            num_sections = random.randint(3, 6)
            topic = c["category"]
            for s_idx in range(1, num_sections + 1):
                template = SECTION_TEMPLATES[(s_idx - 1) % len(SECTION_TEMPLATES)]
                section_title = template.format(topic=topic)
                section_count += 1
                lines.append(f"""INSERT INTO sections (course_id, title, order_index)
SELECT {c["id"]}, '{sql_escape(section_title)}', {s_idx}
WHERE NOT EXISTS (SELECT 1 FROM sections WHERE course_id = {c["id"]} AND order_index = {s_idx});
""")
                # Lectures per section
                num_lectures = random.randint(3, 5)
                for l_idx in range(1, num_lectures + 1):
                    l_template = LECTURE_TEMPLATES[(l_idx - 1) % len(LECTURE_TEMPLATES)]
                    lecture_title = l_template.format(topic=topic)
                    duration = random.choice([8, 10, 12, 15, 18, 20, 25, 30, 35, 45])
                    ltype = random.choices(["VIDEO", "ARTICLE", "DOCUMENT"], weights=[80, 15, 5])[0]
                    preview = "true" if l_idx == 1 and s_idx == 1 else "false"
                    lecture_count += 1
                    lines.append(f"""INSERT INTO lectures (section_id, title, description, duration, order_index, type, preview_enabled, video_url)
SELECT s.id, '{sql_escape(lecture_title)}', 'Learn about {sql_escape(lecture_title.lower())} with practical examples and exercises.', {duration}, {l_idx}, '{ltype}', {preview}, 'https://storage.cyberlearnix.test/videos/course-{c["id"]}/s{s_idx}-l{l_idx}.mp4'
FROM sections s WHERE s.course_id = {c["id"]} AND s.order_index = {s_idx}
AND NOT EXISTS (SELECT 1 FROM lectures l2 JOIN sections s2 ON l2.section_id = s2.id WHERE s2.course_id = {c["id"]} AND s2.order_index = {s_idx} AND l2.order_index = {l_idx});
""")

    lines.append("COMMIT;")
    path = os.path.join(OUTPUT_DIR, "..", "seed_permanent_courses.sql")
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"  ✓ {len(courses)} courses, ~{section_count} sections, ~{lecture_count} lectures → seed_permanent_courses.sql")

# ──────────────────────────────────────────────────────────────
# WRITE: lms_course_db — enrollments
# ──────────────────────────────────────────────────────────────
def write_enrollments_sql():
    lines = ["BEGIN;", ""]
    enrollment_count = 0
    
    for s in students:
        num_enrollments = random.randint(2, 8)
        enrolled_courses = random.sample(published_courses, min(num_enrollments, len(published_courses)))
        for c in enrolled_courses:
            progress = random.choice([0, 5, 10, 15, 20, 25, 30, 40, 50, 60, 70, 75, 80, 85, 90, 95, 100])
            status = "COMPLETED" if progress == 100 else "ACTIVE"
            enrolled_at = random_date()
            completed_at = f"'{random_date()}'" if status == "COMPLETED" else "NULL"
            enrollment_count += 1
            lines.append(f"""INSERT INTO enrollment (course_id, student_id, student_name, progress, status, enrolled_at, completed_at)
SELECT {c["id"]}, '{s["id"]}', '{sql_escape(s["firstName"])} {sql_escape(s["lastName"])}', {progress}, '{status}', '{enrolled_at}', {completed_at}
WHERE NOT EXISTS (SELECT 1 FROM enrollment WHERE course_id = {c["id"]} AND student_id = '{s["id"]}');
""")
    
    lines.append("COMMIT;")
    path = os.path.join(OUTPUT_DIR, "..", "seed_permanent_enrollments.sql")
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"  ✓ {enrollment_count} enrollments → seed_permanent_enrollments.sql")
    return enrollment_count

# ──────────────────────────────────────────────────────────────
# WRITE: lms_course_db — lecture_progress
# ──────────────────────────────────────────────────────────────
def write_progress_sql():
    lines = ["BEGIN;", ""]
    progress_count = 0
    
    # For a subset of students, create lecture progress on their enrolled courses
    for s in students[:50]:
        num_courses = random.randint(1, 4)
        progress_courses = random.sample(published_courses, min(num_courses, len(published_courses)))
        for c in progress_courses:
            # Generate progress for a few lectures in this course
            num_lectures_done = random.randint(1, 10)
            for lecture_offset in range(1, num_lectures_done + 1):
                # Use a deterministic lecture_id based on course
                fake_lecture_id = c["id"] * 100 + lecture_offset
                status = random.choice(["NOT_STARTED", "IN_PROGRESS", "COMPLETED"])
                pct = 0 if status == "NOT_STARTED" else (random.randint(10, 90) if status == "IN_PROGRESS" else 100)
                started = f"'{random_date()}'" if status != "NOT_STARTED" else "NULL"
                completed = f"'{random_date()}'" if status == "COMPLETED" else "NULL"
                time_spent = random.randint(60, 2400) if status != "NOT_STARTED" else 0
                progress_count += 1
                lines.append(f"""INSERT INTO lecture_progress (student_id, course_id, lecture_id, status, progress_percentage, started_at, completed_at, last_accessed_at, time_spent_seconds)
SELECT '{s["id"]}', {c["id"]}, {fake_lecture_id}, '{status}', {pct}, {started}, {completed}, '{random_date()}', {time_spent}
WHERE NOT EXISTS (SELECT 1 FROM lecture_progress WHERE student_id = '{s["id"]}' AND lecture_id = {fake_lecture_id});
""")
    
    lines.append("COMMIT;")
    path = os.path.join(OUTPUT_DIR, "..", "seed_permanent_progress.sql")
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"  ✓ {progress_count} progress records → seed_permanent_progress.sql")

# ──────────────────────────────────────────────────────────────
# WRITE: lms_wishlist_db — wishlist_items
# ──────────────────────────────────────────────────────────────
def write_wishlists_sql():
    lines = ["BEGIN;", ""]
    count = 0
    
    for s in students[:60]:
        num_wishes = random.randint(2, 6)
        wished = random.sample(published_courses, min(num_wishes, len(published_courses)))
        for c in wished:
            wish_id = get_uuid(f"wish_{s['id']}_{c['id']}")
            count += 1
            lines.append(f"""INSERT INTO wishlist_items (id, user_id, course_id, added_at)
SELECT '{wish_id}', '{s["id"]}', {c["id"]}, '{random_date()}'
WHERE NOT EXISTS (SELECT 1 FROM wishlist_items WHERE user_id = '{s["id"]}' AND course_id = {c["id"]});
""")
    
    lines.append("COMMIT;")
    path = os.path.join(OUTPUT_DIR, "..", "seed_permanent_wishlists.sql")
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"  ✓ {count} wishlist items → seed_permanent_wishlists.sql")

# ──────────────────────────────────────────────────────────────
# WRITE: lms_cart_db — cart_items
# ──────────────────────────────────────────────────────────────
def write_carts_sql():
    lines = ["BEGIN;", ""]
    count = 0
    
    for s in students[:40]:
        num_cart = random.randint(1, 4)
        carted = random.sample(published_courses, min(num_cart, len(published_courses)))
        for c in carted:
            inst = instructors[c["instructorIdx"]]
            count += 1
            lines.append(f"""INSERT INTO cart_items (user_id, instructor_id, course_id, course_name, price, quantity)
SELECT '{s["id"]}', '{inst["userId"]}', {c["id"]}, '{sql_escape(c["title"])}', {c["price"]}, 1
WHERE NOT EXISTS (SELECT 1 FROM cart_items WHERE user_id = '{s["id"]}' AND course_id = {c["id"]});
""")
    
    lines.append("COMMIT;")
    path = os.path.join(OUTPUT_DIR, "..", "seed_permanent_carts.sql")
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"  ✓ {count} cart items → seed_permanent_carts.sql")

# ──────────────────────────────────────────────────────────────
# WRITE: lms_review_db — reviews
# ──────────────────────────────────────────────────────────────
def write_reviews_sql():
    lines = ["BEGIN;", ""]
    count = 0
    
    for s in students[:65]:
        num_reviews = random.randint(2, 6)
        reviewed = random.sample(published_courses, min(num_reviews, len(published_courses)))
        for c in reviewed:
            review_uuid = get_uuid(f"review_{s['id']}_{c['id']}")
            rating = random.choices([3, 4, 5], weights=[15, 35, 50])[0]
            comment = sql_escape(random.choice(REVIEW_COMMENTS))
            created = random_date()
            count += 1
            lines.append(f"""INSERT INTO reviews (review_uuid, user_id, course_id, rating, comment, status, created_at, updated_at)
SELECT '{review_uuid}', '{s["id"]}', {c["id"]}, {rating}, '{comment}', 'ACTIVE', '{created}', '{created}'
WHERE NOT EXISTS (SELECT 1 FROM reviews WHERE user_id = '{s["id"]}' AND course_id = {c["id"]});
""")
    
    lines.append("COMMIT;")
    path = os.path.join(OUTPUT_DIR, "..", "seed_permanent_reviews.sql")
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"  ✓ {count} reviews → seed_permanent_reviews.sql")

# ──────────────────────────────────────────────────────────────
# WRITE: lms_coupon_db — coupons
# ──────────────────────────────────────────────────────────────
def write_coupons_sql():
    lines = ["BEGIN;", ""]
    
    for code, title, desc, dtype, dval, minorder, maxdisc, usage_limit, per_user in COUPON_DATA:
        cid = get_uuid(f"coupon_{code}")
        start = "2024-01-01 00:00:00"
        end = "2027-12-31 23:59:59"
        lines.append(f"""INSERT INTO coupons (id, code, title, description, discount_type, discount_value, minimum_order_amount, maximum_discount_amount, min_purchase, usage_limit, per_user_limit, used_count, is_active, status, stackable, start_time, end_time, expiry_date, created_at, created_by_role)
SELECT '{cid}', '{code}', '{sql_escape(title)}', '{sql_escape(desc)}', '{dtype}', {dval}, {minorder}, {maxdisc}, {minorder}, {usage_limit}, {per_user}, 0, true, 'ACTIVE', false, '{start}', '{end}', '{end}', '{random_date()}', 'MAIN_ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM coupons WHERE code = '{code}');
""")
    
    lines.append("COMMIT;")
    path = os.path.join(OUTPUT_DIR, "..", "seed_permanent_coupons.sql")
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"  ✓ {len(COUPON_DATA)} coupons → seed_permanent_coupons.sql")

# ──────────────────────────────────────────────────────────────
# WRITE: lms_order_db — orders + order_item
# ──────────────────────────────────────────────────────────────
def write_orders_sql():
    lines = ["BEGIN;", ""]
    order_count = 0
    order_item_count = 0
    
    for s in students[:50]:
        num_orders = random.randint(1, 4)
        for oidx in range(num_orders):
            order_id = get_uuid(f"order_{s['id']}_{oidx}")
            num_items = random.randint(1, 3)
            ordered = random.sample(published_courses, min(num_items, len(published_courses)))
            total = sum(c["price"] for c in ordered)
            status = random.choice(["COMPLETED", "COMPLETED", "COMPLETED", "PENDING", "CANCELLED"])
            order_count += 1
            lines.append(f"""INSERT INTO orders (order_id, user_id, total_amount, status, created_at)
SELECT '{order_id}', '{s["id"]}', {total}, '{status}', '{random_date()}'
WHERE NOT EXISTS (SELECT 1 FROM orders WHERE order_id = '{order_id}');
""")
            for c in ordered:
                oi_id = get_uuid(f"oi_{order_id}_{c['id']}")
                order_item_count += 1
                lines.append(f"""INSERT INTO order_item (id, order_id, course_id)
SELECT '{oi_id}', '{order_id}', {c["id"]}
WHERE NOT EXISTS (SELECT 1 FROM order_item WHERE id = '{oi_id}');
""")
    
    lines.append("COMMIT;")
    path = os.path.join(OUTPUT_DIR, "..", "seed_permanent_orders.sql")
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"  ✓ {order_count} orders, {order_item_count} order items → seed_permanent_orders.sql")

# ──────────────────────────────────────────────────────────────
# WRITE: lms_payment_db — payments
# ──────────────────────────────────────────────────────────────
def write_payments_sql():
    lines = ["BEGIN;", ""]
    count = 0
    
    for s in students[:50]:
        num_payments = random.randint(1, 4)
        paid_courses = random.sample(published_courses, min(num_payments, len(published_courses)))
        for pidx, c in enumerate(paid_courses):
            txn_id = f"TXN-{s['id'][:8]}-{c['id']}-{pidx}"
            inst = instructors[c["instructorIdx"]]
            status = random.choices(["SUCCESS", "SUCCESS", "SUCCESS", "PENDING", "FAILED"], weights=[60, 15, 10, 10, 5])[0]
            count += 1
            lines.append(f"""INSERT INTO payments (txn_id, amount, status, first_name, email, phone, product_info, course_id, instructor_id, payer_user_id, currency, created_at, updated_at)
SELECT '{sql_escape(txn_id)}', {c["price"]}, '{status}', '{sql_escape(s["firstName"])}', '{s["email"]}', '9{random.randint(100000000, 999999999)}', '{sql_escape(c["title"][:100])}', '{c["id"]}', '{inst["userId"]}', '{s["id"]}', 'INR', '{random_date()}', '{random_date()}'
WHERE NOT EXISTS (SELECT 1 FROM payments WHERE txn_id = '{sql_escape(txn_id)}');
""")
    
    lines.append("COMMIT;")
    path = os.path.join(OUTPUT_DIR, "..", "seed_permanent_payments.sql")
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"  ✓ {count} payments → seed_permanent_payments.sql")

# ──────────────────────────────────────────────────────────────
# WRITE: lms_notification_db — notifications
# ──────────────────────────────────────────────────────────────
def write_notifications_sql():
    lines = ["BEGIN;", ""]
    count = 0
    
    for s in students:
        num_notifs = random.randint(2, 4)
        for nidx in range(num_notifs):
            nid = get_uuid(f"notif_{s['id']}_{nidx}")
            title, message = random.choice(NOTIFICATION_MESSAGES)
            status = random.choice(["SENT", "DELIVERED", "READ"])
            priority = random.choice(["LOW", "MEDIUM", "HIGH"])
            count += 1
            lines.append(f"""INSERT INTO notifications (id, title, message, status, priority, is_deleted, created_at)
SELECT '{nid}', '{sql_escape(title)}', '{sql_escape(message)}', '{status}', '{priority}', false, '{random_date()}'
WHERE NOT EXISTS (SELECT 1 FROM notifications WHERE id = '{nid}');
""")
            # Add user mapping
            lines.append(f"""INSERT INTO notification_users (notification_id, user_id)
SELECT '{nid}', '{s["id"]}'
WHERE NOT EXISTS (SELECT 1 FROM notification_users WHERE notification_id = '{nid}' AND user_id = '{s["id"]}');
""")
            # Add channel
            channel = random.choice(["EMAIL", "IN_APP", "PUSH"])
            lines.append(f"""INSERT INTO notification_channels (notification_id, channel)
SELECT '{nid}', '{channel}'
WHERE NOT EXISTS (SELECT 1 FROM notification_channels WHERE notification_id = '{nid}' AND channel = '{channel}');
""")
    
    lines.append("COMMIT;")
    path = os.path.join(OUTPUT_DIR, "..", "seed_permanent_notifications.sql")
    with open(path, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print(f"  ✓ {count} notifications → seed_permanent_notifications.sql")

# ──────────────────────────────────────────────────────────────
# Main
# ──────────────────────────────────────────────────────────────
if __name__ == "__main__":
    print("=" * 60)
    print("CyberLearnix LMS — Permanent Data Generator")
    print("=" * 60)
    print()
    write_users_sql()
    write_instructors_sql()
    write_courses_sql()
    write_enrollments_sql()
    write_progress_sql()
    write_wishlists_sql()
    write_carts_sql()
    write_reviews_sql()
    write_coupons_sql()
    write_orders_sql()
    write_payments_sql()
    write_notifications_sql()
    print()
    print("=" * 60)
    print("All SQL seed files generated successfully!")
    print("=" * 60)
