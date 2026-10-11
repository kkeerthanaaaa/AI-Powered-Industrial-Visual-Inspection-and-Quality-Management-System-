<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.industrialinspection.model.*" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("activePage", "upload");
    List<Product> products = (List<Product>) request.getAttribute("products");
    List<ProductionBatch> batches = (List<ProductionBatch>) request.getAttribute("batches");
    List<ProductImage> images = (List<ProductImage>) request.getAttribute("images");
%>
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="page-header">
    <div>
        <h1 class="page-title">Optical Inspection Image Acquisition</h1>
        <div class="page-desc">High-resolution optical camera capture and image registration cell</div>
    </div>
</div>

<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">
    <!-- Acquisition Form -->
    <div class="card">
        <h3 style="font-size: 16px; font-weight: 700; margin-bottom: 12px; color: var(--accent-cyan);">
            Acquire &amp; Register Component Capture
        </h3>
        <p style="color: var(--text-muted); font-size: 12px; margin-bottom: 16px;">
            Simulate optical camera line trigger for automotive/aerospace components.
        </p>
        <form action="<%= request.getContextPath() %>/upload-image" method="POST">
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
                <label class="form-label">Production Batch</label>
                <select name="batchId" class="form-control" required>
                    <% if (batches != null) {
                        for (ProductionBatch b : batches) { %>
                            <option value="<%= b.getBatchId() %>"><%= b.getBatchNumber() %></option>
                    <%  }
                       } %>
                </select>
            </div>
            <div class="form-group">
                <label class="form-label">Optical Station / Camera Rig ID</label>
                <input type="text" name="cameraId" class="form-control" value="CAM-OPTICAL-01" required>
            </div>
            <button type="submit" class="btn btn-primary" style="width: 100%; margin-top: 10px;">
                📷 Capture &amp; Register Image File
            </button>
        </form>
    </div>

    <!-- Existing Captured Images Table -->
    <div class="card">
        <h3 style="font-size: 16px; font-weight: 700; margin-bottom: 12px; color: var(--accent-blue);">
            Optical File Repository
        </h3>
        <div class="table-responsive">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Filename</th>
                        <th>Resolution</th>
                        <th>Size</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                <% if (images != null && !images.isEmpty()) {
                    for (ProductImage img : images) { %>
                    <tr>
                        <td><%= img.getImageId() %></td>
                        <td style="font-family: monospace; color: var(--accent-cyan); font-size: 12px;"><%= img.getFileName() %></td>
                        <td><%= img.getResolutionString() %></td>
                        <td><%= img.getFormattedSize() %></td>
                        <td><span class="badge badge-pass"><%= img.getInspectionStatus() %></span></td>
                    </tr>
                <%  }
                   } else { %>
                    <tr>
                        <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 16px;">No captured images logged.</td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
