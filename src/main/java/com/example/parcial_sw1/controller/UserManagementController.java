package com.example.parcial_sw1.controller;

import com.example.parcial_sw1.dto.ReqRes;
import com.example.parcial_sw1.entity.OurUsers;
import com.example.parcial_sw1.service.UsersManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserManagementController {
    @Autowired
    private UsersManagementService usersManagementService;

    @PostMapping("/auth/register")
    public ResponseEntity<ReqRes> register(@RequestBody ReqRes reg){
        return ResponseEntity.ok(usersManagementService.register(reg));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ReqRes> login(@RequestBody ReqRes req){
        return ResponseEntity.ok(usersManagementService.login(req));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<ReqRes> refreshToken(@RequestBody ReqRes req){
        return ResponseEntity.ok(usersManagementService.refreshToken(req));
    }

    @GetMapping("/admin/get-all-users")
    public ResponseEntity<ReqRes> getAllUsers(){
        return ResponseEntity.ok(usersManagementService.getAllUsers());

    }

    @GetMapping("/admin/get-users/{userId}")
    public ResponseEntity<ReqRes> getUSerByID(@PathVariable int userId){
        return ResponseEntity.ok(usersManagementService.getUsersById(userId));

    }

    @PutMapping("/admin/update/{userId}")
    public ResponseEntity<ReqRes> updateUser(@PathVariable int userId, @RequestBody OurUsers reqres, @RequestParam List<String> updatedRoles){
        return ResponseEntity.ok(usersManagementService.updateUser(userId, reqres, updatedRoles));
    }

    @GetMapping("/adminuser/get-profile")
    public ResponseEntity<ReqRes> getMyProfile(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        ReqRes response = usersManagementService.getMyInfo(email);
        return  ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/admin/delete/{userId}")
    public ResponseEntity<ReqRes> deleteUSer(@PathVariable int userId){
        return ResponseEntity.ok(usersManagementService.deleteUser(userId));
    }

    @GetMapping("/admin/get-users-roles/{role}")
    public ResponseEntity<ReqRes> getUsersByRole(@PathVariable String role){
        ReqRes users = usersManagementService.getUsersByRole(role);
        return ResponseEntity.ok(users);

    }

    @GetMapping("/admin/get-users-names")
    public ResponseEntity<ReqRes> getUsersByName(@RequestParam String name){
        ReqRes users = usersManagementService.searchUsersByName(name);
        return ResponseEntity.ok(users);

    }

    @GetMapping("/admin/get-users-roles-names")
    public ResponseEntity<ReqRes> getUsersByRoleAndName(@RequestParam String role, @RequestParam String name){
        ReqRes users = usersManagementService.getUsersByRoleAndName(role, name);
        return ResponseEntity.ok(users);

    }


}
