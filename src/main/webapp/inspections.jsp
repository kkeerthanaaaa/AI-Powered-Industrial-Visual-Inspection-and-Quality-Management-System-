<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.industrialinspection.model.*" %>
<%@ page import="com.industrialinspection.util.DateTimeUtil" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("activePage", "inspections");
    List<InspectionRecord> inspections = (List<InspectionRecord>) request.getAttribute("inspections");
    List<Product> products = (List<Product>) request.getAttribute("products");
    List<ProductionBatch> batches = (List<ProductionBatch>) request.getAttribute("batches");
    List<ProductImage> images = (List<ProductImage>) request.getAttribute("images");
    String keyword = (String) request.getAttribute("keyword");
    String selectedDecision = (String) request.getAttribute("selectedDecision");
    User currentUser = (User) session.getAttribute("currentUser");
%>
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="page-header">
    <div>
        <h1 class="page-title">Visual Inspection Execution &amp; Review Station</h1>
        <div class="page-desc">Optical quality evaluation, AI Computer Vision anomaly detection, and auditor review</div>
    </div>
    <div style="display: flex; gap: 10px;">
        <a href="<%= request.getContextPath() %>/inspections?action=export" class="btn btn-secondary">Export CSV</a>
        <% if (currentUser != null && currentUser.canPerformInspection()) { %>
            <button onclick="document.getElementById('inspModal').style.display='block'" class="btn btn-primary">+ Execute Visual Inspection</button>
        <% } %>
    </div>
</div>

<!-- Filter Bar -->
<div class="card" style="padding: 14px 20px; margin-bottom: 16px;">
    <form action="<%= request.getContextPath() %>/inspections" method="GET" style="display: flex; gap: 10px; align-items: center;">
        <input type="text" name="keyword" class="form-control" placeholder="Search product, batch, or remarks..." value="<%= keyword != null ? keyword : "" %>" style="width: 280px;">
        <select name="decision" class="form-control" style="width: 180px;">
            <option value="ALL">All Decisions</option>
            <option value="PASS" <%= "PASS".equals(selectedDecision) ? "selected" : "" %>>PASS</option>
            <option value="FAIL" <%= "FAIL".equals(selectedDecision) ? "selected" : "" %>>FAIL</option>
            <option value="REQUIRES_REVIEW" <%= "REQUIRES_REVIEW".equals(selectedDecision) ? "selected" : "" %>>REQUIRES_REVIEW</option>
        </select>
        <button type="submit" class="btn btn-secondary">Filter</button>
        <a href="<%= request.getContextPath() %>/inspections" class="btn btn-secondary">Reset</a>
    </form>
</div>

<!-- Inspections Table -->
<div class="table-responsive">
    <table class="data-table">
        <thead>
            <tr>
                <th>ID</th>
                <th>Component</th>
                <th>Batch #</th>
                <th>Inspector</th>
                <th>AI Prediction</th>
                <th>Confidence</th>
                <th>Decision</th>
                <th>Timestamp</th>
                <th>Remarks</th>
                <th>Status</th>
            </tr>
        </thead>
        <tbody>
        <% if (inspections != null && !inspections.isEmpty()) {
            for (InspectionRecord r : inspections) { %>
            <tr>
                <td><%= r.getInspectionId() %></td>
                <td style="font-weight: 600; color: var(--accent-cyan);"><%= r.getProductName() != null ? r.getProductName() : ("Product #" + r.getProductId()) %></td>
                <td><%= r.getBatchNumber() != null ? r.getBatchNumber() : ("Batch #" + r.getBatchId()) %></td>
                <td><%= r.getInspectorName() != null ? r.getInspectorName() : ("User #" + r.getInspectorId()) %></td>
                <td>
                    <span style="font-family: monospace; font-weight: 700; color: <%= "DEFECT_FREE".equals(r.getPredictedLabel()) ? "var(--accent-green)" : "var(--accent-red)" %>;">
                        <%= r.getPredictedLabel() != null ? r.getPredictedLabel() : "PENDING" %>
                    </span>
                </td>
                <td style="font-weight: 600;"><%= String.format("%.1f%%", r.getConfidenceScore()) %></td>
                <td>
                    <span class="badge <%= "PASS".equals(r.getFinalDecision().name()) ? "badge-pass" : ("FAIL".equals(r.getFinalDecision().name()) ? "badge-fail" : "badge-review") %>">
                        <%= r.getFinalDecision().name() %>
                    </span>
                </td>
                <td style="font-size: 12px; color: var(--text-muted);"><%= DateTimeUtil.formatTimestamp(r.getInspectionDate()) %></td>
                <td style="max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;"><%= r.getRemarks() != null ? r.getRemarks() : "" %></td>
                <td><span style="font-size: 11px; color: var(--text-muted);"><%= r.getStatus() %></span></td>
            </tr>
        <%  }
           } else { %>
            <tr>
                <td colspan="10" style="text-align: center; color: var(--text-muted); padding: 24px;">No visual inspection records found matching criteria.</td>
            </tr>
        <% } %>
        </tbody>
    </table>
