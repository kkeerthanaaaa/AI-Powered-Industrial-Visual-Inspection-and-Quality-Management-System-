package com.industrialinspection.report;

import com.industrialinspection.exception.ReportGenerationException;
import com.industrialinspection.model.QualityReport;

import java.io.File;
import java.util.Map;

/**
 * Strategy/Template Interface for Industrial Quality Report Generators.
 * Demonstrates Abstraction and Polymorphism.
 */
public interface ReportGenerator {

    /**
     * Generates a quality report based on input filter parameters.
     *
     * @param parameters Map of filter parameters (dateRange, batchId, productId, format, etc.)
     * @param destination File to write report output
     * @param generatedBy User ID of initiating quality engineer
     * @return QualityReport model metadata
     * @throws ReportGenerationException on file I/O or rendering failure
     */
    QualityReport generate(Map<String, Object> parameters, File destination, int generatedBy) 
            throws ReportGenerationException;

    String getReportName();
}
