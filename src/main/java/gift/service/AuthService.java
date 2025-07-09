package gift.service;

import gift.dto.*;
import gift.entity.User;
import gift.exception.*;
import gift.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final String jwtKey;
    private final String aesKey;

    public AuthService(UserRepository userRepository, @Value("${jwt_key}") String jwtKey, @Value("${aes_key}") String aesKey) {
        this.userRepository = userRepository;
        this.jwtKey = jwtKey;
        this.aesKey = aesKey;
    }

    /**
     * SHA-256 방식으로 해싱
     * 결과를 HEX String으로 반환
     */
    public String encryptSHA256(String text) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        StringBuilder builder = new StringBuilder();
        md.update(text.getBytes());
        for (byte b : md.digest()) {
            builder.append(String.format("%02x", b));
        }

        return builder.toString();
    }

    /**
     * AES/ECB/PKCS5Padding 방식으로 평문을 암호화하고
     * 결과를 Base64 형태로 반환
     */
    public String encryptAES(String plainText) throws Exception {
        // 1) 키 준비 (hex 문자열 → 16/24/32byte)
        byte[] keyBytes = HexFormat.of().parseHex(aesKey);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        // 2) Cipher 설정 (ECB 모드, 패딩은 PKCS5)
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        // 3) 암호화 수행
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // 4) Ciphertext만 Base64로 인코딩해 반환
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * encryptAES()가 반환한 Base64 암호문을 복호화해 평문으로 반환
     */
    public String decryptAES(String cipherTextBase64) throws Exception {
        // 1) Base64 디코딩
        byte[] cipherBytes = Base64.getDecoder().decode(cipherTextBase64);

        // 2) 키 준비 (hex 문자열 → byte[])
        byte[] keyBytes = HexFormat.of().parseHex(aesKey);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

        // 3) Cipher 설정 (ECB 모드, 패딩은 PKCS5)
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        // 4) 복호화
        byte[] plainBytes = cipher.doFinal(cipherBytes);
        return new String(plainBytes, StandardCharsets.UTF_8);
    }


    public UserResponseDto userSignUp(UserRequestDto userRequestDto) {

        User user;

        // 이메일 AES 암호화, 비밀번호 SHA-256 해싱
        try {
            user = new User(
                    encryptAES(userRequestDto.email()),
                    encryptSHA256(userRequestDto.password()));
        } catch (Exception e) {
            throw new EncryptFailedException();
        }

        return new UserResponseDto(userRepository.createUser(user));
    }

    public TokenResponseDto userLogin(UserRequestDto userRequestDto) {

        User user;

        // 이메일 AES 암호화, 비밀번호 SHA-256 해싱
        try {
           user = new User(
                    encryptAES(userRequestDto.email()),
                    encryptSHA256(userRequestDto.password()));
        } catch (Exception e) {
            throw new EncryptFailedException();
        }

        // 동일한 회원 정보 체크 후 없을 경우 예외 반환
        try{
            userRepository.checkUser(user);
        } catch (Exception e) {
            throw new LoginFailedException();
        }

        // JWT 생성 후 반환
        return new TokenResponseDto(Jwts.builder()
                .setSubject(user.email())
                .signWith(Keys.hmacShaKeyFor(jwtKey.getBytes()))
                .compact());
    }

    public List<UserResponseDto> findAllUsers(){
        return userRepository.findAllUsers().stream()
                .map(user -> {
                    try {
                        return new UserResponseDto(
                                user.id(),
                                decryptAES(user.email()),
                                user.password(),
                                user.createdDate()
                        );
                    } catch (Exception e) {
                        throw new DecryptFailedException();
                    }
                })
                .collect(Collectors.toList());
    }

    public UserResponseDto findUserById(Long id) {
        User user = userRepository.findUserById(id);
        String email;

        // email 복호화
        try {
            email = decryptAES(user.email());
        } catch (Exception e) {
            throw new DecryptFailedException();
        }

        return new UserResponseDto(user.id(), email, user.password(), user.createdDate());
    }

    public void deleteUser(Long id){
        boolean flag = userRepository.deleteUser(id);
        if(!flag) {
            throw new ProductNotFoundException(id);
        }
    }

    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto){

        User user;

        try {
            user = new User(
                    encryptAES(userRequestDto.email()),
                    encryptSHA256(userRequestDto.password()));
        }
        catch (Exception e) {throw new EncryptFailedException();
        }

        boolean flag = userRepository.updateUser(id, user);

        // 수정됐는지 검증
        if(!flag) {
            throw new UserNotFoundException(id);
        }

        user = userRepository.findUserById(id);
        return new UserResponseDto(user);
    }
}
