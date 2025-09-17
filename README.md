# Mockito_Ecommerce_Application


# MokitoECommerce

## 📋 Overview

MokitoECommerce is a full-featured Spring Boot e-commerce application supporting web and REST API endpoints, robust security, comprehensive exception handling, and admin/user management. It is designed for learning, testing, and real-world deployment.

---

## 🛠️ Tech Stack

- **Backend:** Spring Boot 3.5.5, Spring Data JPA, Spring Security, Hibernate
- **Frontend:** Thymeleaf (HTML templates), Bootstrap (CSS)
- **Database:** MySQL
- **Authentication:** JWT (JSON Web Token)
- **Testing:** JUnit 5, Mockito
- **Other:** Lombok, Maven

---

## 📦 Features

### User Features

- Registration & Login (JWT and session-based)
- Profile management
- Product browsing, search, and filtering
- Shopping cart (add, remove, update quantities)
- Order placement and order history
- Secure payment simulation

### Admin Features

- Dashboard with user/order/product stats
- Product management (CRUD: create, update, soft delete)
- User management (activate/deactivate/delete)
- Order management (view, update status, cancel)
- Image upload and validation for products

### API Features

- RESTful endpoints for products, cart, orders, authentication
- Consistent JSON responses with [`ApiResponse`](src/main/java/com/cts/ecommerce/dto/ApiResponse.java)
- Error handling for validation, 404, 500, and business logic errors

### Exception Handling

- Unified global exception handler ([`GlobalExceptionHandler`](src/main/java/com/cts/ecommerce/exception/GlobalExceptionHandler.java))
- Custom error pages for 404, 500, validation errors
- API error responses with status codes and messages

### Security

- Role-based access control (`ROLE_ADMIN`, `ROLE_USER`)
- JWT authentication for API endpoints
- CSRF protection (disabled for APIs)
- Password hashing with BCrypt

### Image Upload

- Product image upload with validation ([`ProductValidationService`](src/main/java/com/cts/ecommerce/service/ProductValidationService.java))
- Supported formats: JPG, JPEG, PNG, GIF, WEBP
- Image processing and storage ([`ImageService`](src/main/java/com/cts/ecommerce/service/ImageService.java))

---

## 📚 Major Annotations Used

- `@SpringBootApplication` - Main entry point
- `@Controller`, `@RestController` - Web/API controllers
- `@Service`, `@Repository` - Service and data layers
- `@Entity`, `@Table` - JPA entities
- `@Id`, `@GeneratedValue` - Primary key fields
- `@PreAuthorize` - Method-level security
- `@Valid` - Bean validation
- `@Transactional` - Transaction management
- `@ExceptionHandler`, `@ControllerAdvice` - Exception handling
- `@Autowired`, `@RequiredArgsConstructor` - Dependency injection
- `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` (Lombok) - DTOs and models

---

## 🗂️ Database Tables (Entities)

- **User:** id, email, username, password, roles, active
- **Product:** id, name, description, price, stock, imageUrl, active
- **Order:** id, user, items, status, totalPrice, timestamps
- **Cart:** id, user, items
- **CartItem:** id, cart, product, quantity
- **(Others as needed for roles, audit, etc.)**

---

## 🌐 Endpoints

### Web Endpoints

| Method | Path                            | Description         | Access |
| ------ | ------------------------------- | ------------------- | ------ |
| GET    | `/`                           | Home page           | Public |
| GET    | `/login`                      | Login page          | Public |
| POST   | `/login`                      | Login submit        | Public |
| GET    | `/register`                   | Registration page   | Public |
| POST   | `/register`                   | Register submit     | Public |
| GET    | `/products`                   | Product listing     | Public |
| GET    | `/products/{id}`              | Product detail      | Public |
| GET    | `/profile`                    | User profile        | Auth   |
| POST   | `/profile/update`             | Update profile      | Auth   |
| GET    | `/cart`                       | Shopping cart       | Auth   |
| POST   | `/cart/add/{productId}`       | Add to cart         | Auth   |
| POST   | `/cart/remove/{productId}`    | Remove from cart    | Auth   |
| GET    | `/orders`                     | Order history       | Auth   |
| POST   | `/orders/place`               | Place order         | Auth   |
| GET    | `/admin`                      | Admin dashboard     | Admin  |
| GET    | `/admin/products`             | Admin product list  | Admin  |
| POST   | `/admin/products`             | Create product      | Admin  |
| POST   | `/admin/products/{id}`        | Update product      | Admin  |
| POST   | `/admin/products/{id}/delete` | Delete product      | Admin  |
| GET    | `/admin/users`                | Admin user list     | Admin  |
| POST   | `/admin/users/{id}/delete`    | Delete user         | Admin  |
| GET    | `/admin/orders`               | Admin order list    | Admin  |
| GET    | `/admin/orders/{id}`          | Order details       | Admin  |
| POST   | `/admin/orders/{id}/status`   | Update order status | Admin  |
| POST   | `/admin/orders/{id}/cancel`   | Cancel order        | Admin  |

