package com.fintech.server.controller;

// import com.fintech.server.dto.UserRequestDto; // 사용하지 않으므로 주석 처리
// import com.fintech.server.dto.UserResponseDto; // 사용하지 않으므로 주석 처리
// import com.fintech.server.service.UserService; // 사용하지 않으므로 주석 처리
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    // private final UserService userService; // 모든 메서드가 주석 처리되어 사용하지 않음

    // ====================================================================
    // 아래의 모든 메소드들은 UserService에 해당 기능이 다시 구현될 때까지
    // 임시로 주석 처리하여 비활성화합니다.
    // 이렇게 해야 서버가 정상적으로 실행될 수 있습니다.
    // ====================================================================

    /*
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllActiveUsers() {
        List<User> users = userService.findAllActiveUsers();
        List<UserResponseDto> userDtos = users.stream()
                .map(UserResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserResponseDto> getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(UserResponseDto::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody UserRequestDto.Create request) {
        try {
            User user = userService.createUser(
                    request.getUsername(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getFullName()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(UserResponseDto.from(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long userId, 
                                                      @RequestBody UserRequestDto.Update request) {
        try {
            User user = userService.updateUser(userId, request.getFullName());
            return ResponseEntity.ok(UserResponseDto.from(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long userId) {
        try {
            userService.deactivateUser(userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    */
}
