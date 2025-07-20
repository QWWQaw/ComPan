package cloud.compan.servlet;

import cloud.compan.servlet.config.AppModule;
import cloud.compan.servlet.config.DatabaseModule;
import cloud.compan.servlet.web.RequestDispatcher;
import cloud.compan.servlet.web.RouteRegistry;
import cloud.compan.servlet.web.HardcodedRouteRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * Main Servlet - Application entry point
 * Responsible for initializing Guice container, registering routes, dispatching HTTP requests
 */
@WebServlet(name = "MainServlet", urlPatterns = {"/*"}, loadOnStartup = 1)
public class MainServlet extends HttpServlet {
    
    private Injector injector;
    private RequestDispatcher requestDispatcher;
    
    @Override
    public void init(ServletConfig config) throws ServletException {
        System.out.println("Initializing MainServlet...");
        super.init(config);

        try {
            // 1. Initialize Guice injector
            initializeGuice();
            
            // 2. Register all routes using hardcoded approach
            registerAllRoutes();
            
            System.out.println("MainServlet initialization completed!");
            
        } catch (Exception e) {
            System.err.println("MainServlet initialization failed: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("MainServlet initialization failed", e);
        }
    }
    
    /**
     * Initialize Guice dependency injection container
     */
    private void initializeGuice() {
        System.out.println("Initializing Guice container...");
        
        // Create Guice injector, install all modules
        injector = Guice.createInjector(
            new AppModule(),        // Main application module
            new DatabaseModule()    // Database module
        );
        
        // Get request dispatcher instance
        requestDispatcher = injector.getInstance(RequestDispatcher.class);
        
        System.out.println("Guice container initialization completed");
    }
    
    /**
     * Register all routes using hardcoded approach
     */
    private void registerAllRoutes() {
        System.out.println("Starting hardcoded route registration...");
        
        try {
            // Get route registry
            RouteRegistry routeRegistry = injector.getInstance(RouteRegistry.class);
            
            // Create hardcoded route registry
            HardcodedRouteRegistry hardcodedRouteRegistry = new HardcodedRouteRegistry(routeRegistry, injector);
            
            // Register all routes
            hardcodedRouteRegistry.registerAllRoutes();
            
            System.out.println("All routes hardcoded registration completed");
            
        } catch (Exception e) {
            System.err.println("Route registration failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Route registration failed", e);
        }
    }
    
    /**
     * Handle all HTTP requests
     * Dispatch requests to RequestDispatcher for processing
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Set response encoding
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Dispatch request to appropriate controller method
            requestDispatcher.dispatch(request, response);
            
        } catch (Exception e) {
            System.err.println("Request processing exception: " + e.getMessage());
            e.printStackTrace();
            
            // Return 500 error
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                "{\"error\":\"Internal Server Error\",\"message\":\"" + 
                e.getMessage() + "\",\"status\":500}");
        }
    }
    
    @Override
    public void destroy() {
        System.out.println("MainServlet is being destroyed...");
        
        // Clean up resources
        if (injector != null) {
            // Guice will automatically handle singleton object cleanup
            System.out.println("Guice container resources cleaned up");
        }
        
        super.destroy();
        System.out.println("MainServlet destruction completed");
    }
}
