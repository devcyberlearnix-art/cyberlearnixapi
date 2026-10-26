package com.lms.seeder.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DatabaseConfig {

    @Value("${seeder.datasource.user.url}")
    private String userDbUrl;
    @Value("${seeder.datasource.user.username}")
    private String userDbUser;
    @Value("${seeder.datasource.user.password}")
    private String userDbPassword;

    @Value("${seeder.datasource.course.url}")
    private String courseDbUrl;
    @Value("${seeder.datasource.course.username}")
    private String courseDbUser;
    @Value("${seeder.datasource.course.password}")
    private String courseDbPassword;

    @Value("${seeder.datasource.cart.url}")
    private String cartDbUrl;
    @Value("${seeder.datasource.cart.username}")
    private String cartDbUser;
    @Value("${seeder.datasource.cart.password}")
    private String cartDbPassword;

    @Value("${seeder.datasource.coupon.url}")
    private String couponDbUrl;
    @Value("${seeder.datasource.coupon.username}")
    private String couponDbUser;
    @Value("${seeder.datasource.coupon.password}")
    private String couponDbPassword;

    @Value("${seeder.datasource.order.url}")
    private String orderDbUrl;
    @Value("${seeder.datasource.order.username}")
    private String orderDbUser;
    @Value("${seeder.datasource.order.password}")
    private String orderDbPassword;

    @Value("${seeder.datasource.payment.url}")
    private String paymentDbUrl;
    @Value("${seeder.datasource.payment.username}")
    private String paymentDbUser;
    @Value("${seeder.datasource.payment.password}")
    private String paymentDbPassword;

    @Value("${seeder.datasource.review.url}")
    private String reviewDbUrl;
    @Value("${seeder.datasource.review.username}")
    private String reviewDbUser;
    @Value("${seeder.datasource.review.password}")
    private String reviewDbPassword;

    @Value("${seeder.datasource.instructor.url}")
    private String instructorDbUrl;
    @Value("${seeder.datasource.instructor.username}")
    private String instructorDbUser;
    @Value("${seeder.datasource.instructor.password}")
    private String instructorDbPassword;

    @Value("${seeder.datasource.wishlist.url}")
    private String wishlistDbUrl;
    @Value("${seeder.datasource.wishlist.username}")
    private String wishlistDbUser;
    @Value("${seeder.datasource.wishlist.password}")
    private String wishlistDbPassword;

    @Value("${seeder.datasource.notification.url}")
    private String notificationDbUrl;
    @Value("${seeder.datasource.notification.username}")
    private String notificationDbUser;
    @Value("${seeder.datasource.notification.password}")
    private String notificationDbPassword;

    @Bean
    @Primary
    @Qualifier("userDataSource")
    public DataSource userDataSource() {
        return DataSourceBuilder.create()
                .url(userDbUrl)
                .username(userDbUser)
                .password(userDbPassword)
                .driverClassName("org.postgresql.Driver")
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean
    @Qualifier("courseDataSource")
    public DataSource courseDataSource() {
        return DataSourceBuilder.create()
                .url(courseDbUrl)
                .username(courseDbUser)
                .password(courseDbPassword)
                .driverClassName("org.postgresql.Driver")
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean
    @Qualifier("cartDataSource")
    public DataSource cartDataSource() {
        return DataSourceBuilder.create()
                .url(cartDbUrl)
                .username(cartDbUser)
                .password(cartDbPassword)
                .driverClassName("org.postgresql.Driver")
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean
    @Qualifier("couponDataSource")
    public DataSource couponDataSource() {
        return DataSourceBuilder.create()
                .url(couponDbUrl)
                .username(couponDbUser)
                .password(couponDbPassword)
                .driverClassName("org.postgresql.Driver")
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean
    @Qualifier("orderDataSource")
    public DataSource orderDataSource() {
        return DataSourceBuilder.create()
                .url(orderDbUrl)
                .username(orderDbUser)
                .password(orderDbPassword)
                .driverClassName("org.postgresql.Driver")
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean
    @Qualifier("paymentDataSource")
    public DataSource paymentDataSource() {
        return DataSourceBuilder.create()
                .url(paymentDbUrl)
                .username(paymentDbUser)
                .password(paymentDbPassword)
                .driverClassName("org.postgresql.Driver")
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean
    @Qualifier("reviewDataSource")
    public DataSource reviewDataSource() {
        return DataSourceBuilder.create()
                .url(reviewDbUrl)
                .username(reviewDbUser)
                .password(reviewDbPassword)
                .driverClassName("org.postgresql.Driver")
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean
    @Qualifier("instructorDataSource")
    public DataSource instructorDataSource() {
        return DataSourceBuilder.create()
                .url(instructorDbUrl)
                .username(instructorDbUser)
                .password(instructorDbPassword)
                .driverClassName("org.postgresql.Driver")
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean
    @Qualifier("wishlistDataSource")
    public DataSource wishlistDataSource() {
        return DataSourceBuilder.create()
                .url(wishlistDbUrl)
                .username(wishlistDbUser)
                .password(wishlistDbPassword)
                .driverClassName("org.postgresql.Driver")
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean
    @Qualifier("notificationDataSource")
    public DataSource notificationDataSource() {
        return DataSourceBuilder.create()
                .url(notificationDbUrl)
                .username(notificationDbUser)
                .password(notificationDbPassword)
                .driverClassName("org.postgresql.Driver")
                .type(com.zaxxer.hikari.HikariDataSource.class)
                .build();
    }

    @Bean
    public Map<String, JdbcTemplate> jdbcTemplates(
            @Qualifier("userDataSource") DataSource userDataSource,
            @Qualifier("courseDataSource") DataSource courseDataSource,
            @Qualifier("cartDataSource") DataSource cartDataSource,
            @Qualifier("couponDataSource") DataSource couponDataSource,
            @Qualifier("orderDataSource") DataSource orderDataSource,
            @Qualifier("paymentDataSource") DataSource paymentDataSource,
            @Qualifier("reviewDataSource") DataSource reviewDataSource,
            @Qualifier("instructorDataSource") DataSource instructorDataSource,
            @Qualifier("wishlistDataSource") DataSource wishlistDataSource,
            @Qualifier("notificationDataSource") DataSource notificationDataSource
    ) {
        // Configure HikariCP pool sizes to avoid connection exhaustion
        configureHikariPool(userDataSource);
        configureHikariPool(courseDataSource);
        configureHikariPool(cartDataSource);
        configureHikariPool(couponDataSource);
        configureHikariPool(orderDataSource);
        configureHikariPool(paymentDataSource);
        configureHikariPool(reviewDataSource);
        configureHikariPool(instructorDataSource);
        configureHikariPool(wishlistDataSource);
        configureHikariPool(notificationDataSource);
        
        Map<String, JdbcTemplate> templates = new HashMap<>();
        templates.put("user", new JdbcTemplate(userDataSource));
        templates.put("course", new JdbcTemplate(courseDataSource));
        templates.put("cart", new JdbcTemplate(cartDataSource));
        templates.put("coupon", new JdbcTemplate(couponDataSource));
        templates.put("order", new JdbcTemplate(orderDataSource));
        templates.put("payment", new JdbcTemplate(paymentDataSource));
        templates.put("review", new JdbcTemplate(reviewDataSource));
        templates.put("instructor", new JdbcTemplate(instructorDataSource));
        templates.put("wishlist", new JdbcTemplate(wishlistDataSource));
        templates.put("notification", new JdbcTemplate(notificationDataSource));
        return templates;
    }
    
    private void configureHikariPool(DataSource dataSource) {
        if (dataSource instanceof com.zaxxer.hikari.HikariDataSource) {
            com.zaxxer.hikari.HikariDataSource hikariDataSource = (com.zaxxer.hikari.HikariDataSource) dataSource;
            hikariDataSource.setMaximumPoolSize(2);
            hikariDataSource.setMinimumIdle(1);
        }
    }
}
