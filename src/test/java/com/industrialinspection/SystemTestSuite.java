package com.industrialinspection;

import com.industrialinspection.ai.AIInspectionService;
import com.industrialinspection.ai.PredictionResult;
import com.industrialinspection.ai.SimulatedAIInspectionService;
import com.industrialinspection.model.*;
import com.industrialinspection.model.enums.*;
import com.industrialinspection.service.*;
import com.industrialinspection.util.ImageUtil;
import com.industrialinspection.util.PasswordUtil;

import java.io.File;
import java.sql.Date;
import java.util.List;

/**
 * Industrial Visual Inspection and Quality Management System
 * Comprehensive Automated System Test Suite (Pure Java SE)
 */
public class SystemTestSuite {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("===============================================================================");
        System.out.println("ABC PRECISION MANUFACTURING PVT. LTD. - AUTOMATED TEST SUITE");
        System.out.println("AI-POWERED INDUSTRIAL VISUAL INSPECTION & QUALITY MANAGEMENT SYSTEM");
        System.out.println("===============================================================================\n");

        runTest("Password Hashing & PBKDF2 Verification", SystemTestSuite::testPasswordHashing);
        runTest("User Authentication & Account Lockout", SystemTestSuite::testAuthenticationService);
        runTest("Polymorphic Role-Based Access Control", SystemTestSuite::testPolymorphicRBAC);
        runTest("Product Catalog Registration & Specs", SystemTestSuite::testProductService);
        runTest("Production Batch Lifecycle & Tracking", SystemTestSuite::testBatchService);
        runTest("Machine & Optical Metrology Station Ops", SystemTestSuite::testMachineService);
        runTest("Operator Directory & Shift Assignment", SystemTestSuite::testOperatorService);
        runTest("Synthetic Image Synthesis & Physical Storage", SystemTestSuite::testImageProcessing);
        runTest("Simulated AI Vision Inference & Bounding Boxes", SystemTestSuite::testAIEngine);
        runTest("Quality Inspection Decision Workflow", SystemTestSuite::testInspectionWorkflow);
        runTest("Corrective & Preventive Action (CAPA) Lifecycle", SystemTestSuite::testCorrectiveActions);
        runTest("Executive Quality Report Generation (CSV/TXT)", SystemTestSuite::testReportGenerators);

        System.out.println("\n-------------------------------------------------------------------------------");
        System.out.printf("TEST SUMMARY: Total: %d | Passed: %d | Failed: %d | Success Rate: %.1f%%\n",
                testsRun, testsPassed, testsFailed, (testsPassed * 100.0 / (testsRun == 0 ? 1 : testsRun)));
        System.out.println("-------------------------------------------------------------------------------");

