package cloud.compan.servlet.config;

import com.google.inject.Guice;
import com.google.inject.Injector;
import cloud.compan.servlet.controller.AuthController;
import cloud.compan.servlet.repository.UserRepository;
import cloud.compan.servlet.service.UserService;

/**
 * AutoScanModule Demo
 * Demonstrates the usage of AutoScanModule for automatic component registration
 */
public class AutoScanDemo {
    
    public static void main(String[] args) {
        System.out.println("AutoScanModule Demo - Starting...");
        
        try {
            // Create Injector with AutoScanModule
            Injector injector = Guice.createInjector(new AutoScanModule("cloud.compan.servlet"));
            
            System.out.println("Injector created successfully with AutoScanModule");
            
            // Test Service injection
            System.out.println("Testing Service injection...");
            UserService userService = injector.getInstance(UserService.class);
            System.out.println("UserService injected successfully: " + userService.getClass().getSimpleName());
            
            // Test Controller injection
            System.out.println("Testing Controller injection...");
            AuthController authController = injector.getInstance(AuthController.class);
            System.out.println("AuthController injected successfully: " + authController.getClass().getSimpleName());
            
            // Test Repository injection
            System.out.println("Testing Repository injection...");
            UserRepository userRepository = injector.getInstance(UserRepository.class);
            System.out.println("UserRepository injected successfully: " + userRepository.getClass().getSimpleName());
            
            // Test singleton behavior
            System.out.println("Testing singleton behavior...");
            UserService userService2 = injector.getInstance(UserService.class);
            AuthController authController2 = injector.getInstance(AuthController.class);
            UserRepository userRepository2 = injector.getInstance(UserRepository.class);
            
            System.out.println("UserService singleton test: " + (userService == userService2 ? "PASSED" : "FAILED"));
            System.out.println("AuthController singleton test: " + (authController == authController2 ? "PASSED" : "FAILED"));
            System.out.println("UserRepository singleton test: " + (userRepository == userRepository2 ? "PASSED" : "FAILED"));
            
            System.out.println("AutoScanModule Demo completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Error in AutoScanModule Demo: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 