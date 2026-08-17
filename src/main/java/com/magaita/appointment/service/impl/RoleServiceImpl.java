package com.magaita.appointment.service.impl;

import com.magaita.appointment.dto.RoleDto;
import com.magaita.appointment.entity.Role;
import com.magaita.appointment.exceptions.NotFoundException;
import com.magaita.appointment.repository.RoleRepository;
import com.magaita.appointment.res.Response;
import com.magaita.appointment.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response<RoleDto> createRole(RoleDto roleDto) {

        Role role = modelMapper.map(roleDto,Role.class);

        Role savedRole = roleRepository.save(role);

        RoleDto savedroleDto = modelMapper.map(savedRole,RoleDto.class);

        return Response.<RoleDto>builder()
                .statusCode(201)
                .message("Role saved successfully!")
                .data(savedroleDto)
                .build();
    }

    @Override
    public Response<RoleDto> updateRole(RoleDto roleRequest) {
        Role existingRole = roleRepository.findById(roleRequest.getId())
                .orElseThrow(()->new NotFoundException("Role not found!"));
        modelMapper.map(roleRequest,existingRole);

        Role updatedRole = roleRepository.save(existingRole);

        RoleDto roleDto = modelMapper.map(updatedRole,RoleDto.class);

        return Response.<RoleDto>builder()
                .statusCode(200)
                .message("Role updated successfully!")
                .data(roleDto)
                .build();
    }

    @Override
    public Response<RoleDto> getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(()->new NotFoundException("Role not found"));

        RoleDto roleDto = modelMapper.map(role,RoleDto.class);
        return Response.<RoleDto>builder()
                .statusCode(200)
                .message("Role retrieved successfully!")
                .data(roleDto)
                .build();
    }

    @Override
    public Response<List<RoleDto>> getAllRole() {
        List<Role> roles = roleRepository.findAll();

        List<RoleDto> roleDto = roles.stream()
                .map(role->modelMapper.map(role,RoleDto.class))
                .toList();

        return Response.<List<RoleDto>>builder()
                .statusCode(200)
                .message("Roles retrieved successfully!")
                .data(roleDto)
                .build();
    }

    @Override
    public Response<?> deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(()->new NotFoundException("Role not found!"));

        roleRepository.delete(role);

        return Response.builder()
                .statusCode(200)
                .message("Role deleted successfully")
                .build();
    }
}
