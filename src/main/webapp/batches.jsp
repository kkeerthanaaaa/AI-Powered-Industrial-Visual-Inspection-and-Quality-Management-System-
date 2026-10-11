<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.industrialinspection.model.*" %>
<%@ page import="com.industrialinspection.util.DateTimeUtil" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("activePage", "batches");
    List<ProductionBatch> batches = (List<ProductionBatch>) request.getAttribute("batches");
    List<Product> products = (List<Product>) request.getAttribute("products");
    List<Machine> machines = (List<Machine>) request.getAttribute("machines");
    List<Operator> operators = (List<Operator>) request.getAttribute("operators");
    String keyword = (String) request.getAttribute("keyword");
    String selectedStatus = (String) request.getAttribute("selectedStatus");
    User currentUser = (User) session.getAttribute("currentUser");
%>
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="page-header">
    <div>
        <h1 class="page-title">Production Batches &amp; Lots</h1>
        <div class="page-desc">Shift-based manufacturing runs with machine and operator allocations</div>
    </div>
    <div style="display: flex; gap: 10px;">
        <a href="<%= request.getContextPath() %>/batches?action=export" class="btn btn-secondary">Export CSV</a>
        <% if (currentUser != null && currentUser.canManageBatches()) { %>
            <button onclick="document.getElementById('batchModal').style.display='block'" class="btn btn-primary">+ Create Production Batch</button>
        <% } %>
    </div>
</div>

<!-- Filter Bar -->
<div class="card" style="padding: 14px 20px; margin-bottom: 16px;">
    <form action="<%= request.getContextPath() %>/batches" method="GET" style="display: flex; gap: 10px; align-items: center;">
        <input type="text" name="keyword" class="form-control" placeholder="Search batch number..." value="<%= keyword != null ? keyword : "" %>" style="width: 250px;">
        <select name="status" class="form-control" style="width: 180px;">
            <option value="ALL">All Statuses</option>
            <option value="PENDING" <%= "PENDING".equals(selectedStatus) ? "selected" : "" %>>PENDING</option>
            <option value="IN_INSPECTION" <%= "IN_INSPECTION".equals(selectedStatus) ? "selected" : "" %>>IN_INSPECTION</option>
            <option value="APPROVED" <%= "APPROVED".equals(selectedStatus) ? "selected" : "" %>>APPROVED</option>
            <option value="REJECTED" <%= "REJECTED".equals(selectedStatus) ? "selected" : "" %>>REJECTED</option>
            <option value="QUARANTINED" <%= "QUARANTINED".equals(selectedStatus) ? "selected" : "" %>>QUARANTINED</option>
        </select>
        <button type="submit" class="btn btn-secondary">Filter</button>
        <a href="<%= request.getContextPath() %>/batches" class="btn btn-secondary">Reset</a>
    </form>
</div>

<!-- Batches Table -->
<div class="table-responsive">
    <table class="data-table">
        <thead>
            <tr>
                <th>Batch #</th>
                <th>Target Component</th>
                <th>Lot Size</th>
                <th>Date</th>
                <th>Shift</th>
                <th>Machine</th>
                <th>Operator</th>
                <th>Status</th>
                <% if (currentUser != null && currentUser.canManageBatches()) { %>
                <th>Action</th>
                <% } %>
            </tr>
        </thead>
        <tbody>
        <% if (batches != null && !batches.isEmpty()) {
            for (ProductionBatch b : batches) { %>
            <tr>
                <td style="font-weight: 700; color: var(--accent-cyan);"><%= b.getBatchNumber() %></td>
                <td><%= b.getProductName() != null ? b.getProductName() : ("Product #" + b.getProductId()) %></td>
                <td><%= b.getBatchSize() %> units</td>
                <td><%= DateTimeUtil.formatDate(b.getProductionDate()) %></td>
                <td><%= b.getShift().name() %></td>
                <td><%= b.getMachineName() != null ? b.getMachineName() : ("Machine #" + b.getMachineId()) %></td>
                <td><%= b.getOperatorName() != null ? b.getOperatorName() : ("Operator #" + b.getOperatorId()) %></td>
                <td>
                    <span class="badge <%= "APPROVED".equals(b.getStatus().name()) ? "badge-pass" : ("REJECTED".equals(b.getStatus().name()) ? "badge-fail" : "badge-review") %>">
                        <%= b.getStatus().name() %>
                    </span>
                </td>
                <% if (currentUser != null && currentUser.canManageBatches()) { %>
                <td>
                    <a href="<%= request.getContextPath() %>/batches?action=delete&id=<%= b.getBatchId() %>" 
                       onclick="return confirm('Delete batch <%= b.getBatchNumber() %>?')" 
                       class="btn btn-danger btn-sm">Delete</a>
                </td>
                <% } %>
            </tr>
        <%  }
           } else { %>
            <tr>
                <td colspan="9" style="text-align: center; color: var(--text-muted); padding: 24px;">No production batches found.</td>
            </tr>
        <% } %>
        </tbody>
    </table>
</div>

<!-- Modal Dialog -->
<div id="batchModal" style="display:none; position:fixed; z-index:999; left:0; top:0; width:100%; height:100%; background:rgba(0,0,0,0.7); overflow:auto;">
    <div class="card" style="width: 480px; margin: 60px auto; background: var(--bg-card);">
        <h2 style="font-size: 18px; margin-bottom: 14px;">Schedule New Production Batch</h2>
        <form action="<%= request.getContextPath() %>/batches" method="POST">
            <div class="form-group">
                <label class="form-label">Batch Number</label>
                <input type="text" name="batchNumber" class="form-control" value="BATCH-2026-<%= System.currentTimeMillis() % 10000 %>" required>
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
                <label class="form-label">Lot Size (Units)</label>
                <input type="number" name="batchSize" class="form-control" value="100" min="1" required>
            </div>
            <div class="form-group">
                <label class="form-label">Shift</label>
                <select name="shift" class="form-control">
                    <option value="MORNING">MORNING</option>
                    <option value="EVENING">EVENING</option>
                    <option value="NIGHT">NIGHT</option>
                </select>
            </div>
            <div class="form-group">
                <label class="form-label">Machine</label>
                <select name="machineId" class="form-control">
                    <% if (machines != null) {
                        for (Machine m : machines) { %>
                            <option value="<%= m.getMachineId() %>"><%= m.getMachineCode() %> - <%= m.getMachineName() %></option>
                    <%  }
                       } %>
                </select>
            </div>
            <div class="form-group">
                <label class="form-label">Floor Operator</label>
                <select name="operatorId" class="form-control">
                    <% if (operators != null) {
                        for (Operator o : operators) { %>
                            <option value="<%= o.getOperatorId() %>"><%= o.getName() %> (<%= o.getEmployeeCode() %>)</option>
                    <%  }
                       } %>
                </select>
            </div>
            <div class="form-group">
                <label class="form-label">Status</label>
                <select name="status" class="form-control">
                    <option value="PENDING">PENDING</option>
                    <option value="IN_INSPECTION">IN_INSPECTION</option>
                </select>
            </div>
            <div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px;">
                <button type="button" class="btn btn-secondary" onclick="document.getElementById('batchModal').style.display='none'">Cancel</button>
                <button type="submit" class="btn btn-primary">Create Batch</button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
