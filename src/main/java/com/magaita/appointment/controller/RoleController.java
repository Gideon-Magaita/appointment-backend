package com.magaita.appointment.controller;


import com.magaita.appointment.dto.RoleDto;
import com.magaita.appointment.res.Response;
import com.magaita.appointment.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public ResponseEntity<Response<RoleDto>>createRole(@RequestBody RoleDto roleDto){
     return ResponseEntity.ok(roleService.createRole(roleDto));
    }

    @PutMapping
    public ResponseEntity<Response<RoleDto>>updateRole(@RequestBody RoleDto requestRole){
        return ResponseEntity.ok(roleService.updateRole(requestRole));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<RoleDto>>getRoleById(@PathVariable Long id){
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @GetMapping
    public ResponseEntity<Response<List<RoleDto>>>getAllRole(){
        return ResponseEntity.ok(roleService.getAllRole());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<?>>deleteRole(@PathVariable Long id){
        return ResponseEntity.ok(roleService.deleteRole(id));
    }
}
