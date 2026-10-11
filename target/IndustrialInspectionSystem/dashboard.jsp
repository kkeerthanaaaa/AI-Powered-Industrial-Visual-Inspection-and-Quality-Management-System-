<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<%
    request.setAttribute("activePage", "dashboard");
    Map<String, Object> kpi = (Map<String, Object>) request.getAttribute("kpi");
    Map<String, Integer> defectDist = (Map<String, Integer>) request.getAttribute("defectDist");
    Map<String, Integer> machineDefects = (Map<String, Integer>) request.getAttribute("machineDefects");
    Map<String, Double> monthlyTrend = (Map<String, Double>) request.getAttribute("monthlyTrend");
%>
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="page-header">
    <div>
        <h1 class="page-title">Real-Time Manufacturing Quality Dashboard</h1>
        <div class="page-desc">Automotive &amp; Aerospace Precision Line Optical Quality Metrics</div>
    </div>
    <div>
        <a href="<%= request.getContextPath() %>/inspections" class="btn btn-primary">+ Execute Visual Inspection</a>
    </div>
</div>

<!-- KPI Cards -->
<div class="grid-kpi">
    <div class="kpi-card">
        <div class="kpi-title">Total Products</div>
        <div class="kpi-value"><%= kpi != null ? kpi.get("totalProducts") : "0" %></div>
    </div>
    <div class="kpi-card">
        <div class="kpi-title">Inspections Logged</div>
        <div class="kpi-value"><%= kpi != null ? kpi.get("totalInspections") : "0" %></div>
    </div>
    <div class="kpi-card">
        <div class="kpi-title">Conforming (PASS)</div>
        <div class="kpi-value green"><%= kpi != null ? kpi.get("passedInspections") : "0" %></div>
    </div>
    <div class="kpi-card">
        <div class="kpi-title">Rejected (FAIL)</div>
        <div class="kpi-value red"><%= kpi != null ? kpi.get("failedInspections") : "0" %></div>
    </div>
    <div class="kpi-card">
        <div class="kpi-title">Defect Yield Rate</div>
        <div class="kpi-value amber"><%= kpi != null ? kpi.get("defectPercentage") : "0.0" %>%</div>
    </div>
    <div class="kpi-card">
        <div class="kpi-title">Critical Defects</div>
        <div class="kpi-value red"><%= kpi != null ? kpi.get("criticalDefects") : "0" %></div>
    </div>
</div>

<!-- Analytical Tables -->
<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">
    <!-- Defect Distribution -->
    <div class="card">
        <h3 style="font-size: 15px; font-weight: 700; margin-bottom: 12px; color: var(--accent-cyan);">
            Optical Defect Taxonomy Distribution
        </h3>
        <div class="table-responsive">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Defect Category</th>
                        <th style="text-align: right;">Count</th>
                    </tr>
                </thead>
                <tbody>
                <% if (defectDist != null) {
                    for (Map.Entry<String, Integer> e : defectDist.entrySet()) { %>
                    <tr>
                        <td><%= e.getKey() %></td>
                        <td style="text-align: right; font-weight: 700; color: var(--accent-amber);"><%= e.getValue() %></td>
                    </tr>
                <%  }
                   } %>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Machine-wise Volume -->
    <div class="card">
        <h3 style="font-size: 15px; font-weight: 700; margin-bottom: 12px; color: var(--accent-blue);">
            Machine-wise Defect Volume
        </h3>
        <div class="table-responsive">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Machine ID</th>
                        <th style="text-align: right;">Defect Count</th>
                    </tr>
                </thead>
                <tbody>
                <% if (machineDefects != null) {
                    for (Map.Entry<String, Integer> e : machineDefects.entrySet()) { %>
                    <tr>
                        <td><%= e.getKey() %></td>
                        <td style="text-align: right; font-weight: 700; color: var(--accent-red);"><%= e.getValue() %></td>
                    </tr>
                <%  }
                   } %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Monthly Trend Summary -->
<div class="card">
    <h3 style="font-size: 15px; font-weight: 700; margin-bottom: 12px; color: var(--accent-green);">
        Six-Month Quality Yield Progression
    </h3>
    <div class="table-responsive">
        <table class="data-table">
            <thead>
                <tr>
                    <% if (monthlyTrend != null) {
                        for (String month : monthlyTrend.keySet()) { %>
                            <th><%= month %></th>
                    <%  }
                       } %>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <% if (monthlyTrend != null) {
                        for (Double val : monthlyTrend.values()) { %>
                            <td style="font-weight: 700; color: var(--accent-green);"><%= String.format("%.2f%%", val) %></td>
                    <%  }
                       } %>
                </tr>
            </tbody>
        </table>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
