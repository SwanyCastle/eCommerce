# 🛒 Ecommerce Project
이커머스 프로젝트 

# 🛠️ 사용 기술
<table>
  <thead>
    <th>Tech</th>
    <th>Version</th>
  </thead>
  <tbody>
    <tr>
      <td><img src="https://img.shields.io/badge/java-007396?style=for-the-badge&logo=OpenJDK&logoColor=white"></td>
      <td>17</td>
    </tr>
    <tr>
      <td><img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"></td>
      <td>3.3.4</td>
    </tr>
    <tr>
      <td><img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white"></td>
      <td>8.10.2</td>
    </tr>
    <tr>
      <td><img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white"></td>
      <td>9.0.1</td>
    </tr>
    <tr>
      <td><img src="https://img.shields.io/badge/spring data jpa-6DB33F?style=for-the-badge&logo=spring data jpa&logoColor=white"></td>
      <td>3.3.4</td>
    </tr> 
    </tr>
      <tr>
      <td><img src="https://img.shields.io/badge/lombok-6DB33F?style=for-the-badge&logo=lombok&logoColor=white"></td>
      <td>1.18.34</td>
    </tr>
    <tr>
      <td><img src="https://img.shields.io/badge/springsecurity-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white"></td>
      <td>3.3.4</td>
    </tr>  
    <tr>
      <td><img src="https://img.shields.io/badge/jjwt-E34F26?style=for-the-badge&logo=jjwt&logoColor=white"></td>
      <td>0.11.2</td>
    </tr>
    <tr>
      <td><img src="https://img.shields.io/badge/redis-FF4438?style=for-the-badge&logo=redis&logoColor=white"></td>
      <td>7.4.0</td>
  </tbody>
</table>


# 🗓️ 프로젝트 기능 정의
### 사용자

  - 회원가입 기능 : 회원가입시 기본적으로 사용자의 권한은 ROLE_CUSTOMER 권한과 회원 유형(type) 은 app 을 기본으로 가지고 판매자 회원 가입시 ROLE_SELLER 권한으로 회원가입을 진행한다.
                회원가입시 아이디, 비빌번호, 이메일 정보를 필수로 받아 회원가입을 진행한다. 아이디가 중복될 시 회원가입 되지 않도록 하고
                입력받은 이메일로 인증 번호를 발송해 해당 사용자의 이메일이 맞는지 확인하는 이메일 인증이 된 사용자들만 회원가입할 수 있도록 구현한다.
    
  - 로그인 기능 : 사용자의 아이디, 비밀번호륿 받아 유효한 사용자인지 검중한 후 JWT 토큰을 활용하여 해당 사용자의 PK, 아이디를 포함하는
               Access Token 발급 하여 응답한다. 아이디, 비밀번호 로그인 뿐 아니라 OAuth2 를 사용해서 kakao, naver 소셜 로그인도 마찬가지로
               로그인이 되면 우리 서비스의 Access Token 을 발급 하도록 구현한다.
    
  - 로그아웃 기능 : 로그아웃 시 로그인으로 발급된 토큰을 재사용 할 수 없도록 사용하여 블랙리스트에 등록해 처리한다.

  - 회원 정보 조회, 수정, 삭제 : 회원정보 조회, 수정 삭제 시 해당 유저만 조회, 수정 삭제 하도록 구현 한다.

### 상품

  - 상품 등록 : 유저의 권한이 SELLER 인 유저만 등록 가능하도록 한다. ( 추후 상품 사진 등록도 가능하도록 기능 추가 )
    
  - 상품 목록 조회 : 상품 목록 조회시 페이징 처리 및 조회시 전달된 검색어가 있다면 검색어를 기준으로 검색 및 정렬해서 조회한다.

  - 특전 판매자가 등록한 상품 목록 조회 : 유저의 권한이 SELLER 인 유저가 등록한 상품목록을 페이징 처리하여 조회한다.
    
  - 상품 상세 정보 조회 : 상품의 상세정보를 조회한다.
    
  - 상품 정보 수정, 삭제 : 유저 권한이 SELLER 인 유저가 자신의 상품만 수정, 삭제 가능하도록 구현한다.

### 장바구니

  - 장바구니 등록 : 유저 권한이 CUSTOMER 인 사용자만 생성 가능하도록 구현하고 장바구니 내에 있는 여러상품을 받아서 등록 하도록 구현한다.

  - 장바구니 목록 조회 : 유저 권한이 CUSTOMER 인 사용자만장바구니 목록 조회시 생성일자 및 판매자 id 를 기준으로 정렬하고 페이징 처리하여 조회하다.

  - 장바구니 수정, 삭제 : 유저 권한이 CUSTOMER 인 사용자만 장바구니에 상품의 정보를 수정, 삭제 하도록 구현한다.

  - 장바구니 목록 전체 삭제 : 유저 권한이 CUSTOMER 인 사용자만 해당사용자가 장바구니에 등록한 모든 상품의 정보를 삭제하도록 구현한다.

### 리뷰

  - 리뷰 등록 : 유저 권한이 CUSTOMER 인 사용자만 리뷰 생성 가능하도록 구현한다. ( 추후 리뷰 이미지 등록 기능 추가 )

  - 특정 사용자가 작성한 리뷰 목록 조회 : 유저 권한이 CUSTOMER 인 사용자만 해당 사용자가 작성한 리뷰를 최근 날짜순으로 작성한 리뷰 목록 조회한다.

  - 특정 상품에 대한 리뷰 목록 조회 : 특정 상품에 대한 리뷰 목록을 최근 날짜순, 별점 높은순, 별점 낮은순 기준으로 정렬해서 조회한다.

  - 리뷰 상세 정보 조회 : 리뷰 상세 정보를 조회한다.

  - 리뷰 정보 수정 : 유저 권한이 CUSTOMER 인 사용자만 해당 사용자가 작성한 리뷰를 수정 가능하도록 구현한다.

  - 리뷰 정보 삭제 : 리뷰 작성자 및 해당 상품 게시한 판매자만 리뷰 삭제 가능하도록 기능 구현한다.

# ERD
<img width="1120" alt="ecommerce-erd" src="https://github.com/user-attachments/assets/c9372158-ec77-4fac-b59b-5a7d648fd78a">

