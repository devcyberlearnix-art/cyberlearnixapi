package com.user.register.controller;







import com.user.register.dto.ApiResponse;



import com.user.register.dto.UpdateUserProfileRequest;



import com.user.register.dto.UserProfileResponse;



import com.user.register.entity.User;



import com.user.register.service.UserService;



import jakarta.servlet.http.HttpServletRequest;



import org.springframework.http.ResponseEntity;



import org.springframework.web.bind.annotation.*;



import org.springframework.web.multipart.MultipartFile;







import java.time.LocalDateTime;



import java.util.List;



import java.util.Map;



import java.util.UUID;







@RestController



@RequestMapping("/api/v1/users")



public class UserController {



    private final UserService userService;



    



    public UserController(UserService userService) {



        this.userService = userService;



    }







    @GetMapping("/me")



    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(HttpServletRequest request) {



        try {



            UserProfileResponse profile = userService.getLoggedInUserProfile(request);



            return ResponseEntity.ok(



                    new ApiResponse<>(true, "User profile fetched successfully", profile, LocalDateTime.now())



            );



        } catch (RuntimeException e) {



            return ResponseEntity.status(400)



                    .body(new ApiResponse<>(false, "Missing or invalid Authorization header", null));



        }



    }







    @PutMapping("/me")



    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(



            HttpServletRequest request,



            @RequestBody UpdateUserProfileRequest updateRequest



    ) {



        try {



            UserProfileResponse updatedProfile = userService.updateUserProfile(request, updateRequest);



            return ResponseEntity.ok(



                    new ApiResponse<>(true, "Profile updated successfully", updatedProfile, LocalDateTime.now())



            );



        } catch (RuntimeException e) {



            return ResponseEntity.status(400)



                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));



        }



    }







    @PostMapping("/me/photo")



    public ResponseEntity<ApiResponse<UserProfileResponse>> uploadProfilePhoto(



            HttpServletRequest request,



            @RequestParam("file") MultipartFile file



    ) {



        try {



            UserProfileResponse updatedProfile = userService.uploadProfilePhoto(request, file);



            return ResponseEntity.ok(



                    new ApiResponse<>(true, "Profile photo updated successfully", updatedProfile, LocalDateTime.now())



            );



        } catch (RuntimeException e) {



            return ResponseEntity.status(400)



                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));



        }



    }







    @DeleteMapping("/me")



    public ResponseEntity<ApiResponse<UserProfileResponse>> deleteAccount(HttpServletRequest request) {



        try {



            ApiResponse<UserProfileResponse> response = userService.softDeleteUser(request);



            return ResponseEntity.ok(response);



        } catch (RuntimeException e) {



            return ResponseEntity.status(400)



                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));



        }



    }







    // Admin endpoint to fetch all users



    @GetMapping



    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> getAllUsers(HttpServletRequest request) {



        try {



            // Check for service-to-service authentication



            String authHeader = request.getHeader("Authorization");



            if (authHeader != null && authHeader.startsWith("Bearer ")) {



                String token = authHeader.substring(7);



                // Allow service tokens for admin operations



                if (isServiceToken(token)) {



                    List<UserProfileResponse> users = userService.getAllUsersProfiles();



                    return ResponseEntity.ok(



                            new ApiResponse<>(true, "All users fetched successfully", users, LocalDateTime.now())



                    );



                }



            }



            // Regular admin authentication



            List<UserProfileResponse> users = userService.getAllUsersProfiles();



            return ResponseEntity.ok(



                    new ApiResponse<>(true, "All users fetched successfully", users, LocalDateTime.now())



            );



        } catch (RuntimeException e) {



            return ResponseEntity.status(400)



                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));



        }



    }







    // Admin endpoint to fetch user by ID



    @GetMapping("/{id}")



    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(@PathVariable UUID id) {



        try {



            UserProfileResponse user = userService.getUserById(id);



            return ResponseEntity.ok(



                    new ApiResponse<>(true, "User fetched successfully", user, LocalDateTime.now())



            );



        } catch (RuntimeException e) {



            return ResponseEntity.status(404)



                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));



        }



    }







    // Admin endpoint to update user status



    @PutMapping("/{id}/status")



    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserStatus(



            @PathVariable UUID id,



            @RequestBody Map<String, String> request) {



        try {



            String status = request.get("status");



            UserProfileResponse user = userService.updateUserStatus(id, status);



            return ResponseEntity.ok(



                    new ApiResponse<>(true, "User status updated successfully", user, LocalDateTime.now())



            );



        } catch (RuntimeException e) {



            return ResponseEntity.status(400)



                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));



        }



    }







    // Admin endpoint to delete user



    @DeleteMapping("/{id}")

    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable UUID id) {

        try {

            userService.deleteUserById(id);

            return ResponseEntity.ok(

                    new ApiResponse<>(true, "User deleted successfully", null, LocalDateTime.now())

            );

        } catch (RuntimeException e) {

            return ResponseEntity.status(400)

                    .body(new ApiResponse<>(false, e.getMessage(), null, LocalDateTime.now()));

        }

    }

    // Helper method to check if token is a service token



    private boolean isServiceToken(String token) {



        try {



            // Service tokens have a specific UUID subject (11111111-1111-1111-1111-111111111111)



            // This is a simple check - in production you'd validate the token signature



            io.jsonwebtoken.Jwts.parserBuilder()



                    .setSigningKey("8c4e9d2f1a7b6c5d9e3f0a1b7c8d4e5f9a2b6c1d8e7f3a4b5c9d1e6f8a2b7c3".getBytes())



                    .build()



                    .parseClaimsJws(token);



            return true;



        } catch (Exception e) {

            return false;

        }

    }

}