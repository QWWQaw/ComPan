//package cloud.compan.servlet.controller;
//
//import cloud.compan.servlet.annotations.enums.RequestMethod;
//import cloud.compan.servlet.config.AppModule;
//import cloud.compan.servlet.utils.JsonUtils;
//import cloud.compan.servlet.web.ControllerScanner;
//import cloud.compan.servlet.web.RequestDispatcher;
//import cloud.compan.servlet.web.RouteRegistry;
//import com.google.inject.Guice;
//import com.google.inject.Injector;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.DisplayName;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * 测试控制器集成测试
// * 测试整个路由系统的完整功能
// */
//@DisplayName("测试控制器集成测试")
//class TestControllerIntegrationTest {
//
//    private RouteRegistry routeRegistry;
//    private RequestDispatcher requestDispatcher;
//    private JsonUtils jsonUtils;
//    private Injector injector;
//
//    @BeforeEach
//    void setUp() {
//        // 初始化Guice注入器
//        injector = Guice.createInjector(new AppModule());
//
//        // 获取依赖
//        routeRegistry = injector.getInstance(RouteRegistry.class);
//        requestDispatcher = injector.getInstance(RequestDispatcher.class);
//        jsonUtils = injector.getInstance(JsonUtils.class);
//
//        // 扫描并注册测试控制器
//        ControllerScanner scanner = injector.getInstance(ControllerScanner.class);
//        scanner.scanAndRegister("cloud.compan.servlet.controller");
//    }
//
//    @Test
//    @DisplayName("测试路由注册成功")
//    void testRouteRegistration() {
//        // 检查是否有路由被注册
//        var allRoutes = routeRegistry.getAllRoutes();
//        assertTrue(allRoutes.size() > 0, "应该有路由被注册");
//
//        // 检查具体路由
//        var helloRoute = routeRegistry.findRoute("/api/hello", RequestMethod.GET);
//        assertTrue(helloRoute.isPresent(), "应该找到/api/hello路由");
//
//        var statusRoute = routeRegistry.findRoute("/api/status", RequestMethod.GET);
//        assertTrue(statusRoute.isPresent(), "应该找到/api/status路由");
//    }
//
//    @Test
//    @DisplayName("测试JsonUtils功能")
//    void testJsonUtils() {
//        // 测试基本序列化
//        TestData data = new TestData("test", 123);
//        String json = jsonUtils.toJson(data);
//
//        assertNotNull(json);
//        assertTrue(json.contains("\"name\":\"test\""));
//        assertTrue(json.contains("\"value\":123"));
//
//        // 测试反序列化
//        TestData deserializedData = jsonUtils.fromJson(json, TestData.class);
//        assertNotNull(deserializedData);
//        assertEquals("test", deserializedData.getName());
//        assertEquals(123, deserializedData.getValue());
//    }
//
//    @Test
//    @DisplayName("测试路由匹配功能")
//    void testRouteMatching() {
//        // 测试精确匹配
//        var exactRoute = routeRegistry.findRoute("/api/hello", RequestMethod.GET);
//        assertTrue(exactRoute.isPresent());
//
//        // 测试方法不匹配
//        var wrongMethod = routeRegistry.findRoute("/api/hello", RequestMethod.POST);
//        assertFalse(wrongMethod.isPresent());
//
//        // 测试路径不存在
//        var nonExistent = routeRegistry.findRoute("/api/nonexistent", RequestMethod.GET);
//        assertFalse(nonExistent.isPresent());
//    }
//
//    @Test
//    @DisplayName("测试依赖注入")
//    void testDependencyInjection() {
//        // 确保所有组件都正确注入
//        assertNotNull(routeRegistry);
//        assertNotNull(requestDispatcher);
//        assertNotNull(jsonUtils);
//
//        // 测试控制器依赖注入
//        TestController testController = injector.getInstance(TestController.class);
//        assertNotNull(testController);
//    }
//
//    /**
//     * 测试数据类
//     */
//    public static class TestData {
//        private String name;
//        private int value;
//
//        public TestData() {}
//
//        public TestData(String name, int value) {
//            this.name = name;
//            this.value = value;
//        }
//
//        public String getName() { return name; }
//        public void setName(String name) { this.name = name; }
//
//        public int getValue() { return value; }
//        public void setValue(int value) { this.value = value; }
//    }
//}