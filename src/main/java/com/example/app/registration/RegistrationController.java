package com.example.app.registration;

import com.example.app.api_response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @RequestBody LoginRequest request
    ){
     var res = authenticationService.authenticate(request);
     return ResponseEntity.ok(
             new ApiResponse<>(
                     true,res,"success"
             )
     );
    }
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @RequestBody RegisterRequest request
    ) {
        AuthenticationResponse res
                = authenticationService.register(request);
        return ResponseEntity.ok(new ApiResponse<>(
                         true,
                         res,
                         "user successfully registered"
                 )
         );
    }

}
