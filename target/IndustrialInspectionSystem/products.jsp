<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.industrialinspection.model.Product" %>
<%@ page import="com.industrialinspection.model.User" %>
<%@ page import="com.industrialinspection.util.DateTimeUtil" %>
<%@ page import="java.util.List" %>
<%
    request.setAttribute("activePage", "products");
    List<Product> products = (List<Product>) request.getAttribute("products");
    String keyword = (String) request.getAttribute("keyword");
    String selectedCat = (String) request.getAttribute("selectedCategory");
    User currentUser = (User) session.getAttribute("currentUser");
%>
<jsp:include page="/WEB-INF/includes/header.jsp" />

<div class="page-header">
    <div>
        <h1 class="page-title">Product Catalog &amp; Components</h1>
        <div class="page-desc">Precision components manufactured for automotive &amp; aerospace platforms</div>
    </div>
    <div style="display: flex; gap: 10px;">
        <a href="<%= request.getContextPath() %>/products?action=export" class="btn btn-secondary">Export CSV</a>
        <% if (currentUser != null && currentUser.canManageProducts()) { %>
            <button onclick="document.getElementById('productModal').style.display='block'" class="btn btn-primary">+ Register New Component</button>
        <% } %>
    </div>
</div>

<!-- Filter Bar -->
<div class="card" style="padding: 14px 20px; margin-bottom: 16px;">
    <form action="<%= request.getContextPath() %>/products" method="GET" style="display: flex; gap: 10px; align-items: center;">
        <input type="text" name="keyword" class="form-control" placeholder="Search product name or batch..." value="<%= keyword != null ? keyword : "" %>" style="width: 250px;">
        <select name="category" class="form-control" style="width: 220px;">
            <option value="ALL">All Categories</option>
            <option value="Automotive Braking" <%= "Automotive Braking".equals(selectedCat) ? "selected" : "" %>>Automotive Braking</option>
            <option value="Automotive Powertrain" <%= "Automotive Powertrain".equals(selectedCat) ? "selected" : "" %>>Automotive Powertrain</option>
            <option value="Aerospace Structural" <%= "Aerospace Structural".equals(selectedCat) ? "selected" : "" %>>Aerospace Structural</option>
            <option value="Automotive Transmission" <%= "Automotive Transmission".equals(selectedCat) ? "selected" : "" %>>Automotive Transmission</option>
            <option value="Aerospace Transmission" <%= "Aerospace Transmission".equals(selectedCat) ? "selected" : "" %>>Aerospace Transmission</option>
        </select>
        <button type="submit" class="btn btn-secondary">Filter</button>
        <a href="<%= request.getContextPath() %>/products" class="btn btn-secondary">Reset</a>
    </form>
</div>

<!-- Products Table -->
<div class="table-responsive">
    <table class="data-table">
        <thead>
            <tr>
                <th>ID</th>
                <th>Component Name</th>
                <th>Category</th>
                <th>Production Line</th>
                <th>Mfg Date</th>
                <th>Batch #</th>
                <th>Supplier</th>
                <th>Customer</th>
                <% if (currentUser != null && currentUser.canManageProducts()) { %>
                <th>Action</th>
                <% } %>
            </tr>
        </thead>
        <tbody>
        <% if (products != null && !products.isEmpty()) {
            for (Product p : products) { %>
            <tr>
                <td><%= p.getProductId() %></td>
                <td style="font-weight: 600; color: var(--accent-cyan);"><%= p.getProductName() %></td>
                <td><%= p.getCategory() %></td>
                <td><%= p.getProductionLine() %></td>
                <td><%= DateTimeUtil.formatDate(p.getManufacturingDate()) %></td>
                <td><%= p.getBatchNumber() %></td>
                <td><%= p.getSupplier() %></td>
                <td><%= p.getCustomer() %></td>
                <% if (currentUser != null && currentUser.canManageProducts()) { %>
                <td>
                    <a href="<%= request.getContextPath() %>/products?action=delete&id=<%= p.getProductId() %>" 
                       onclick="return confirm('Delete product #<%= p.getProductId() %>?')" 
                       class="btn btn-danger btn-sm">Delete</a>
                </td>
                <% } %>
            </tr>
        <%  }
           } else { %>
            <tr>
                <td colspan="9" style="text-align: center; color: var(--text-muted); padding: 24px;">No products found matching filters.</td>
            </tr>
        <% } %>
        </tbody>
    </table>
</div>

<!-- Modal Dialog -->
<div id="productModal" style="display:none; position:fixed; z-index:999; left:0; top:0; width:100%; height:100%; background:rgba(0,0,0,0.7); overflow:auto;">
    <div class="card" style="width: 480px; margin: 60px auto; background: var(--bg-card);">
        <h2 style="font-size: 18px; margin-bottom: 14px;">Register New Precision Component</h2>
        <form action="<%= request.getContextPath() %>/products" method="POST">
            <div class="form-group">
                <label class="form-label">Component Name</label>
                <input type="text" name="productName" class="form-control" required placeholder="e.g. Brake Disc 320mm">
            </div>
            <div class="form-group">
                <label class="form-label">Category</label>
                <input type="text" name="category" class="form-control" value="Automotive Braking" required>
            </div>
            <div class="form-group">
                <label class="form-label">Production Line</label>
                <input type="text" name="productionLine" class="form-control" value="Auto-Line-2" required>
            </div>
            <div class="form-group">
                <label class="form-label">Batch Reference Number</label>
                <input type="text" name="batchNumber" class="form-control" value="BATCH-2026-01" required>
            </div>
            <div class="form-group">
                <label class="form-label">Supplier</label>
                <input type="text" name="supplier" class="form-control" value="Tata Steel Ltd" required>
            </div>
            <div class="form-group">
                <label class="form-label">Target Customer</label>
                <input type="text" name="customer" class="form-control" value="OEM Client" required>
            </div>
            <div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 16px;">
                <button type="button" class="btn btn-secondary" onclick="document.getElementById('productModal').style.display='none'">Cancel</button>
                <button type="submit" class="btn btn-primary">Save Component</button>
            </div>
        </form>
    </div>
</div>

<jsp:include page="/WEB-INF/includes/footer.jsp" />
