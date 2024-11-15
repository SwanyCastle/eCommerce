# 🛒 Ecommerce Project
이커머스 프로젝트

# 🗓️ 프로젝트 기능 정의

### 📌 사용자

- **회원가입 기능**: 회원가입 시 기본적으로 사용자의 권한은 `ROLE_CUSTOMER` 권한과 회원 유형(type)은 `app`을 기본으로 하며, 판매자 회원가입 시 `ROLE_SELLER` 권한으로 회원가입을 진행합니다. 회원가입 시 아이디, 비밀번호, 이메일을 필수로 받으며, 아이디가 중복되면 회원가입이 되지 않도록 처리합니다. 입력한 이메일로 인증 번호를 발송하여 이메일 인증을 받은 사용자만 회원가입할 수 있습니다.
  
- **로그인 기능**: 사용자의 아이디와 비밀번호를 받아 유효한 사용자인지 확인한 후 JWT 토큰을 활용해 해당 사용자의 아이디를 포함하는 Access Token을 발급하여 redis 에 사용자의 ID 값을 키로 저장 후 응답합니다. 아이디/비밀번호 로그인 외에도 Kakao, Naver 소셜 로그인을 통해서도 Access Token을 발급하고 redis 에 사용자의 ID 값을 키로 저장 후 응답합니다.

- **로그아웃 기능**: 로그아웃 시 로그인으로 발급된 토큰을 재사용할 수 없도록 로그아웃 시 redis 에 사용자 ID 를 키 값으로 저장되어있는 Access Tokne 값을 삭제 처리합니다.

- **회원 정보 조회, 수정, 삭제**: 회원정보 조회, 수정, 삭제 시 해당 유저만 조회, 수정, 삭제할 수 있도록 구현합니다.

---

### 📌 상품

- **상품 등록**: 유저의 권한이 `SELLER`인 유저만 등록할 수 있도록 합니다.

- **상품 목록 조회**: 상품 목록 조회 시 페이징 처리 및 전달된 검색어가 있으면 해당 검색어를 기준으로 검색 및 정렬하여 조회합니다.

- **특정 판매자가 등록한 상품 목록 조회**: 유저의 권한이 `SELLER`인 유저가 등록한 상품 목록을 페이징 처리하여 조회합니다.

- **상품 상세 정보 조회**: 상품의 상세정보를 조회합니다.

- **상품 정보 수정, 삭제**: 유저 권한이 `SELLER`인 유저가 자신의 상품만 수정하거나 삭제할 수 있습니다.

---

### 📌 장바구니

- **장바구니 등록**: 유저 권한이 `CUSTOMER`인 사용자만 생성할 수 있으며, 여러 상품을 장바구니에 등록할 수 있도록 구현합니다.

- **장바구니 목록 조회**: 유저 권한이 `CUSTOMER`인 사용자만 장바구니 목록을 조회할 수 있으며, 장바구니 내에 있는 상품의 목록을 조회하고 장바구니 내의 각 상품마다의 갯수에 따른 총 가격을 계산하고, 장바구니 전체 총 가격을 계산해 응답합니다.

- **장바구니 수정, 삭제**: 유저 권한이 `CUSTOMER`인 사용자만 장바구니에 있는 상품의 수량을 수정 할 수 있고, 장바구니에 담은 상품을 장바구니에서 삭제할 수 있습니다.

- **장바구니 목록 전체 삭제**: 유저 권한이 `CUSTOMER`인 사용자만 자신의 장바구니에 등록한 모든 상품을 삭제할 수 있습니다.

---

### 📌 리뷰

- **리뷰 등록**: 유저 권한이 `CUSTOMER`인 사용자만 리뷰를 생성할 수 있습니다. 리뷰 등록시 특정 샅품에 대한 리뷰들의 평점의 평균값을 상품의 평점으로 업데이트 합니다.

- **특정 사용자가 작성한 리뷰 목록 조회**: 유저 권한이 `CUSTOMER`인 사용자만 자신이 작성한 리뷰를 최근 날짜순, 별점 높은 순, 별점 낮은 순으로 조회할 수 있습니다.

- **특정 상품에 대한 리뷰 목록 조회**: 특정 상품에 대한 리뷰 목록을 최근 날짜순, 별점 높은 순, 별점 낮은 순으로 정렬하여 조회할 수 있습니다.

- **리뷰 상세 정보 조회**: 리뷰의 상세 정보를 조회합니다.

- **리뷰 정보 수정**: 유저 권한이 `CUSTOMER`인 사용자만 자신이 작성한 리뷰를 수정할 수 있습니다. 리뷰 수정시 특정 샅품에 대한 리뷰들의 평점의 평균값을 상품의 평점으로 업데이트 합니다.

- **리뷰 정보 삭제**: 유저 권한이 `CUSTOMER`인 사용자만 자신이 작성한 리뷰를 삭제할 수 있습니다. 리뷰 삭제시 특정 샅품에 대한 리뷰들의 평점의 평균값을 상품의 평점으로 업데이트 합니다.

---

# 🛠️ 사용 기술
| Tech                                  | Version |
|---------------------------------------|---------|
| ![Java](https://img.shields.io/badge/java-007396?style=for-the-badge&logo=OpenJDK&logoColor=white) | 17      |
| ![Spring Boot](https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white) | 3.3.4   |
| ![Gradle](https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white) | 8.10.2  |
| ![MySQL](https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white) | 9.0.1   |
| ![Spring Data JPA](https://img.shields.io/badge/spring%20data%20jpa-6DB33F?style=for-the-badge&logo=spring%20data%20jpa&logoColor=white) | 3.3.4   |
| ![Lombok](https://img.shields.io/badge/lombok-6DB33F?style=for-the-badge&logo=lombok&logoColor=white) | 1.18.34 |
| ![Spring Security](https://img.shields.io/badge/springsecurity-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white) | 3.3.4   |
| ![JWT](https://img.shields.io/badge/jjwt-E34F26?style=for-the-badge&logo=jjwt&logoColor=white) | 0.11.2  |
| ![Redis](https://img.shields.io/badge/redis-FF4438?style=for-the-badge&logo=redis&logoColor=white) | 7.4.0   |

# ERD
![Ecommerce ERD](https://github.com/user-attachments/assets/a0f6d344-1a2b-41aa-977f-1e9e46dc75fb)
