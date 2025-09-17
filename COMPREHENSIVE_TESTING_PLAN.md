# 🧪 Comprehensive Testing Plan for MokitoECommerce

## 📋 **Testing Overview**
This document provides a systematic approach to test all URL paths, exception handling, and security in your e-commerce application.

## 🔐 **Security Testing Matrix**

### **1. Authentication Required Endpoints**
Test these endpoints **without login** (should redirect to login or return 401):

#### **Web Endpoints (Should redirect to login)**
- `GET /profile` - User profile page
- `POST /profile/update` - Update profile
- `GET /cart` - Cart page
- `GET /orders` - Orders page
- `GET /payment` - Payment page

#### **API Endpoints (Should return 401 Unauthorized)**
- `GET /api/cart` - Get cart
- `POST /api/cart/add/{productId}` - Add to cart
- `DELETE /api/cart/remove/{productId}` - Remove from cart
- `DELETE /api/cart/clear` - Clear cart
- `POST /api/cart/increase/{cartItemId}` - Increase quantity
- `POST /api/cart/decrease/{cartItemId}` - Decrease quantity
- `GET /api/orders` - Get orders
- `POST /api/orders/place` - Place order
- `POST /api/orders/{id}/cancel` - Cancel order

### **2. Admin Only Endpoints**
Test these endpoints **as regular user** (should return 403 Forbidden):

#### **Web Admin Endpoints**
- `GET /admin` - Admin dashboard
- `GET /admin/orders` - Admin orders page
- `GET /admin/orders/{id}` - Order details
- `POST /admin/orders/{id}/status` - Update order status
- `POST /admin/orders/{id}/cancel` - Cancel order
- `GET /admin/products` - Admin products page
- `POST /admin/products` - Create product
- `POST /admin/products/{id}` - Update product
- `POST /admin/products/{id}/delete` - Delete product
- `GET /admin/users` - Admin users page
- `POST /admin/users/{id}/delete` - Delete user
- `POST /admin/users/{id}/toggle-status` - Toggle user status
- `POST /admin/users/{id}/activate` - Activate user
- `POST /admin/users/{id}/deactivate` - Deactivate user

#### **API Admin Endpoints**
- `POST /products/api/admin` - Create product (API)
- `PUT /products/api/admin/{id}` - Update product (API)
- `DELETE /products/api/admin/{id}` - Delete product (API)

### **3. Public Endpoints**
Test these endpoints **without login** (should work normally):

#### **Web Public Endpoints**
- `GET /` - Home page
- `GET /login` - Login page
- `POST /login` - Login form submission
- `GET /register` - Registration page
- `POST /register` - Registration form submission
- `GET /products` - Products listing page
- `GET /products/{id}` - Product detail page

#### **API Public Endpoints**
- `GET /products/api` - Get all products (API)
- `GET /products/api/search` - Search products (API)
- `GET /products/api/{id}` - Get product by ID (API)
- `POST /api/auth/register` - Register user (API)
- `POST /api/auth/login` - Login user (API)
- `POST /api/auth/logout` - Logout user (API)

## 🚨 **Exception Handling Testing**

### **1. 404 Not Found Tests**
Test these URLs (should show 404 error page for web, JSON for API):

#### **Web 404 Tests**
- `GET /nonexistent-page` - Non-existent page
- `GET /products/999999` - Non-existent product ID
- `GET /admin/nonexistent` - Non-existent admin page

#### **API 404 Tests**
- `GET /products/api/999999` - Non-existent product ID (API)
- `GET /api/nonexistent` - Non-existent API endpoint
- `GET /api/orders/999999` - Non-existent order ID

### **2. 500 Internal Server Error Tests**
Test these scenarios (should show 500 error page for web, JSON for API):

#### **Invalid Data Tests**
- `POST /products/api/admin` with invalid JSON
- `POST /api/auth/register` with invalid email format
- `POST /admin/products` with invalid product data

### **3. Validation Error Tests**
Test these scenarios (should show validation errors):

#### **Web Validation Tests**
- `POST /register` with empty fields
- `POST /admin/products` with invalid product data
- `POST /profile/update` with invalid email

