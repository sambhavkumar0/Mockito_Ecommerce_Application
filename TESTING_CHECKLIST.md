# ✅ MokitoECommerce Testing Checklist

## 🚀 **Quick Start Testing**

### **Step 1: Start Your Application**
```bash
# Make sure your app is running
mvn spring-boot:run
# or
./mvnw spring-boot:run
```

### **Step 2: Open Testing Tools**
1. **Browser Testing**: Open `test-endpoints.html` in your browser
2. **API Testing**: Run `test-api-endpoints.ps1` in PowerShell (Windows) or `test-api-endpoints.sh` in terminal (Linux/Mac)

---

## 🔐 **Security Testing Checklist**

### **Authentication Required Endpoints**
Test these **without logging in** (should redirect to login or return 401):

#### **Web Endpoints** (Should redirect to `/login`)
- [ ] `GET /profile` → Should redirect to login
- [ ] `POST /profile/update` → Should redirect to login  
- [ ] `GET /cart` → Should redirect to login
- [ ] `GET /orders` → Should redirect to login
- [ ] `GET /payment` → Should redirect to login

#### **API Endpoints** (Should return 401 Unauthorized)
- [ ] `GET /api/cart` → Should return 401
- [ ] `POST /api/cart/add/1` → Should return 401
- [ ] `DELETE /api/cart/remove/1` → Should return 401
- [ ] `GET /api/orders` → Should return 401
- [ ] `POST /api/orders/place` → Should return 401

### **Admin Only Endpoints**
Test these **as regular user** (should return 403 Forbidden):

#### **Web Admin Endpoints** (Should return 403)
- [ ] `GET /admin` → Should return 403
- [ ] `GET /admin/products` → Should return 403
- [ ] `GET /admin/users` → Should return 403
- [ ] `GET /admin/orders` → Should return 403
- [ ] `POST /admin/products` → Should return 403

#### **API Admin Endpoints** (Should return 403)
- [ ] `POST /products/api/admin` → Should return 403
- [ ] `PUT /products/api/admin/1` → Should return 403
- [ ] `DELETE /products/api/admin/1` → Should return 403

---

## 🚨 **Exception Handling Testing Checklist**

### **404 Not Found Tests**
Test these URLs (should show 404 error page for web, JSON for API):

#### **Web 404 Tests** (Should show 404 HTML page)
- [ ] `GET /nonexistent-page` → Should show 404 page
- [ ] `GET /products/999999` → Should show 404 page
- [ ] `GET /admin/nonexistent` → Should show 404 page

#### **API 404 Tests** (Should return 404 JSON)
- [ ] `GET /products/api/999999` → Should return 404 JSON
- [ ] `GET /api/nonexistent` → Should return 404 JSON
- [ ] `GET /api/orders/999999` → Should return 404 JSON

### **500 Internal Server Error Tests**
Test these scenarios (should show 500 error page for web, JSON for API):

#### **Invalid Data Tests**
- [ ] `POST /products/api/admin` with invalid JSON → Should return 400/500
- [ ] `POST /api/auth/register` with invalid email → Should return 400
- [ ] `POST /admin/products` with invalid data → Should show validation errors

---

## 🌐 **Public Endpoints Testing Checklist**

### **Web Public Endpoints** (Should work without login)
- [ ] `GET /` → Should show home page
- [ ] `GET /login` → Should show login form
- [ ] `GET /register` → Should show registration form
- [ ] `GET /products` → Should show products listing
- [ ] `GET /products/1` → Should show product detail (if exists)

### **API Public Endpoints** (Should return data without auth)
- [ ] `GET /products/api` → Should return products JSON
- [ ] `GET /products/api/search` → Should return search results
- [ ] `GET /products/api/1` → Should return product JSON (if exists)
- [ ] `POST /api/auth/register` → Should register user
- [ ] `POST /api/auth/login` → Should login user

---

## 🔍 **Detailed Testing Scenarios**