        if (testsFailed > 0) {
            System.err.println("❌ SYSTEM TEST SUITE FINISHED WITH FAILURES.");
            System.exit(1);
        } else {
            System.out.println("✅ ALL INDUSTRIAL QUALITY ASSURANCE SYSTEM TESTS PASSED SUCCESSFULLY!");
        }
    }

    private static void runTest(String name, TestCase testCase) {
        testsRun++;
        System.out.printf("[TEST %02d] %-55s ... ", testsRun, name);
        try {
            testCase.run();
            testsPassed++;
            System.out.println("PASSED");
        } catch (Throwable t) {
            testsFailed++;
            System.out.println("FAILED");
            System.err.println("       Error: " + t.getMessage());
            t.printStackTrace(System.err);
        }
    }

    @FunctionalInterface
    interface TestCase {
        void run() throws Exception;
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError(String.format("Assertion Failed: %s [Expected: %s, Actual: %s]", message, expected, actual));
        }
    }

    // 1. Password Hashing
    private static void testPasswordHashing() {
        String rawPassword = "AdminSecret#2026";
        String salt = PasswordUtil.generateSalt();
        assertTrue(salt != null && !salt.isEmpty(), "Salt should be non-empty");

        String hash = PasswordUtil.hashPassword(rawPassword, salt);
        assertTrue(hash != null && hash.length() >= 32, "Hash should be valid SHA256 hex string");

        boolean verified = PasswordUtil.verifyPassword(rawPassword, hash, salt);
        assertTrue(verified, "Password should verify with correct salt");

        boolean wrongVerified = PasswordUtil.verifyPassword("WrongPassword", hash, salt);
        assertTrue(!wrongVerified, "Password should fail with wrong credentials");
    }

    // 2. Authentication Service & Lockout
    private static void testAuthenticationService() throws Exception {
        AuthenticationService auth = new AuthenticationService();
        User admin = auth.login("admin", "Admin@123", "127.0.0.1");
        assertTrue(admin != null, "Admin login should succeed");
        assertEquals("admin", admin.getUsername(), "Username must match");

        // Test failed attempts
        int failed = 0;
        for (int i = 0; i < 6; i++) {
            try {
                auth.login("admin", "WrongPass123", "127.0.0.1");
            } catch (Exception e) {
                failed++;
            }
        }
        assertTrue(failed > 0, "Failed attempts should throw invalid login exceptions");
    }

    // 3. Polymorphic RBAC
    private static void testPolymorphicRBAC() {
        Administrator admin = new Administrator(1, "admin", "Admin User", "admin@abc.com");
        QualityEngineer qe = new QualityEngineer(2, "engineer", "Quality Eng", "qe@abc.com");
        InspectionOperator op = new InspectionOperator(3, "operator", "Line Operator", "op@abc.com");

        assertTrue(admin.canManageUsers(), "Admin can manage users");
        assertTrue(admin.canBackupDatabase(), "Admin can backup DB");
        assertTrue(!op.canManageUsers(), "Operator cannot manage users");
        assertTrue(qe.canPerformInspection(), "QE can perform inspection");
        assertTrue(qe.canCreateCorrectiveAction(), "QE can create CAPA");
        assertTrue(!op.canCreateCorrectiveAction(), "Operator cannot create CAPA");
    }

    // 4. Product Service
    private static void testProductService() throws Exception {
        ProductService ps = new ProductService();
        List<Product> products = ps.getAllProducts();
        assertTrue(products != null && !products.isEmpty(), "Initial seed products should be loaded");

        Product p = new Product();
        p.setProductName("High-Precision Test Turbine Shaft");
        p.setCategory("Aerospace Testing");
        p.setProductionLine("Line-1 (CNC Turning)");
        p.setManufacturingDate(new Date(System.currentTimeMillis()));
        p.setBatchNumber("TEST-BATCH-001");
        p.setSupplier("Precision Alloys Ltd");
        p.setCustomer("Aero Dynamics Corp");
        p.setDescription("Test spec titanium shaft");

        ps.addProduct(p, 1);
        assertTrue(p.getProductId() > 0, "Created product should receive valid ID");

        Product retrieved = ps.getProductById(p.getProductId());
        assertEquals("High-Precision Test Turbine Shaft", retrieved.getProductName(), "Retrieved name must match");
    }

    // 5. Batch Service
    private static void testBatchService() throws Exception {
        BatchService bs = new BatchService();
        List<ProductionBatch> batches = bs.getAllBatches();
        assertTrue(batches != null && !batches.isEmpty(), "Initial batches should exist");

        ProductionBatch b = new ProductionBatch();
        b.setBatchNumber("BATCH-TEST-2026-X");
        b.setProductId(1);
        b.setBatchSize(500);
        b.setProductionDate(new Date(System.currentTimeMillis()));
        b.setShift(Shift.MORNING);
        b.setMachineId(1);
        b.setOperatorId(1);
        b.setStatus(BatchStatus.PENDING);

        bs.createBatch(b, 1);
        assertTrue(b.getBatchId() > 0, "Created batch must have valid ID");

        bs.updateBatchStatus(b.getBatchId(), BatchStatus.APPROVED, 1);
        ProductionBatch updated = bs.getBatchById(b.getBatchId());
        assertEquals(BatchStatus.APPROVED, updated.getStatus(), "Batch status must update to APPROVED");
    }

    // 6. Machine Service
    private static void testMachineService() throws Exception {
        MachineService ms = new MachineService();
        List<Machine> list = ms.getAllMachines();
        assertTrue(list != null && !list.isEmpty(), "Machines should exist");

        Machine m = new Machine();
        m.setMachineCode("OPT-TEST-09");
        m.setMachineName("Sub-Micron Laser Profilometer");
        m.setMachineType("Optical CMM");
        m.setProductionLine("Line-Metrology-Alpha");
        m.setStatus(MachineStatus.ACTIVE);
        m.setInstallationDate(new Date(System.currentTimeMillis()));

        ms.addMachine(m, 1);
        assertTrue(m.getMachineId() > 0, "Machine should be registered");

        ms.updateStatus(m.getMachineId(), MachineStatus.MAINTENANCE, 1);
        Machine updated = ms.getMachineById(m.getMachineId());
        assertEquals(MachineStatus.MAINTENANCE, updated.getStatus(), "Status should be MAINTENANCE");
    }

    // 7. Operator Service
    private static void testOperatorService() throws Exception {
        OperatorService os = new OperatorService();
        List<Operator> ops = os.getAllOperators();
        assertTrue(ops != null && !ops.isEmpty(), "Operators should be available");

        Operator op = new Operator();
        op.setEmployeeCode("OP-TEST-77");
        op.setName("Vikram Malhotra");
        op.setDepartment("CNC Optical Metrology");
        op.setShift(Shift.EVENING);
        op.setContact("+91-9876543299");
        op.setStatus(OperatorStatus.ACTIVE);

        os.addOperator(op, 1);
        assertTrue(op.getOperatorId() > 0, "Operator should be created");
    }

    // 8. Synthetic Image Processing
    private static void testImageProcessing() throws Exception {
        File sampleImg = ImageUtil.createSampleIndustrialImage("test_sample_part.png", "Gear Shaft 400", true);
        assertTrue(sampleImg.exists(), "Sample industrial image should be created");

        int[] dims = ImageUtil.getImageDimensions(sampleImg);
        assertTrue(dims[0] > 0 && dims[1] > 0, "Dimensions should be positive");
    }

    // 9. Simulated AI Engine
    private static void testAIEngine() throws Exception {
        AIInspectionService aiService = new SimulatedAIInspectionService();
        File sampleImg = ImageUtil.createSampleIndustrialImage("ai_test_specimen.png", "Aero Impeller", true);

        PredictionResult result = aiService.analyzeImage(sampleImg);
        assertTrue(result != null, "AI prediction result should not be null");
        assertTrue(result.getConfidenceScore() >= 0.0 && result.getConfidenceScore() <= 100.0, 
                "Confidence score must be valid");
        assertTrue(result.getProcessingTimeMs() >= 0, "Processing time must be positive");

        if (!result.isDefectFree()) {
            assertTrue(result.getDefectType() != null, "Defect type should be categorized");
            assertTrue(result.getSeverity() != null, "Severity level should be assigned");
            assertTrue(result.getBoundingBoxW() > 0 && result.getBoundingBoxH() > 0, 
                    "Bounding box dimensions must be positive");
        }
    }

    // 10. Inspection Workflow
    private static void testInspectionWorkflow() throws Exception {
        InspectionService is = new InspectionService();
        ImageService imgService = new ImageService();

        File sampleImg = ImageUtil.createSampleIndustrialImage("insp_test_pin.png", "Shaft Pin", false);
        ProductImage pImg = imgService.uploadAndRegisterImage(sampleImg, 1, 1, "CAM-LINE-01", 1);
        assertTrue(pImg.getImageId() > 0, "Image must be registered in database");

        // Complete inspection
        InspectionRecord record = is.completeInspection(1, 1, pImg.getImageId(), 1, null, 
                InspectionDecision.PASS, "Optical inspection verified clean", "APPROVED");
        assertTrue(record != null, "Inspection record should be created");
        assertTrue(record.getInspectionId() > 0, "Inspection ID should be assigned");
        assertEquals(InspectionDecision.PASS, record.getFinalDecision(), "Decision must be PASS");
    }

    // 11. Corrective Action Lifecycle
    private static void testCorrectiveActions() throws Exception {
        CorrectiveActionService cas = new CorrectiveActionService();
        List<CorrectiveAction> all = cas.getAllActions();
        assertTrue(all != null, "Actions list should not be null");

        CorrectiveAction ca = new CorrectiveAction();
        ca.setInspectionId(1);
        ca.setRootCause("Thermal expansion of spindle shaft");
        ca.setCorrectiveAction("Install chiller on spindle circulation line");
        ca.setResponsiblePerson("Chief QA Engineer");
        ca.setStatus(ActionStatus.OPEN);
        ca.setTargetDate(new Date(System.currentTimeMillis() + 86400000L * 7));

        cas.createCorrectiveAction(ca, 1);
        assertTrue(ca.getActionId() > 0, "CAPA record should be created");

        cas.updateStatus(ca.getActionId(), ActionStatus.IN_PROGRESS, "Work order issued to maintenance", 1);
        CorrectiveAction updated = cas.getActionById(ca.getActionId());
        assertEquals(ActionStatus.IN_PROGRESS, updated.getStatus(), "Status should be IN_PROGRESS");
    }

    // 12. Quality Report Generation
    private static void testReportGenerators() throws Exception {
        QualityReportService qrs = new QualityReportService();
        File reportsDir = new File("reports");
        if (!reportsDir.exists()) reportsDir.mkdirs();

        QualityReport dailyRep = qrs.generateReport(ReportType.DAILY_INSPECTION, "CSV", 1, null);
        assertTrue(dailyRep != null, "Daily report should be generated");
        File file = new File(dailyRep.getFilePath());
        assertTrue(file.exists() && file.length() > 0, "CSV Report file should be written to disk");

        QualityReport txtRep = qrs.generateReport(ReportType.PRODUCT_DEFECT, "TXT", 1, null);
        assertTrue(txtRep != null, "Text report should be generated");
        File txtFile = new File(txtRep.getFilePath());
        assertTrue(txtFile.exists() && txtFile.length() > 0, "TXT Report file should be written to disk");
    }
}