### API Endpoints

| Method | Path                                | Description          | Access |
| ------ | ----------------------------------- | -------------------- | ------ |
| GET    | `/products/api`                   | List products        | Public |
| GET    | `/products/api/search`            | Search products      | Public |
| GET    | `/products/api/{id}`              | Get product by ID    | Public |
| POST   | `/api/auth/register`              | Register user        | Public |
| POST   | `/api/auth/login`                 | Login user           | Public |
| GET    | `/api/cart`                       | Get cart             | Auth   |
| POST   | `/api/cart/add/{productId}`       | Add to cart          | Auth   |
| DELETE | `/api/cart/remove/{productId}`    | Remove from cart     | Auth   |
| DELETE | `/api/cart/clear`                 | Clear cart           | Auth   |
| POST   | `/api/cart/increase/{cartItemId}` | Increase quantity    | Auth   |
| POST   | `/api/cart/decrease/{cartItemId}` | Decrease quantity    | Auth   |
| GET    | `/api/orders`                     | Get orders           | Auth   |
| POST   | `/api/orders/place`               | Place order          | Auth   |
| POST   | `/api/orders/{id}/cancel`         | Cancel order         | Auth   |
| POST   | `/products/api/admin`             | Create product (API) | Admin  |
| PUT    | `/products/api/admin/{id}`        | Update product (API) | Admin  |
| DELETE | `/products/api/admin/{id}`        | Delete product (API) | Admin  |

---

## 🧩 Functionalities Implemented

- **Validation:** Bean validation for forms and DTOs, custom validation for images and product fields ([`ProductValidationService`](src/main/java/com/cts/ecommerce/service/ProductValidationService.java))
- **Image Upload:** File type/size validation, unique filename generation, image processing ([`ImageService`](src/main/java/com/cts/ecommerce/service/ImageService.java))
- **Security:** JWT authentication, role-based access, password hashing, session management ([`SecurityConfig`](src/main/java/com/cts/ecommerce/config/SecurityConfig.java))
- **Exception Handling:** Custom error pages, API error responses, unified handler ([`GlobalExceptionHandler`](src/main/java/com/cts/ecommerce/exception/GlobalExceptionHandler.java))
- **Testing:** Unit and integration tests for services, controllers, and exception handling ([`src/test/java/com/cts/ecommerce`](src/test/java/com/cts/ecommerce))
- **Soft Delete:** Products are soft-deleted from listings, not hard-deleted from DB
- **Cart & Order Logic:** Cart item transfer, stock checks, order status management

---

## 📝 How to Run

```sh
# Start the application
mvn spring-boot:run
# or
./mvnw spring-boot:run
```

- Access web UI at [http://localhost:8080](http://localhost:8080)
- API endpoints are available under `/api/` and `/products/api/`

---

## 🧪 Testing

- See [TESTING_CHECKLIST.md](TESTING_CHECKLIST.md) and [COMPREHENSIVE_TESTING_PLAN.md](COMPREHENSIVE_TESTING_PLAN.md) for step-by-step scenarios and expected results.
- Run unit tests with:
  ```sh
  mvn test
  ```

---

## 📄 Documentation

- [HELP.md](HELP.md): Getting started, reference docs, guides
- [COMPREHENSIVE_TESTING_PLAN.md](COMPREHENSIVE_TESTING_PLAN.md): Security, exception, and functional testing matrix
- [TESTING_CHECKLIST.md](TESTING_CHECKLIST.md): Quick start, endpoint lists, tools

---

## 📊 Expected Results

| Test Type              | Web Endpoints     | API Endpoints        |
| ---------------------- | ----------------- | -------------------- |
| Unauthenticated Access | Redirect to login | 401 Unauthorized     |
| Regular User → Admin  | 403 Forbidden     | 403 Forbidden        |
| 404 Errors             | 404 HTML page     | 404 JSON response    |
| 500 Errors             | 500 HTML page     | 500 JSON response    |
| Validation Errors      | Form validation   | 400 JSON with errors |

---

## 🎯 Success Criteria

- All endpoints work as expected for each role
- Security and validation are enforced
- Errors are handled gracefully
- API and web responses are consistent
- All tests pass

---

## 📬 Contact

For questions or contributions, please open an issue or submit a pull request.