</div>

<!-- Modal Dialog -->
<div id="inspModal" style="display:none; position:fixed; z-index:999; left:0; top:0; width:100%; height:100%; background:rgba(0,0,0,0.7); overflow:auto;">
    <div class="card" style="width: 520px; margin: 50px auto; background: var(--bg-card);">
        <h2 style="font-size: 18px; margin-bottom: 6px;">Visual Inspection Workflow Console</h2>
        <p style="color: var(--text-muted); font-size: 12px; margin-bottom: 16px;">
            Select component &amp; lot, link optical capture, and record quality decision.
        </p>
        <form action="<%= request.getContextPath() %>/inspections" method="POST">
            <div class="form-group">
                <label class="form-label">Production Batch</label>
                <select name="batchId" class="form-control" required>
                    <% if (batches != null) {
                        for (ProductionBatch b : batches) { %>
                            <option value="<%= b.getBatchId() %>"><%= b.getBatchNumber() %> - <%= b.getProductName() %></option>
                    <%  }
                       } %>
                </select>
            </div>
            <div class="form-group">
                <label class="form-label">Target Product</label>
                <select name="productId" class="form-control" required>
                    <% if (products != null) {
                        for (Product p : products) { %>
                            <option value="<%= p.getProductId() %>"><%= p.getProductName() %></option>
                    <%  }
                       } %>
                </select>
            </div>
            <div class="form-group">
                <label class="form-label">Optical Capture Image File</label>
                <select name="imageId" class="form-control" required>
                    <% if (images != null) {
                        for (ProductImage img : images) { %>
                            <option value="<%= img.getImageId() %>"><%= img.getFileName() %> (<%= img.getResolutionString() %>)</option>
                    <%  }
                       } %>
                </select>
                <div style="margin-top: 4px; font-size: 11px;">
                    <a href="<%= request.getContextPath() %>/upload-image" style="color: var(--accent-cyan); text-decoration: none;">+ Upload / Acquire New Image</a>
                </div>
            </div>
            <div class="form-group">
                <label class="form-label">Final Decision</label>
                <select name="finalDecision" class="form-control" required>
                    <option value="PASS">PASS (Conforms to drawing &amp; tolerance)</option>
                    <option value="FAIL">FAIL (Non-conformance / Defect Detected)</option>
                    <option value="REQUIRES_REVIEW">REQUIRES_REVIEW (Secondary verification needed)</option>
                </select>
            </div>
            <div class="form-group">
                <label class="form-label">Inspector Remarks / Review Comments</label>
                <textarea name="remarks" class="form-control" rows="3" placeholder="Optical surface inspection verified against standard..."></textarea>
            </div>
            <div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px;">
                <button type="button" class="btn btn-secondary" onclick="document.getElementById('inspModal').style.display='none'">Cancel</button>
                <button type="submit" class="btn btn-primary">⚡ Run AI &amp; Submit Inspection</button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
