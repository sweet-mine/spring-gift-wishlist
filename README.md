# spring-gift-wishlist
# 기본 코드 준비
1. 미션1 상품 관리에서 작성했던 소스코드 로딩

# 유효성 검사 및 예외 처리
1. RequestDto에 글자수 제한, 특수문자 제한 유효성 검사 추가 , Service 레이어에서 제품명에 특정 문구 포함 여부 검사 추가
2. README.md 업데이트
3. 코드 피드백 반영

상품 수정 할 때도 금지 상품명 검사하도록 수정

예외처리 핸들러 주석 업데이트

Service 레이어에 하드 코딩돼있던 금지 단어를 application.properties에 정의, 이에 따른 application.properties 인코딩 방식 UTF-8로 변경

# 회원 로그인
1. Dto, Entity 구현 및 Controller, Service 정의
2. Dto, Entity 수정 및 AuthController 구현
3. ResponseDto 생성자 추가, Entity의 권한 필드 삭제, Service 및 Repository 레이어 구현, users TABLE 구조 선언
4. /login 기능 JWT 토큰 반환 추가, 이메일과 비밀번호 Base64 인코딩하여 데이터베이스에 저장하도록 수정, 이에 따른 User Entity 생성자 수정
5. users 테이블에 id(primary key), role(역할) 속성 추가. 이에 따른 Entity, Repository, Dto 수정
6. users 관리자 페이지 추가, 이를 위한 검색, 수정, 삭제 등의 비즈니스 로직 추가, 예외 처리 핸들러 추가
7. 코드 피드백 반영

Controller 메서드명 의도 명확해지도록 변경

base64로 인코딩하던 이메일과 비밀번호를 이메일 : AES, 비밀번호 : SHA-256 방식으로 암호화하도록 변경

로그인 실패할시 LoginFailedException을 던져 401이 반환되도록 수정

유저 정보 업데이트 메서드에 암호화 추가

애매한 것 : 역할 부여 및 토큰 검증? 현재 상황에서는 나뉘어진 역할도 없고 로그인해야만 사용 할 수 있는 기능도 없으므로 step3에서 구현하기로 결정
테스트 코드 : 테스트 코드 작성은 아직 공부가 좀 더 필요.... 일단 Postman으로 테스트