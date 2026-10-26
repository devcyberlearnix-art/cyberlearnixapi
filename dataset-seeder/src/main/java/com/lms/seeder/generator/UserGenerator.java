package com.lms.seeder.generator;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@UtilityClass
public class UserGenerator {

    private static final Random random = new Random();

    private static final String[] INDIAN_FIRST_NAMES = {
        "Arjun", "Priya", "Rahul", "Ananya", "Kiran", "Sneha", "Vikram", "Neha",
        "Akhil", "Meghana", "Rohan", "Sanya", "Aarav", "Isha", "Ravi", "Kavya",
        "Dev", "Tara", "Yash", "Riya", "Aditya", "Pooja", "Manish", "Divya",
        "Suresh", "Anita", "Rajesh", "Meera", "Venkat", "Lakshmi", "Karthik", "Shreya",
        "Naveen", "Priyanka", "Siddharth", "Nisha", "Amit", "Swati", "Vikas", "Preeti",
        "Deepak", "Rashmi", "Saurabh", "Kavita", "Manoj", "Sunita", "Rakesh", "Anjali",
        "Sanjay", "Geeta", "Ajay", "Rekha", "Sunil", "Suman", "Vijay", "Savita",
        "Dinesh", "Kiran", "Mahesh", "Sarika", "Pradeep", "Neha", "Ashok", "Kavita",
        "Ramesh", "Sangeeta", "Suresh", "Anita", "Rajendra", "Meena", "Harish", "Shalini"
    };

    private static final String[] INDIAN_LAST_NAMES = {
        "Reddy", "Sharma", "Kumar", "Rao", "Varma", "Patel", "Singh", "Mehta",
        "Das", "Gupta", "Jain", "Bose", "Nair", "Iyer", "Pillai", "Menon",
        "Sen", "Roy", "Kapoor", "Chopra", "Banerjee", "Mukherjee", "Chatterjee", "Dutta",
        "Srivastava", "Verma", "Malhotra", "Khanna", "Saxena", "Bhatia", "Ahuja", "Chopra",
        "Garg", "Goel", "Jain", "Kapoor", "Malik", "Nayak", "Prasad", "Rana",
        "Shukla", "Tripathi", "Upadhyay", "Vaidya", "Yadav", "Zacharia", "Abraham", "Mathew"
    };

    private static final String[] CITIES = {
        "Hyderabad", "Bangalore", "Mumbai", "Delhi", "Chennai", "Pune", "Kolkata",
        "Ahmedabad", "Jaipur", "Lucknow", "Chandigarh", "Indore", "Coimbatore", "Kochi",
        "Visakhapatnam", "Nagpur", "Bhopal", "Surat", "Baroda", "Mysore", "Guwahati"
    };

    private static final String[] STATES = {
        "Telangana", "Karnataka", "Maharashtra", "Delhi", "Tamil Nadu", "Maharashtra",
        "West Bengal", "Gujarat", "Rajasthan", "Uttar Pradesh", "Punjab", "Madhya Pradesh",
        "Tamil Nadu", "Kerala", "Andhra Pradesh", "Maharashtra", "Madhya Pradesh", "Gujarat",
        "Gujarat", "Karnataka", "Assam"
    };

    private static final String[] ORGANIZATIONS = {
        "TechCorp", "Infosys", "Wipro", "TCS", "HCL Technologies", "Google India",
        "Microsoft India", "Amazon India", "Flipkart", "Zoho", "Freshworks", "Razorpay",
        "PhonePe", "Swiggy", "Ola", "Self-Employed", "Accenture India", "IBM India",
        "Capgemini India", "Cognizant India", "SAP Labs India", "Oracle India", "Adobe India"
    };

    private static final String[] SKILLS = {
        "Java", "Spring Boot", "Microservices", "SQL", "PostgreSQL", "Docker", "Kubernetes",
        "AWS", "DevOps", "Python", "Cyber Security", "React", "JavaScript", "Angular",
        "TypeScript", "MongoDB", "Redis", "Linux", "Git", "REST APIs", "Node.js",
        "Machine Learning", "Deep Learning", "Data Engineering", "System Design",
        "Software Engineering", "CI/CD", "Terraform", "Ansible", "Jenkins"
    };

    private static final String[] QUALIFICATIONS = {
        "B.Tech", "M.Tech", "BCA", "MCA", "B.Sc CS", "M.Sc CS", "B.E.", "PhD",
        "Diploma", "MBA"
    };

    private static final String[] FIELDS_OF_STUDY = {
        "Computer Science", "Information Technology", "Software Engineering",
        "Computer Applications", "Data Science", "Artificial Intelligence"
    };

    public static String generateFirstName() {
        return INDIAN_FIRST_NAMES[random.nextInt(INDIAN_FIRST_NAMES.length)];
    }

    public static String generateLastName() {
        return INDIAN_LAST_NAMES[random.nextInt(INDIAN_LAST_NAMES.length)];
    }

    public static String generateEmail(String firstName, String lastName) {
        return String.format("%s.%s@cyberlearnix.test",
                firstName.toLowerCase(), lastName.toLowerCase());
    }

    public static String generateCity() {
        return CITIES[random.nextInt(CITIES.length)];
    }

    public static String generateState() {
        return STATES[random.nextInt(STATES.length)];
    }

    public static String generateOrganization() {
        return ORGANIZATIONS[random.nextInt(ORGANIZATIONS.length)];
    }

    public static String generateSkills() {
        int count = 3 + random.nextInt(4); // 3-6 skills
        List<String> selectedSkills = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String skill = SKILLS[random.nextInt(SKILLS.length)];
            if (!selectedSkills.contains(skill)) {
                selectedSkills.add(skill);
            }
        }
        return String.join(", ", selectedSkills);
    }

    public static String generateQualification() {
        return QUALIFICATIONS[random.nextInt(QUALIFICATIONS.length)];
    }

    public static String generateFieldOfStudy() {
        return FIELDS_OF_STUDY[random.nextInt(FIELDS_OF_STUDY.length)];
    }

    // BCrypt hash for "Password13!" (from existing seed_permanent_users.sql)
    public static final String PASSWORD_HASH = "$2a$10$IXHGtp2am7E0V2t/WiH4KeHrqqYiEV4HwKfODgOO3XIpicLdbAdUG";
}