#### **API Validation Tests**
- `POST /api/auth/register` with invalid data
- `POST /products/api/admin` with invalid product data

## 📝 **Step-by-Step Testing Process**

### **Phase 1: Security Testing**

#### **Step 1: Test Without Authentication**
1. Open browser in incognito/private mode
2. Test all "Authentication Required" endpoints
3. Verify redirects to login page for web endpoints
4. Verify 401 responses for API endpoints

#### **Step 2: Test as Regular User**
1. Register a new user account
2. Login as regular user
3. Test all "Admin Only" endpoints
4. Verify 403 Forbidden responses

#### **Step 3: Test as Admin User**
1. Create admin user in database or use existing admin
2. Login as admin user
3. Test all admin endpoints
4. Verify access is granted

### **Phase 2: Exception Handling Testing**

#### **Step 1: Test 404 Errors**
1. Test all "404 Not Found Tests" URLs
2. Verify proper error pages are shown
3. Check console for any errors

#### **Step 2: Test 500 Errors**
1. Test all "500 Internal Server Error Tests" scenarios
2. Verify proper error handling
3. Check error messages are user-friendly

#### **Step 3: Test Validation Errors**
1. Test all "Validation Error Tests" scenarios
2. Verify validation messages are displayed
3. Check form validation works correctly

### **Phase 3: Functional Testing**

#### **Step 1: Test All Public Endpoints**
1. Test home page loads correctly
2. Test product listing and search
3. Test product detail pages
4. Test registration and login flows

#### **Step 2: Test Authenticated User Flows**
1. Test profile management
2. Test cart functionality
3. Test order placement
4. Test order history

#### **Step 3: Test Admin Flows**
1. Test admin dashboard
2. Test product management
3. Test user management
4. Test order management

## 🛠️ **Testing Tools & Commands**

### **Browser Testing**
- Use browser developer tools (F12)
- Check Network tab for HTTP status codes
- Check Console tab for JavaScript errors

### **API Testing**
Use curl commands or Postman:

```bash
# Test API without authentication
curl -X GET http://localhost:8080/api/cart

# Test API with authentication
curl -X GET http://localhost:8080/api/cart \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Test admin API as regular user
curl -X POST http://localhost:8080/products/api/admin \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer USER_JWT_TOKEN" \
  -d '{"name":"Test Product","price":10.0}'
```

### **Database Testing**
Check user roles in database:
```sql
-- Check user roles
SELECT email, roles FROM users;

-- Check if admin user exists
SELECT * FROM users WHERE roles LIKE '%ADMIN%';
```

## 📊 **Expected Results Summary**

| Test Type | Web Endpoints | API Endpoints |
|-----------|---------------|---------------|
| **Unauthenticated Access** | Redirect to login | 401 Unauthorized |
| **Regular User → Admin** | 403 Forbidden | 403 Forbidden |
| **404 Errors** | 404 HTML page | 404 JSON response |
| **500 Errors** | 500 HTML page | 500 JSON response |
| **Validation Errors** | Form validation messages | 400 JSON with errors |

## 🔍 **Common Issues to Check**

1. **Security Issues**
   - Can regular users access admin pages?
   - Are API endpoints properly protected?
   - Are JWT tokens validated correctly?

2. **Exception Handling Issues**
   - Are 404 errors showing proper pages?
   - Are 500 errors handled gracefully?
   - Are validation errors user-friendly?

3. **Functional Issues**
   - Do all forms work correctly?
   - Are redirects working properly?
   - Are error messages helpful?

## 📋 **Testing Checklist**

- [ ] All public endpoints work without authentication
- [ ] All protected endpoints require authentication
- [ ] All admin endpoints require admin role
- [ ] 404 errors show proper error pages
- [ ] 500 errors are handled gracefully
- [ ] Validation errors are user-friendly
- [ ] API responses are consistent
- [ ] Web pages load correctly
- [ ] Forms submit and validate properly
- [ ] Redirects work as expected

---

**Note**: Test each scenario thoroughly and document any issues found. This comprehensive testing will ensure your application is secure, robust, and user-friendly.