### **Scenario 1: New User Registration & Login**
1. [ ] Go to `/register`
2. [ ] Fill out registration form with valid data
3. [ ] Submit form → Should redirect to login or show success
4. [ ] Go to `/login`
5. [ ] Login with new credentials → Should redirect to home/profile
6. [ ] Try to access `/admin` → Should be denied (403 or redirect)

### **Scenario 2: Product Management (Admin)**
1. [ ] Login as admin user
2. [ ] Go to `/admin/products`
3. [ ] Create a new product with valid data
4. [ ] Edit an existing product
5. [ ] Delete a product
6. [ ] Verify all operations work correctly

### **Scenario 3: Shopping Cart (Regular User)**
1. [ ] Login as regular user
2. [ ] Go to `/products`
3. [ ] Add products to cart (via API or form)
4. [ ] Go to `/cart`
5. [ ] Modify cart quantities
6. [ ] Remove items from cart
7. [ ] Place order

### **Scenario 4: Error Handling**
1. [ ] Try to access non-existent pages
2. [ ] Submit forms with invalid data
3. [ ] Try to access admin features as regular user
4. [ ] Verify all errors are handled gracefully

---

## 🛠️ **Testing Tools Usage**

### **Browser Testing Tool** (`test-endpoints.html`)
1. Open the file in your browser
2. Click each test button
3. Check results for each test
4. Note any failures and investigate

### **API Testing Scripts**
1. **Windows**: Run `test-api-endpoints.ps1` in PowerShell
2. **Linux/Mac**: Run `test-api-endpoints.sh` in terminal
3. Check output for pass/fail status
4. Investigate any failures

### **Manual Testing**
1. Use the URL list in the testing plan
2. Test each endpoint manually
3. Check browser developer tools (F12)
4. Verify HTTP status codes and responses

---

## 📊 **Expected Results Summary**

| Test Type | Web Endpoints | API Endpoints |
|-----------|---------------|---------------|
| **Unauthenticated Access** | Redirect to login | 401 Unauthorized |
| **Regular User → Admin** | 403 Forbidden | 403 Forbidden |
| **404 Errors** | 404 HTML page | 404 JSON response |
| **500 Errors** | 500 HTML page | 500 JSON response |
| **Validation Errors** | Form validation messages | 400 JSON with errors |

---

## 🚨 **Common Issues to Watch For**

### **Security Issues**
- [ ] Regular users can access admin pages
- [ ] API endpoints are not properly protected
- [ ] JWT tokens are not validated correctly
- [ ] Session management issues

### **Exception Handling Issues**
- [ ] 404 errors show white pages instead of error pages
- [ ] 500 errors are not handled gracefully
- [ ] Validation errors are not user-friendly
- [ ] API error responses are inconsistent

### **Functional Issues**
- [ ] Forms don't submit correctly
- [ ] Redirects don't work properly
- [ ] Error messages are not helpful
- [ ] Pages don't load correctly

---

## 📝 **Testing Notes**

### **Before Testing**
- [ ] Ensure application is running on `http://localhost:8080`
- [ ] Check database has some test data
- [ ] Verify all dependencies are installed

### **During Testing**
- [ ] Test in incognito/private browser mode
- [ ] Clear browser cache between tests
- [ ] Check browser console for errors
- [ ] Monitor application logs

### **After Testing**
- [ ] Document any issues found
- [ ] Fix critical security issues immediately
- [ ] Update exception handling if needed
- [ ] Verify all tests pass before deployment

---

## 🎯 **Success Criteria**

Your application passes testing if:
- [ ] All security tests pass (proper authentication/authorization)
- [ ] All exception handling tests pass (proper error pages/responses)
- [ ] All public endpoints work correctly
- [ ] No critical security vulnerabilities found
- [ ] All error messages are user-friendly
- [ ] Application is stable and responsive

---

**Happy Testing! 🚀**
